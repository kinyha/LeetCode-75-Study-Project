package exercise.yandex.dev.tasks.deliveryCalculator;

import java.util.List;

public class PackageRepositoryImpl implements PackageRepository {

    @Override
    public List<Package> getPackages() {
        return List.of(
                new Package("Bike", new Size(160,80, 30), false),
                new Package("Phone", new Size(100, 20, 10), true)
        );
    }
}
