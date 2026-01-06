package exercise.yandex.dev.tasks.old.deliveryCalculator;

public class DileveryServices {

    DeliveryResult calculate(Package pkg, Tariff tariff, int distanceKm) {
        if (pkg == null) {
            throw new IllegalArgumentException("package cant be null");
        }
        if (tariff == null) {
            throw new IllegalArgumentException("tariff cant be null");
        }
        if (distanceKm <= 0) {
            throw new IllegalArgumentException("Wrong distance");
        }

        double result = 0;
        //100*100*100 = 1m3 = 10rub/1km / glas 1.5
        double glassModifier = pkg.isGlass() ? 2 : 1;
        double tatifForCube = 10;//1_000_000 = 10rub/km
        result =  ((double)pkg.size().getVolum() / 1_000_000) * tatifForCube * distanceKm * glassModifier * tariff.getKaff();

        return DeliveryResult.success((int) result);
    }
}
