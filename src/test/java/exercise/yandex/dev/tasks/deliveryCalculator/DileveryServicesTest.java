package exercise.yandex.dev.tasks.deliveryCalculator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.assertArg;

class DileveryServicesTest {
    DileveryServices dileveryServices;

    @BeforeEach
    void setUp() {
        dileveryServices = new DileveryServices();

    }

    @Test
    @DisplayName("Happe")
    void calculate() {
        //given
        Package bike = new Package("Bike", new Size(160, 80, 30), false);
        Tariff tariff = Tariff.ECONOM;
        int distance = 600;
        //when
        var result = dileveryServices.calculate(bike,tariff,distance);

        //then
        System.out.println(result);
        assertEquals(2304, result.price());
    }
}