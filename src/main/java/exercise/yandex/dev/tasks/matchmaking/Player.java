package exercise.yandex.dev.tasks.matchmaking;

public record Player(
        String id,
        int mmr,
        Role primaryRole,
        Role secondRole
) {
}
