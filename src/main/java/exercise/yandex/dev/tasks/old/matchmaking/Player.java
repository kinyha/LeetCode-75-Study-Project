package exercise.yandex.dev.tasks.old.matchmaking;

public record Player(
        String id,
        int mmr,
        Role primaryRole,
        Role secondRole
) {
}
