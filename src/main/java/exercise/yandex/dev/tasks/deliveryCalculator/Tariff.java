package exercise.yandex.dev.tasks.deliveryCalculator;

public enum Tariff {
    ECONOM(1), EXPRESS(2);

    private final int kaff;

    Tariff(int kaff) {
        this.kaff = kaff;
    }

    public int getKaff() {
        return kaff;
    }
}
