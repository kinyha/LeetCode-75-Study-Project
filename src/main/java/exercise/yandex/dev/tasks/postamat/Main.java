package exercise.yandex.dev.tasks.postamat;

/*Яндекс

*
 * Постамат - автоматическая станция приёма/выдачи посылок.
 * В маркете формируются заказы, и хочется добавить возможность получения через постамат.
 * Запускаем MVP: небольшая аудитория пользователей, несколько постаматов в Москве.
 * При заказе пользователь сможет выбрать, что хочет получить заказ в постамате.
 *
 * В рамках задачи нужно реализовать код для MVP решения:
 * - курьер привозит заказ и пробует положить его в ячейку, указывая номер заказа. Постамат сам выбирает ячейку и возвращает в ответ. Она откроется вызывающим этот метод кодом.
 * - после того, как заказ положили в ячейку, пользователю отправляется СМС c кодом получения. Заказ будет ждать вечно
 * - в случае любых ошибок - курьер забирает заказ назад и попробует положить заказ в ячейку на следующий день (для MVP это ок)
 * - пользователь может получить заказ по коду выдачи из СМС. При вводе кода выдачи постамат должен вывести на экран текст "ваш заказ ХХХ в ячейке YYY", ячейка откроется сама.
 *
 * Ограничения:
 * - все ячейки одного размера, но их может быть разное количество, зависит от конкретного постамата
 * - один заказ - одна коробка, она влезает в ячейку
 * - ячейки каждого постамата пронумерованы
 * - каждый постамат сам хранит своё состояние
 *
 * Для отправки сообщения пользователю надо использовать клиент UserNotificationApi.


 ///Postamat(String id, Map<Integer(номер ячейки),String(заказ)>
 PostalBox -> Положить(String orderNumber)-> Отдать truy? открыть дверь -> sendSMS(String orderNumber) else false -> Положить через день
 PostalBox -> Забрать (StringOrderNumber) -> ваш заказ ХХХ в ячейке YYY открыть дверь


class PostalBox {
    private final UserNotificationApi notificationApi;

    // нужно реализовать методы хранения и выдачи заказа
}

*/

import java.util.Map;

/**
 * Синхронный клиент, вызывающий postalbox.notify.market.yandex.net
 * Реализацию интерфейса описывать не нужно.
 *//*
interface UserNotificationApi {
    // нужно описать метод(ы) для отправки сообщения с кодом выдачи
    // в ответ придёт код выдачи, который был отправлен пользователю
}
#yandex*/



public class Main {
    static void main() {
        Postamat postamat = new Postamat("ps1", Map.of());
        PostamatMemory postamatMemory = new PostamatMemory();


        PostalBox postalBox = new PostalBox(new UserNotificationApi() {
            @Override
            public void sendNotification(String orderNumber) {
                System.out.println("Notification");
            }
        }, postamatMemory);

        var a = postalBox.putOrder("order1");
        System.out.println(a);
        System.out.println(postalBox.getPostomat());
        var b = postalBox.takeOrder("order1");
        System.out.println(b);
    }
}
