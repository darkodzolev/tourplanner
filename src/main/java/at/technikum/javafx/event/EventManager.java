package at.technikum.javafx.event;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class EventManager {

    private final ConcurrentHashMap<Events, CopyOnWriteArrayList<EventListener>> eventListeners = new ConcurrentHashMap<>();

    public void subscribe(Events event, EventListener listener) {
        eventListeners
                .computeIfAbsent(event, e -> new CopyOnWriteArrayList<>())
                .add(listener);
    }

    public void publish(Events event, String message) {
        publishInternal(event, message);
    }

    public void publish(Events event, Object payload) {
        publishInternal(event, payload);
    }

    private void publishInternal(Events event, Object payload) {
        var listeners = eventListeners.getOrDefault(event, new CopyOnWriteArrayList<>());
        for (EventListener listener : listeners) {
            listener.event(payload);
        }
    }
}