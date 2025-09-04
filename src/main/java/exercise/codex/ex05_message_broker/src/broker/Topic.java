package exercise.codex.ex05_message_broker.src.broker;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

// Внутренняя модель темы: очередь сообщений + подписчики
class Topic {
    final String name;
    final Deque<Message> queue = new ArrayDeque<>();
    final List<Subscriber> subscribers = new ArrayList<>();
    Topic(String name) { this.name = name; }
}

