package exercise.codex.ex05_message_broker.src.broker;

public interface Subscriber {
    void onMessage(Message m) throws Exception;
}

