package exercise.yandex.dev.tasks.old.matchmaking;

public record PlayerAssignment(
        Player player,
        Role assignedRole,
        AssignmentType type
) {


    int getEffectiveMmr() {
        if (player.primaryRole() == assignedRole) return player.mmr();
        if (player.secondRole() == assignedRole) return player.mmr() - 50;
        return player.mmr() - 100;  // autofill
    }
}
