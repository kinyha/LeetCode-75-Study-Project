package exercise.yandex.dev.tasks.matchmaking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MatchMakingTest {

    MatchMaking matchMaking;
    List<Player> testPlayers;

    @BeforeEach
    void setUp() {
        matchMaking = new MatchMaking();
        testPlayers = createTestDataset();
    }

    /**
     * 30 игроков, распределение ролей (популярность):
     * MID: 9 (самая популярная)
     * TOP: 7
     * ADC: 6
     * JUNGLE: 5
     * SUP: 3 (наименее популярная)
     *
     * MMR: 850-2950, неравномерно (больше в середине ~2000)
     */
    List<Player> createTestDataset() {
        List<Player> players = new ArrayList<>();

        // ===== MID MAINS (9) - самая популярная роль =====
        players.add(new Player("MidGod", 2850, Role.MID, Role.TOP));
        players.add(new Player("Faker_Fan", 2400, Role.MID, Role.ADC));
        players.add(new Player("MidOrFeed", 1950, Role.MID, Role.SUP));
        players.add(new Player("SoloKiller", 2100, Role.MID, Role.TOP));
        players.add(new Player("AhriMain", 1650, Role.MID, Role.ADC));
        players.add(new Player("Yasuo0Deaths", 1200, Role.MID, Role.TOP));
        players.add(new Player("ControlMage", 2600, Role.MID, Role.SUP));
        players.add(new Player("ZedSmurf", 2950, Role.MID, Role.JUNGLE));
        players.add(new Player("CasualMid", 850, Role.MID, Role.ADC));

        // ===== TOP MAINS (7) =====
        players.add(new Player("IslandKing", 2700, Role.TOP, Role.JUNGLE));
        players.add(new Player("TankPlayer", 2200, Role.TOP, Role.SUP));
        players.add(new Player("SplitPusher", 1800, Role.TOP, Role.MID));
        players.add(new Player("GarenOTP", 1400, Role.TOP, Role.MID));
        players.add(new Player("WeakSide", 2450, Role.TOP, Role.ADC));
        players.add(new Player("Hashinshin", 2050, Role.TOP, Role.JUNGLE));
        players.add(new Player("CheeseTop", 950, Role.TOP, Role.SUP));

        // ===== ADC MAINS (6) =====
        players.add(new Player("KiteGod", 2550, Role.ADC, Role.MID));
        players.add(new Player("CSPerfect", 2300, Role.ADC, Role.TOP));
        players.add(new Player("JinxLover", 1750, Role.ADC, Role.SUP));
        players.add(new Player("SafePlayer", 2000, Role.ADC, Role.MID));
        players.add(new Player("ADC_Diff", 1500, Role.ADC, Role.JUNGLE));
        players.add(new Player("Rat_IRL", 2800, Role.ADC, Role.MID));

        // ===== JUNGLE MAINS (5) =====
        players.add(new Player("GankMachine", 2650, Role.JUNGLE, Role.TOP));
        players.add(new Player("FarmJungle", 1900, Role.JUNGLE, Role.MID));
        players.add(new Player("LeeSinGod", 2150, Role.JUNGLE, Role.MID));
        players.add(new Player("ObjectiveBot", 1100, Role.JUNGLE, Role.SUP));
        players.add(new Player("Invader", 2350, Role.JUNGLE, Role.TOP));

        // ===== SUPPORT MAINS (3) - наименее популярная =====
        players.add(new Player("WardBot", 2500, Role.SUP, Role.ADC));
        players.add(new Player("Enchanter", 850, Role.SUP, Role.MID));
        players.add(new Player("HookCity", 2250, Role.SUP, Role.TOP));

        return players;
    }

    @Test
    @DisplayName("Dataset has 30 players")
    void datasetSize() {
        assertEquals(30, testPlayers.size());
    }

    @Test
    @DisplayName("Role distribution: MID(9) > TOP(7) > ADC(6) > JG(5) > SUP(3)")
    void roleDistribution() {
        long midCount = testPlayers.stream().filter(p -> p.primaryRole() == Role.MID).count();
        long topCount = testPlayers.stream().filter(p -> p.primaryRole() == Role.TOP).count();
        long adcCount = testPlayers.stream().filter(p -> p.primaryRole() == Role.ADC).count();
        long jgCount = testPlayers.stream().filter(p -> p.primaryRole() == Role.JUNGLE).count();
        long supCount = testPlayers.stream().filter(p -> p.primaryRole() == Role.SUP).count();

        System.out.println("=== Role Distribution ===");
        System.out.printf("MID: %d | TOP: %d | ADC: %d | JG: %d | SUP: %d%n",
                midCount, topCount, adcCount, jgCount, supCount);

        assertEquals(9, midCount);
        assertEquals(7, topCount);
        assertEquals(6, adcCount);
        assertEquals(5, jgCount);
        assertEquals(3, supCount);
    }

    @Test
    @DisplayName("MMR spread: 850-2950")
    void mmrRange() {
        int minMmr = testPlayers.stream().mapToInt(Player::mmr).min().orElse(0);
        int maxMmr = testPlayers.stream().mapToInt(Player::mmr).max().orElse(0);
        double avgMmr = testPlayers.stream().mapToInt(Player::mmr).average().orElse(0);

        System.out.println("=== MMR Stats ===");
        System.out.printf("Min: %d | Max: %d | Avg: %.0f%n", minMmr, maxMmr, avgMmr);

        assertEquals(850, minMmr);
        assertEquals(2950, maxMmr);
    }

    @Test
    @DisplayName("Can create match with 30 players")
    void canCreateMatch() {
        testPlayers.forEach(matchMaking::enqueue);

        var result = matchMaking.tryCreateMatch();

        assertTrue(result.isPresent(), "Should create match with 30 players in queue");

        if (result.isPresent()) {
            printMatch(result.get());
        }
    }

    @Test
    @DisplayName("Cannot create match with <10 players")
    void notEnoughPlayers() {
        testPlayers.stream().limit(8).forEach(matchMaking::enqueue);

        var result = matchMaking.tryCreateMatch();

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Match MMR balance should be reasonable")
    void matchBalance() {
        testPlayers.forEach(matchMaking::enqueue);

        var result = matchMaking.tryCreateMatch();
        assertTrue(result.isPresent());

        Match match = result.get();
        System.out.printf("Team1: %d | Team2: %d | Diff: %d%n",
                match.team1().avgMmr(),
                match.team2().avgMmr(),
                match.mmrDifference());

        // TODO: включить когда реализуешь балансировку
        // assertTrue(match.mmrDifference() <= 100, "Diff should be <=100");
    }

    @Test
    @DisplayName("Each team has all 5 roles")
    void allRolesFilled() {
        testPlayers.forEach(matchMaking::enqueue);

        var result = matchMaking.tryCreateMatch();
        assertTrue(result.isPresent());

        Match match = result.get();

        assertEquals(5, match.team1().roster().size());
        assertEquals(5, match.team2().roster().size());

        for (Role role : Role.values()) {
            assertTrue(match.team1().roster().containsKey(role), "Team1 missing " + role);
            assertTrue(match.team2().roster().containsKey(role), "Team2 missing " + role);
        }
    }

    @Test
    @DisplayName("SUP shortage forces secondary/autofill")
    void supportShortage() {
        // Только 3 SUP мейна на 30 человек
        // При создании матча нужно 2 SUP
        // Значит 1 из них уйдёт на secondary/autofill в следующем матче

        testPlayers.forEach(matchMaking::enqueue);

        var match1 = matchMaking.tryCreateMatch();
        assertTrue(match1.isPresent());

        var match2 = matchMaking.tryCreateMatch();
        assertTrue(match2.isPresent());

        // Проверяем сколько игроков получили SUP на primary
        long supPrimary1 = countAssignmentType(match1.get(), Role.SUP, AssignmentType.PRIMARY);
        long supPrimary2 = countAssignmentType(match2.get(), Role.SUP, AssignmentType.PRIMARY);

        System.out.printf("SUP primary assignments: Match1=%d, Match2=%d%n", supPrimary1, supPrimary2);

        // Всего 3 SUP мейна, нужно 4 (по 2 на матч)
        // Значит минимум 1 должен быть SECONDARY или AUTOFILL
        // TODO: включить когда реализуешь secondary/autofill
         assertTrue(supPrimary1 + supPrimary2 <= 3);
    }

    // ===== Helpers =====

    void printMatch(Match match) {
        System.out.println("\n=== MATCH CREATED ===");
        System.out.println("Team 1 (avg " + match.team1().avgMmr() + "):");
        match.team1().roster().forEach((role, pa) ->
                System.out.printf("  %-7s: %-15s MMR:%4d [%s]%n",
                        role, pa.player().id(), pa.player().mmr(), pa.type()));

        System.out.println("Team 2 (avg " + match.team2().avgMmr() + "):");
        match.team2().roster().forEach((role, pa) ->
                System.out.printf("  %-7s: %-15s MMR:%4d [%s]%n",
                        role, pa.player().id(), pa.player().mmr(), pa.type()));

        System.out.println("MMR Difference: " + match.mmrDifference());
    }

    long countAssignmentType(Match match, Role role, AssignmentType type) {
        long count = 0;
        var pa1 = match.team1().roster().get(role);
        var pa2 = match.team2().roster().get(role);

        if (pa1 != null && pa1.type() == type) count++;
        if (pa2 != null && pa2.type() == type) count++;

        return count;
    }
}
