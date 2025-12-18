package exercise.yandex.dev.tasks.matchmaking;

import java.util.UUID;

public record Match(
        String matchId,
        Team team1,
        Team team2,
        int mmrDifference
) {
    static Match of(Team t1, Team t2) {
        int dif = Math.abs(t1.avgMmr() - t2.avgMmr());
        return new Match(UUID.randomUUID().toString(), t1,t2,dif);
    }
}
