package exercise.yandex.dev.OneNew;


public record User(String id, UserLimits limits) {
    public User {
        if(id == null) {
            throw new RuntimeException("User should have id");
        }
        if (limits == null) {
            throw new RuntimeException("User should have limits");
        }
    }
}
