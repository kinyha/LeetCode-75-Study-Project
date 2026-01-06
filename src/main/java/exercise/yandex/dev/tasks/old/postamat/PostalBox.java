package exercise.yandex.dev.tasks.old.postamat;

import java.util.Map;

public class PostalBox {
    private final UserNotificationApi notificationApi;
    private final PostamatMemory postamatMemory;

    public PostalBox(UserNotificationApi notificationApi, PostamatMemory postamatMemory) {
        this.notificationApi = notificationApi;
        this.postamatMemory = postamatMemory;
    }

    String putOrder(String orderNumber) {
        Postamat postamat = postamatMemory.postamats.stream().filter(postamat1 -> postamat1.id().equals("id1"))
                .findFirst().orElseThrow();
        Map<Integer, String> cells = postamat.cells();
        boolean flag = false;

        //while (flag) { /// ??
            for (Map.Entry<Integer, String> entry : postamat.cells().entrySet()) {
                if (entry.getValue() == null) {
                    cells.put(entry.getKey(), orderNumber);
                    flag = true;
                    break;
                }
            }
        //}
        if (flag) {
            notificationApi.sendNotification(orderNumber);
            openDoor();
            return orderNumber;
        }
        return "";
    }

    String takeOrder(String orderNumber) {
        Postamat postamat = postamatMemory.postamats.stream().filter(postamat1 -> postamat1.id().equals("id1"))
                .findFirst().orElseThrow();
        Map<Integer, String> cells = postamat.cells();
        String message = "";
        for(Map.Entry<Integer, String> entry : cells.entrySet()) {
            if (orderNumber.equals(entry.getValue())) {
                message = String.format("ваш заказ %s в ячейке %s",entry.getValue(),entry.getKey());
                openDoor();
            }
        }
        return message;
    }

    private void openDoor() {
        System.out.println("Door opened");
    }

    Postamat getPostomat(){
        return postamatMemory.postamats.stream().filter(postamat1 -> postamat1.id().equals("id1"))
                .findFirst().orElseThrow();
    }
}
