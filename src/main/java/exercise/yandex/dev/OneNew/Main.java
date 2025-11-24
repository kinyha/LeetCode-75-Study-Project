//package exercise.yandex.dev.OneNew;
//
///*
//Яндекс
//
//Вы — backend-разработчик в финтех компании.
//Компания предоставляет платежные услуги и должна контролировать лимиты пользователей.
//Product owner просит создать систему проверки лимитов перед проведением платежей.
//
//### Определения
//
//Платеж:
//- сумма (в рублях)
//- тип операции (только списание)
//- timestamp
//
//Лимиты пользователя:
//- дневной лимит по сумме
//- максимальный размер одной операции
//
//История операций:
//- список совершенных платежей пользователя
//
//### Задача
//Написать систему проверки лимитов, которая:
//- на вход получает платеж и id пользователя
//- проверяет все установленные лимиты
//- возвращает результат: можно ли провести операцию
//- если нельзя, то указывает причину (какой лимит превышен)
//
//### Ограничения
//- в рамках данной задачи считаем, что все платежи одного пользователя происходят строго последовательно.
//Во время проверки лимита не может быть проведен платеж того же пользователя.
//* */
//
///*Payment(userId,amount,timestamp)
//User(userId,List<Limits>)
////PaymentsCheckService
//
//Map<String, List<Payments> paymentsHistory
//Map<String, User> users //repository
//
//CheckResult(boolean access, String message)
//
//* */
//
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Map;
//
//public class Main {
//    public static void main(String[] args) {
//        UserLimits limits1 = new UserLimits(new BigDecimal(1000),new BigDecimal(300));
//        UserLimits limits2 = new UserLimits(new BigDecimal(1000),new BigDecimal(600));
//        User user1 = new User("user1", limits1);
//        User user2 = new User("user2", limits2);
//
//        LocalDateTime now = LocalDateTime.now();
//        Payment payment1 = new Payment("user1", new BigDecimal(100), now.minusMinutes(10)); //1/6
//        Payment payment2 = new Payment("user1", new BigDecimal(300), now.minusMinutes(500)); //1 4/6
//        Payment payment3 = new Payment("user1", new BigDecimal(400), now.minusMinutes(500));  // 60 * 12 = 720 800
//
//        Map<String, User> users = Map.of(user1.id(),user1,
//                user2.id(),user2);
//        Map<String, List<Payment>> paymentHistory = Map.of(
//                user1.id(),List.of(payment1,payment2,payment3),
//                user2.id(),List.of()
//        );
//
//        PaymentCheckService paymentCheckService = new PaymentCheckService(paymentHistory, users);
//
//        CheckResult  result = paymentCheckService.check("user1", new Payment("user1",new BigDecimal(200), LocalDateTime.now()));
//        System.out.println(result);
//
//
//
//
//
//
//    }
//}
