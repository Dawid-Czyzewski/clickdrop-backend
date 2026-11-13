package pl.clickdrop.economy.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.clickdrop.economy.api.dto.BattleResponse;
import pl.clickdrop.economy.api.dto.CreateBattleRequest;
import pl.clickdrop.economy.application.exception.*;
import pl.clickdrop.economy.domain.battle.*;
import pl.clickdrop.economy.domain.cases.CaseDrop;
import pl.clickdrop.economy.domain.cases.CaseDropRepository;
import pl.clickdrop.economy.domain.cases.GameCase;
import pl.clickdrop.economy.domain.cases.GameCaseRepository;
import pl.clickdrop.economy.domain.inventory.UserInventoryItem;
import pl.clickdrop.economy.domain.inventory.UserInventoryItemRepository;
import pl.clickdrop.economy.domain.item.Item;
import pl.clickdrop.economy.domain.item.ItemRepository;
import pl.clickdrop.economy.domain.wallet.Wallet;
import pl.clickdrop.economy.domain.wallet.WalletRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BattleService {

    private static final Set<Integer> ALLOWED_ROUNDS = Set.of(1, 3, 5);

    private final BattleRepository battleRepository;
    private final BattleRoundDropRepository battleRoundDropRepository;
    private final GameCaseRepository gameCaseRepository;
    private final CaseDropRepository caseDropRepository;
    private final ItemRepository itemRepository;
    private final WalletRepository walletRepository;
    private final UserInventoryItemRepository inventoryItemRepository;
    private final BattleDropRoller battleDropRoller;
    private final BattleResponseMapper battleResponseMapper;

    @Transactional
    public BattleResponse create(Long userId, CreateBattleRequest request) {
        int rounds = requireRounds(request == null ? null : request.rounds());
        GameCase gameCase = requireCase(request == null ? null : request.caseId());
        BigDecimal entryCost = gameCase.getPrice().multiply(BigDecimal.valueOf(rounds));
        charge(userId, entryCost);

        Battle battle = new Battle();
        battle.setCaseId(gameCase.getId());
        battle.setRounds(rounds);
        battle.setStatus(BattleStatus.WAITING);
        battle.setPlayer1UserId(userId);
        battle.setEntryCost(entryCost);
        battle = battleRepository.save(battle);

        if (request != null && Boolean.TRUE.equals(request.vsBot())) {
            return finish(battle, null);
        }
        return battleResponseMapper.toResponse(battle, List.of());
    }

    @Transactional
    public BattleResponse join(Long userId, Long battleId) {
        Battle battle = battleRepository.findById(battleId)
                .orElseThrow(() -> new BattleNotFoundException(battleId));
        if (battle.getStatus() != BattleStatus.WAITING || battle.getPlayer2UserId() != null) {
            throw new BattleNotJoinableException(battleId);
        }
        if (Objects.equals(battle.getPlayer1UserId(), userId)) {
            throw new BattleNotJoinableException(battleId);
        }
        charge(userId, battle.getEntryCost());
        return finish(battle, userId);
    }

    @Transactional(readOnly = true)
    public BattleResponse get(Long battleId) {
        Battle battle = battleRepository.findById(battleId)
                .orElseThrow(() -> new BattleNotFoundException(battleId));
        List<BattleRoundDrop> drops =
                battleRoundDropRepository.findByBattleIdOrderByRoundIndexAscSideAsc(battleId);
        return battleResponseMapper.toResponse(battle, drops);
    }

    private BattleResponse finish(Battle battle, Long player2UserId) {
        battle.setPlayer2UserId(player2UserId);

        List<CaseDrop> pool = caseDropRepository.findByCaseId(battle.getCaseId());
        if (pool.isEmpty()) {
            throw new CaseDropPoolEmptyException(battle.getCaseId());
        }
        Map<Long, Item> itemsById = itemRepository.findAllById(
                pool.stream().map(CaseDrop::getItemId).distinct().toList()
        ).stream().collect(Collectors.toMap(Item::getId, Function.identity()));

        List<BattleRoundDrop> drops = new ArrayList<>(battle.getRounds() * 2);
        BigDecimal p1 = BigDecimal.ZERO;
        BigDecimal p2 = BigDecimal.ZERO;

        for (int round = 0; round < battle.getRounds(); round++) {
            var a = battleDropRoller.roll(battle.getCaseId(), pool, itemsById);
            var b = battleDropRoller.roll(battle.getCaseId(), pool, itemsById);
            drops.add(toEntity(battle.getId(), round, BattleSide.PLAYER1, a));
            drops.add(toEntity(battle.getId(), round, BattleSide.PLAYER2, b));
            p1 = p1.add(a.value());
            p2 = p2.add(b.value());
        }

        battle.setPlayer1TotalValue(p1);
        battle.setPlayer2TotalValue(p2);
        Long winnerUserId = resolveWinner(battle.getPlayer1UserId(), player2UserId, p1, p2);
        battle.setWinnerUserId(winnerUserId);
        battle.setStatus(BattleStatus.FINISHED);
        battle.setFinishedAt(Instant.now());
        battleRepository.save(battle);

        if (winnerUserId != null) {
            awardLoot(winnerUserId, drops);
        }
        battleRoundDropRepository.saveAll(drops);
        return battleResponseMapper.toResponse(battle, drops);
    }

    private void awardLoot(Long winnerUserId, List<BattleRoundDrop> drops) {
        for (BattleRoundDrop drop : drops) {
            UserInventoryItem inv = new UserInventoryItem();
            inv.setUserId(winnerUserId);
            inv.setItemId(drop.getItemId());
            inv.setExterior(drop.getExterior());
            inv.setStatTrack(drop.isStatTrack());
            inv.setFloatValue(drop.getFloatValue());
            inv.setPaintSeed(drop.getPaintSeed());
            inv = inventoryItemRepository.save(inv);
            drop.setInventoryItemId(inv.getId());
        }
    }

    private static Long resolveWinner(Long p1, Long p2, BigDecimal v1, BigDecimal v2) {
        int cmp = v1.compareTo(v2);
        if (cmp > 0) return p1;
        if (cmp < 0) return p2;
        return p1;
    }

    private void charge(Long userId, BigDecimal amount) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new WalletNotFoundException(userId));
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException();
        }
        wallet.setBalance(wallet.getBalance().subtract(amount));
        walletRepository.save(wallet);
    }

    private GameCase requireCase(Long caseId) {
        if (caseId == null) {
            throw new CaseNotFoundException(0L);
        }
        return gameCaseRepository.findById(caseId)
                .filter(GameCase::isActive)
                .orElseThrow(() -> new CaseNotFoundException(caseId));
    }

    private static int requireRounds(Integer rounds) {
        if (rounds == null || !ALLOWED_ROUNDS.contains(rounds)) {
            throw new InvalidBattleRoundsException();
        }
        return rounds;
    }

    private static BattleRoundDrop toEntity(
            Long battleId, int round, BattleSide side, BattleDropRoller.RolledDrop rolled
    ) {
        BattleRoundDrop drop = new BattleRoundDrop();
        drop.setBattleId(battleId);
        drop.setRoundIndex(round);
        drop.setSide(side);
        drop.setItemId(rolled.itemId());
        drop.setExterior(rolled.exterior());
        drop.setStatTrack(rolled.statTrack());
        drop.setFloatValue(rolled.floatValue());
        drop.setPaintSeed(rolled.paintSeed());
        drop.setDropValue(rolled.value());
        return drop;
    }
}
