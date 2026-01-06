//package exercise.yandex.dev.tasks.old.Booking.deliveryCalculator;
//
//import exercise.yandex.dev.tasks.old.deliveryCalculator.DileveryServices;
//import exercise.yandex.dev.tasks.old.deliveryCalculator.Package;
//import exercise.yandex.dev.tasks.old.deliveryCalculator.Size;
//import exercise.yandex.dev.tasks.old.deliveryCalculator.Tariff;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//import static org.mockito.ArgumentMatchers.assertArg;
//
//class DileveryServicesTest {
//    DileveryServices dileveryServices;
//
//    @BeforeEach
//    void setUp() {
//        dileveryServices = new DileveryServices();
//
//    }
//
//    @Test
//    @DisplayName("Happe")
//    void calculate() {
//        //given
//        exercise.yandex.dev.tasks.old.deliveryCalculator.Package bike = new Package("Bike", new Size(160, 80, 30), false);
//        Tariff tariff = Tariff.ECONOM;
//        int distance = 600;
//        //when
//        var result = dileveryServices.calculate(bike,tariff,distance);
//
//        //then
//        System.out.println(result);
//        assertEquals(2304, result.price());
//    }
//}