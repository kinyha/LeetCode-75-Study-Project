package exercise.yandex.dev.tasks.matchmaking;

import java.util.Map;

public record Team(
        Map<Role, PlayerAssignment> roster
) {
    int avgMmr() {
        return (int) roster.values()
                .stream()
                .map(PlayerAssignment::getEffectiveMmr)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);
    }

}
