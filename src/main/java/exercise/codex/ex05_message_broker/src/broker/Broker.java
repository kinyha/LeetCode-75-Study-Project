package exercise.codex.ex05_message_broker.src.broker;

public interface Broker extends AutoCloseable {
    void createTopic(String name);
    void publish(Message message);
    Subscription subscribe(String topic, Subscriber subscriber);
    @Override void close();

    interface Subscription extends AutoCloseable {
        @Override void close();
    }
}

