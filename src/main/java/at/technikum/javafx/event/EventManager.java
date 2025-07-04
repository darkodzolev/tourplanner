package at.technikum.javafx.event;

import java.util.*;

public class EventManager {
    private final Map<Events, List<EventListener>> eventListeners = new HashMap<>();

    public void subscribe(Events event, EventListener listener) {
        eventListeners
                .computeIfAbsent(event, e -> new ArrayList<>())
                .add(listener);
    }

    // old signature stays for String-based events
    public void publish(Events event, String message) {
        publishInternal(event, message);
    }

    // new overload for any payload
    public void publish(Events event, Object payload) {
        publishInternal(event, payload);
    }

    // shared logic
    private void publishInternal(Events event, Object payload) {
        for (EventListener listener : eventListeners.getOrDefault(event, List.of())) {
            listener.event(payload);
        }
    }
}