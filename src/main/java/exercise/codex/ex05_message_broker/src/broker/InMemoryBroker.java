package exercise.codex.ex05_message_broker.src.broker;

import exercise.codex.ex05_message_broker.src.broker.Broker;

import java.util.HashMap;
import java.util.Map;

// Каркас простого брокера: одна нить-диспетчер, очереди на темы, доставка всем подписчикам (fan-out)
// TODO: Реализуйте многопоточную логику:
// - createTopic: регистрирует тему
// - publish: кладёт сообщение в очередь темы и будит диспетчер
// - subscribe: добавляет подписчика в список темы
// - dispatcher-поток: в цикле ждёт, пока нет сообщений; затем снимает и рассылает всем подписчикам
// Без гарантий persistency; допускается at-most-once.
public class InMemoryBroker implements Broker {
    private final Map<String, Topic> topics = new HashMap<>();
    private final Thread dispatcher;
    private volatile boolean shutdown = false;

    public InMemoryBroker() {
        // TODO: создать и запустить поток-диспетчер
        dispatcher = null;
    }

    @Override
    public void createTopic(String name) {
        // TODO: synchronized(this) { topics.putIfAbsent(name, new Topic(name)); }
        throw new UnsupportedOperationException("Implement createTopic");
    }

    @Override
    public void publish(Message message) {
        // TODO: synchronized(this) { найти topic; добавить в queue; notifyAll(); }
        throw new UnsupportedOperationException("Implement publish");
    }

    @Override
    public Subscription subscribe(String topic, Subscriber subscriber) {
        // TODO: synchronized(this) { добавить подписчика; вернуть Subscription, снимающий подписчика }
        throw new UnsupportedOperationException("Implement subscribe");
    }

    @Override
    public void close() {
        // TODO: корректно остановить диспетчер (shutdown=true; notifyAll; join)
        throw new UnsupportedOperationException("Implement close");
    }
}

