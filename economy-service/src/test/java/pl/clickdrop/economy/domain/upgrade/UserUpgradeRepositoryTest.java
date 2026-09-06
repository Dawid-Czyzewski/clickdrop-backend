package pl.clickdrop.economy.domain.upgrade;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserUpgradeRepositoryTest {

    @Autowired
    UserUpgradeRepository userUpgradeRepository;

    @Autowired
    UpgradeRepository upgradeRepository;

    @Test
    void savesWithDefaultLevelZero_andTimestamps() {
        Long upgradeId = upgradeId("BETTER_MOUSE");

        UserUpgrade userUpgrade = new UserUpgrade();
        userUpgrade.setUserId(15L);
        userUpgrade.setUpgradeId(upgradeId);

        UserUpgrade saved = userUpgradeRepository.saveAndFlush(userUpgrade);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUserId()).isEqualTo(15L);
        assertThat(saved.getUpgradeId()).isEqualTo(upgradeId);
        assertThat(saved.getLevel()).isZero();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void findByUserId_returnsOnlyThatUsersUpgrades() {
        Long betterMouseId = upgradeId("BETTER_MOUSE");
        Long gamingMouseId = upgradeId("GAMING_MOUSE");

        userUpgradeRepository.saveAndFlush(userUpgrade(15L, betterMouseId, 3));
        userUpgradeRepository.saveAndFlush(userUpgrade(15L, gamingMouseId, 1));
        userUpgradeRepository.saveAndFlush(userUpgrade(99L, betterMouseId, 2));

        List<UserUpgrade> forUser15 = userUpgradeRepository.findByUserId(15L);

        assertThat(forUser15).hasSize(2);
        assertThat(forUser15).extracting(UserUpgrade::getUpgradeId)
                .containsExactlyInAnyOrder(betterMouseId, gamingMouseId);
        assertThat(forUser15).allMatch(uu -> uu.getUserId().equals(15L));
    }

    @Test
    void findByUserIdAndUpgradeId_returnsExactRow() {
        Long upgradeId = upgradeId("GAMING_SETUP");
        userUpgradeRepository.saveAndFlush(userUpgrade(15L, upgradeId, 0));

        UserUpgrade found = userUpgradeRepository
                .findByUserIdAndUpgradeId(15L, upgradeId)
                .orElseThrow();

        assertThat(found.getLevel()).isZero();
        assertThat(userUpgradeRepository.existsByUserIdAndUpgradeId(15L, upgradeId)).isTrue();
        assertThat(userUpgradeRepository.existsByUserIdAndUpgradeId(15L, upgradeId("BETTER_MOUSE"))).isFalse();
    }

    @Test
    void rejectsDuplicateUserIdAndUpgradeId() {
        Long upgradeId = upgradeId("BETTER_MOUSE");
        userUpgradeRepository.saveAndFlush(userUpgrade(15L, upgradeId, 3));

        UserUpgrade duplicate = userUpgrade(15L, upgradeId, 1);

        assertThatThrownBy(() -> userUpgradeRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void allowsSameUpgradeForDifferentUsers() {
        Long upgradeId = upgradeId("AI_AUTO_CLICKER");

        userUpgradeRepository.saveAndFlush(userUpgrade(15L, upgradeId, 1));
        UserUpgrade otherUser = userUpgradeRepository.saveAndFlush(userUpgrade(16L, upgradeId, 2));

        assertThat(otherUser.getId()).isNotNull();
        assertThat(userUpgradeRepository.findByUserIdAndUpgradeId(16L, upgradeId)).isPresent();
    }

    private Long upgradeId(String code) {
        return upgradeRepository.findByCode(code).orElseThrow().getId();
    }

    private static UserUpgrade userUpgrade(Long userId, Long upgradeId, int level) {
        UserUpgrade userUpgrade = new UserUpgrade();
        userUpgrade.setUserId(userId);
        userUpgrade.setUpgradeId(upgradeId);
        userUpgrade.setLevel(level);
        return userUpgrade;
    }
}