package exercise.yandex.dev.tasks.old.matchmaking;


public class Main {
    static void main() {
        System.out.println("qwe");


        // T1 (Korea)
        Player zeus = new Player("Zeus", 2920, Role.TOP, Role.MID);
        Player oner = new Player("Oner", 2900, Role.JUNGLE, Role.TOP);
        Player faker = new Player("Faker", 2980, Role.MID, Role.TOP);
        Player gumayusi = new Player("Gumayusi", 2910, Role.ADC, Role.MID);
        Player keria = new Player("Keria", 2940, Role.SUP, Role.ADC);

        // G2 (Europe)
        Player brokenBlade = new Player("BrokenBlade", 2880, Role.TOP, Role.MID);
        Player yike = new Player("Yike", 2850, Role.JUNGLE, Role.SUP);
        Player caps = new Player("Caps", 2950, Role.MID, Role.ADC);
        Player hansSama = new Player("HansSama", 2870, Role.ADC, Role.MID);
        Player mikyx = new Player("Mikyx", 2890, Role.SUP, Role.TOP);

        MatchMaking matchMaking = new MatchMaking();
        matchMaking.enqueue(zeus);
        matchMaking.enqueue(oner);
        matchMaking.enqueue(faker);
        matchMaking.enqueue(gumayusi);
        matchMaking.enqueue(keria);

        matchMaking.enqueue(brokenBlade);
        matchMaking.enqueue(yike);
        matchMaking.enqueue(caps);
        matchMaking.enqueue(hansSama);
        matchMaking.enqueue(mikyx);

        var a = matchMaking.tryCreateMatch();
        System.out.println(a);



//        Map<Role, PlayerAssignment> rosterT1 = Map.of(
//                Role.TOP, new PlayerAssignment(zeus, Role.TOP, AssignmentType.PRIMARY),
//                Role.JUNGLE, new PlayerAssignment(oner, Role.JUNGLE, AssignmentType.PRIMARY),
//                Role.MID, new PlayerAssignment(faker, Role.MID, AssignmentType.PRIMARY),
//                Role.ADC, new PlayerAssignment(gumayusi, Role.ADC, AssignmentType.PRIMARY),
//                Role.SUP, new PlayerAssignment(keria, Role.SUP, AssignmentType.PRIMARY)
//        );
//        Map<Role, PlayerAssignment> rosterG2 = Map.of(
//                Role.TOP, new PlayerAssignment(brokenBlade, Role.TOP, AssignmentType.PRIMARY),
//                Role.JUNGLE, new PlayerAssignment(yike, Role.JUNGLE, AssignmentType.PRIMARY),
//                Role.MID, new PlayerAssignment(caps, Role.MID, AssignmentType.PRIMARY),
//                Role.ADC, new PlayerAssignment(hansSama, Role.ADC, AssignmentType.PRIMARY),
//                Role.SUP, new PlayerAssignment(mikyx, Role.SUP, AssignmentType.PRIMARY)
//        );
//        Team team1 = new Team(rosterT1);
//        Team team2 = new Team(rosterG2);
//
//        System.out.println("T1 avg mmr - " +  team1.avgMmr());
//        Match match = Match.of(team1,team2);
//        System.out.println(match.mmrDifference());

    }
}
