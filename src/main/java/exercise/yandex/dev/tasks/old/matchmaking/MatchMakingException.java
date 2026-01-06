package exercise.yandex.dev.tasks.old.matchmaking;

public class MatchMakingException extends RuntimeException {
    public MatchMakingException(String cantCrateMatch) {
        super(cantCrateMatch);
    }
}
