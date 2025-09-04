package exercise.codex.ex05_message_broker.src.broker;

public record Message(String topic, String key, String payload, long timestamp) {
    public static Message of(String topic, String payload) {
        return new Message(topic, null, payload, System.currentTimeMillis());
    }
}

