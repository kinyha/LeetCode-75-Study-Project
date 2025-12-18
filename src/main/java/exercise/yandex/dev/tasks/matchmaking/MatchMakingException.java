package exercise.yandex.dev.tasks.matchmaking;

public class MatchMakingException extends RuntimeException {
    public MatchMakingException(String cantCrateMatch) {
        super(cantCrateMatch);
    }
}
