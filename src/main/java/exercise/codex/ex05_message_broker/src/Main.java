package exercise.codex.ex05_message_broker.src;


public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("Каркас брокера: реализуйте InMemoryBroker");

        // После реализации можно проверить так:
        // try (Broker b = new InMemoryBroker()) {
        //     b.createTopic("news");
        //     var sub = b.subscribe("news", m -> System.out.printf("[%s] %s%n", m.topic(), m.payload()));
        //     b.publish(Message.of("news", "hello"));
        //     b.publish(Message.of("news", "world"));
        //     Thread.sleep(200);
        //     sub.close();
        // }
    }
}

