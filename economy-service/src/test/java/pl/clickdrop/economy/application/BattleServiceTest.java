package pl.clickdrop.economy.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.clickdrop.economy.api.dto.BattleResponse;
import pl.clickdrop.economy.api.dto.CreateBattleRequest;
import pl.clickdrop.economy.application.exception.InsufficientFundsException;
import pl.clickdrop.economy.domain.battle.Battle;
import pl.clickdrop.economy.domain.battle.BattleRepository;
import pl.clickdrop.economy.domain.battle.BattleRoundDrop;
import pl.clickdrop.economy.domain.battle.BattleRoundDropRepository;
import pl.clickdrop.economy.domain.battle.BattleStatus;
import pl.clickdrop.economy.domain.cases.CaseDrop;
import pl.clickdrop.economy.domain.cases.CaseDropRepository;
import pl.clickdrop.economy.domain.cases.GameCase;
import pl.clickdrop.economy.domain.cases.GameCaseRepository;
import pl.clickdrop.economy.domain.inventory.UserInventoryItem;
import pl.clickdrop.economy.domain.inventory.UserInventoryItemRepository;
import pl.clickdrop.economy.domain.item.Item;
import pl.clickdrop.economy.domain.item.ItemExterior;
import pl.clickdrop.economy.domain.item.ItemRarity;
import pl.clickdrop.economy.domain.item.ItemRepository;
import pl.clickdrop.economy.domain.wallet.Wallet;
import pl.clickdrop.economy.domain.wallet.WalletRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BattleServiceTest {

    private static final Long USER = 15L;
    private static final Long CASE_ID = 3L;

    @Mock BattleRepository battleRepository;
    @Mock BattleRoundDropRepository battleRoundDropRepository;
    @Mock GameCaseRepository gameCaseRepository;
    @Mock CaseDropRepository caseDropRepository;
    @Mock ItemRepository itemRepository;
    @Mock WalletRepository walletRepository;
    @Mock UserInventoryItemRepository inventoryItemRepository;
    @Mock BattleDropRoller battleDropRoller;
    @Mock BattleResponseMapper battleResponseMapper;

    @InjectMocks
    BattleService battleService;

    @Test
    void create_waitingBattle_chargesEntryCost() {
        GameCase gameCase = gameCase(CASE_ID, "65.00");
        Wallet wallet = wallet(USER, "200.00");
        when(gameCaseRepository.findById(CASE_ID)).thenReturn(Optional.of(gameCase));
        when(walletRepository.findByUserId(USER)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(inv -> inv.getArgument(0));
        when(battleRepository.save(any(Battle.class))).thenAnswer(inv -> {
            Battle b = inv.getArgument(0);
            b.setId(9L);
            return b;
        });
        when(battleResponseMapper.toResponse(any(Battle.class), anyList()))
                .thenAnswer(inv -> mapped(inv.getArgument(0), List.of()));

        BattleResponse result = battleService.create(USER, new CreateBattleRequest(CASE_ID, 1, false));

        assertThat(result.status()).isEqualTo(BattleStatus.WAITING);
        assertThat(result.entryCost()).isEqualByComparingTo("65.00");
        assertThat(wallet.getBalance()).isEqualByComparingTo("135.00");
        verify(battleDropRoller, never()).roll(any(), anyList(), any());
    }

    @Test
    void create_vsBot_finishesAndAwardsLootToWinner() {
        GameCase gameCase = gameCase(CASE_ID, "65.00");
        Wallet wallet = wallet(USER, "200.00");
        Item item = item(10L, "USP_A", ItemRarity.MIL_SPEC);
        CaseDrop drop = caseDrop(CASE_ID, 10L);

        when(gameCaseRepository.findById(CASE_ID)).thenReturn(Optional.of(gameCase));
        when(walletRepository.findByUserId(USER)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(inv -> inv.getArgument(0));
        when(battleRepository.save(any(Battle.class))).thenAnswer(inv -> {
            Battle b = inv.getArgument(0);
            if (b.getId() == null) b.setId(9L);
            return b;
        });
        when(caseDropRepository.findByCaseId(CASE_ID)).thenReturn(List.of(drop));
        when(itemRepository.findAllById(anyList())).thenReturn(List.of(item));

        var high = new BattleDropRoller.RolledDrop(
                10L, item, ItemExterior.FACTORY_NEW, false, new BigDecimal("40.00"));
        var low = new BattleDropRoller.RolledDrop(
                10L, item, ItemExterior.BATTLE_SCARRED, false, new BigDecimal("10.00"));
        when(battleDropRoller.roll(any(), anyList(), any())).thenReturn(high, low);

        AtomicLong invIds = new AtomicLong(100);
        when(inventoryItemRepository.save(any(UserInventoryItem.class))).thenAnswer(inv -> {
            UserInventoryItem row = inv.getArgument(0);
            row.setId(invIds.getAndIncrement());
            return row;
        });
        when(battleRoundDropRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
        when(battleResponseMapper.toResponse(any(Battle.class), anyList()))
                .thenAnswer(inv -> mapped(inv.getArgument(0), inv.getArgument(1)));

        BattleResponse result = battleService.create(USER, new CreateBattleRequest(CASE_ID, 1, true));

        assertThat(result.status()).isEqualTo(BattleStatus.FINISHED);
        assertThat(result.winnerUserId()).isEqualTo(USER);
        assertThat(result.player1TotalValue()).isEqualByComparingTo("40.00");
        assertThat(result.player2TotalValue()).isEqualByComparingTo("10.00");
        assertThat(result.roundsDrops()).hasSize(2);
        assertThat(result.roundsDrops()).allMatch(d -> d.inventoryItemId() != null);

        ArgumentCaptor<UserInventoryItem> invCaptor = ArgumentCaptor.forClass(UserInventoryItem.class);
        verify(inventoryItemRepository, atLeastOnce()).save(invCaptor.capture());
        assertThat(invCaptor.getAllValues()).allMatch(row -> USER.equals(row.getUserId()));
    }

    @Test
    void create_insufficientFunds_throwsWithoutBattle() {
        GameCase gameCase = gameCase(CASE_ID, "65.00");
        Wallet wallet = wallet(USER, "10.00");
        when(gameCaseRepository.findById(CASE_ID)).thenReturn(Optional.of(gameCase));
        when(walletRepository.findByUserId(USER)).thenReturn(Optional.of(wallet));

        assertThatThrownBy(() -> battleService.create(USER, new CreateBattleRequest(CASE_ID, 1, true)))
                .isInstanceOf(InsufficientFundsException.class);

        verify(battleRepository, never()).save(any());
    }

    private static BattleResponse mapped(Battle battle, List<BattleRoundDrop> drops) {
        return new BattleResponse(
                battle.getId(),
                battle.getCaseId(),
                battle.getRounds(),
                battle.getStatus(),
                battle.getPlayer1UserId(),
                battle.getPlayer2UserId(),
                battle.getPlayer1TotalValue(),
                battle.getPlayer2TotalValue(),
                battle.getWinnerUserId(),
                battle.getEntryCost(),
                drops.stream().map(d -> new pl.clickdrop.economy.api.dto.BattleDropDto(
                        d.getRoundIndex(),
                        d.getSide(),
                        d.getItemId(),
                        "USP_A",
                        "USP A",
                        ItemRarity.MIL_SPEC,
                        d.getExterior(),
                        d.isStatTrack(),
                        d.getDropValue(),
                        d.getInventoryItemId()
                )).toList()
        );
    }

    private static GameCase gameCase(Long id, String price) {
        GameCase gameCase = new GameCase();
        gameCase.setId(id);
        gameCase.setCode("CS20_CASE");
        gameCase.setPrice(new BigDecimal(price));
        gameCase.setActive(true);
        gameCase.setMinLevel(1);
        return gameCase;
    }

    private static Wallet wallet(Long userId, String balance) {
        Wallet wallet = new Wallet();
        wallet.setUserId(userId);
        wallet.setBalance(new BigDecimal(balance));
        return wallet;
    }

    private static Item item(Long id, String code, ItemRarity rarity) {
        Item item = new Item();
        item.setId(id);
        item.setCode(code);
        item.setDisplayName(code.replace('_', ' '));
        item.setRarity(rarity);
        item.setActive(true);
        return item;
    }

    private static CaseDrop caseDrop(Long caseId, Long itemId) {
        CaseDrop drop = new CaseDrop();
        drop.setCaseId(caseId);
        drop.setItemId(itemId);
        drop.setWeight(100);
        return drop;
    }
}
