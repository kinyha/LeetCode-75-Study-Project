package exercise.yandex.dev.tasks.old.deliveryCalculator;

import java.util.List;

public class PackageRepositoryImpl implements PackageRepository {

    @Override
    public List<exercise.yandex.dev.tasks.old.deliveryCalculator.Package> getPackages() {
        return List.of(
                new exercise.yandex.dev.tasks.old.deliveryCalculator.Package("Bike", new Size(160,80, 30), false),
                new Package("Phone", new Size(100, 20, 10), true)
        );
    }
}
