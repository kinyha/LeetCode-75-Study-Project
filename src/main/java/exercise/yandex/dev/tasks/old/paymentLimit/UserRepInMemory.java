package exercise.yandex.dev.tasks.old.paymentLimit;

import java.util.List;

public class UserRepInMemory implements UserRepository{
    @Override
    public List<User> getUsers() {
        return List.of();
    }
}
