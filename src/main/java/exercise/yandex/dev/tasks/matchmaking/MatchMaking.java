package exercise.yandex.dev.tasks.matchmaking;

import java.util.*;

public class MatchMaking {
    List<Player> queue = new ArrayList<>();

    void enqueue(Player player) {
        queue.add(player);
    }

    Optional<Match> tryCreateMatch() {
        if (queue.size() < 10) {
            return Optional.empty();
        }

        List<Player> sorted = new ArrayList<>(queue);
        sorted.sort(Comparator.comparing(Player::mmr));

        int mmrWindow = 12;   //in players

        List<Player> playersWindow = findWindow(mmrWindow);


        Map<String, Team> teams = Map.of(
                "team1",new Team(Map.of()),
                "team2",new Team(Map.of())
        );
                //findTeam(playersWindow);

        while (!isTeamsCompleted(teams)) {
            teams = findTeam(playersWindow);

            if (!isTeamsCompleted(teams)) {
                mmrWindow += 2;
                playersWindow = findWindow(mmrWindow);
                //infolog
                System.err.println("treCreatMatch: up window");

                if (mmrWindow > 30) {
                    throw new MatchMakingException("Cant crate match");
                }
            }

        }


        return Optional.of(Match.of(teams.get("team1"), teams.get("team2")));
    }

    private boolean isTeamsCompleted(Map<String, Team> teams) {
        return teams.get("team1").roster().size() == 5 && teams.get("team2").roster().size() == 5;
    }

    private List<Player> findWindow(int mmrWindow) {
        return queue.stream()
                .sorted(Comparator.comparing(Player::mmr).reversed())
                .limit(mmrWindow)
                .toList();
    }

    Map<String, Team> findTeam(List<Player> players) {
        List<Player> lobby = new ArrayList<>();
        Map<Role, PlayerAssignment> team1 = new HashMap<>();
        Map<Role, PlayerAssignment> team2 = new HashMap<>();

        //findPrime()
        List<Player> actualPlayers = new ArrayList<>(players);

        for (Role role : Role.values()) {
            for (Player player : players) {

                if (player.primaryRole().equals(role) && countRolesInList(team1,team2, role) < 2) {
                    lobby.add(player);
                    actualPlayers.remove(player);
                    if (countRolesInList(team1,team2, role) == 1) {
                        team1.put(role, new PlayerAssignment(player, role, AssignmentType.PRIMARY));
                    } else {
                        team2.put(role, new PlayerAssignment(player, role, AssignmentType.PRIMARY));
                    }
                }
            }
        }
        players = new ArrayList<>(actualPlayers);

        //PutFromSecondary
        if (lobby.size() < 10) {
            for (Role role : Role.values()) {
                for (Player player : players) {
                    if (player.secondRole().equals(role) && countRolesInList(team1,team2, role) < 2 && lobby.size() < 10) {
                        lobby.add(player);
                        actualPlayers.remove(player);
                        if (!team1.containsKey(role)) {
                            team1.put(role, new PlayerAssignment(player, role, AssignmentType.SECONDARY));
                        } else if (!team2.containsKey(role)) {
                            team2.put(role, new PlayerAssignment(player, role, AssignmentType.SECONDARY));
                        }

                    }

                }
            }
        }
        players = new ArrayList<>(actualPlayers);

        //autofill
        if (lobby.size() < 10) {
            putAutofill(team1, actualPlayers, lobby);

            putAutofill(team2, actualPlayers, lobby);
        }

        Team t1 = new Team(team1);
        System.out.println(t1.avgMmr());

        Team t2 = new Team(team2);
        System.out.println(t2.avgMmr());

        return Map.of(
                "team1", t1,
                "team2", t2);

    }

    private static void putAutofill(Map<Role, PlayerAssignment> team, List<Player> actualPlayers, List<Player> lobby) {
        if (team.size() == 4) {

            for (Role role : Role.values()) {
                if (!team.containsKey(role)) {
                    Player autofill = actualPlayers.stream().findFirst().orElseThrow();
                    lobby.add(autofill);
                    actualPlayers.remove(autofill);
                    team.put(role, new PlayerAssignment(autofill, role, AssignmentType.AUTOFILL));
                }
            }
        }
    }

    int countRolesInList(Map<Role, PlayerAssignment> team1, Map<Role, PlayerAssignment> team2, Role role) {
        int count = 0;
        if(team1.containsKey(role)) count++;
        if(team2.containsKey(role)) count++;
        return count;
    }

    void cancelQueue(Player player) {
        queue.remove(player);
    }

    int getEffectiveMmr(Player player, Role assignedRole) {
        if (player.primaryRole() == assignedRole) return player.mmr();
        if (player.secondRole() == assignedRole) return player.mmr() - 50;
        return player.mmr() - 100;  // autofill
    }
}
