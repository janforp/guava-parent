package com.google.common.eventbus;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Wraps an event that was posted, but which had no subscribers and thus could not be delivered.
 *
 * <p>Registering a DeadEvent subscriber is useful for debugging or logging, as it can detect
 * misconfigurations in a system's event distribution.
 *
 * @author Cliff Biffle
 * @since 10.0
 */
@Beta
public class DeadEvent {

    private final Object source;

    private final Object event;

    /**
     * Creates a new DeadEvent.
     *
     * @param source object broadcasting the DeadEvent (generally the {@link EventBus}).
     * @param event the event that could not be delivered.
     */
    public DeadEvent(Object source, Object event) {
        this.source = checkNotNull(source);
        this.event = checkNotNull(event);
    }

    /**
     * Returns the object that originated this event (<em>not</em> the object that originated the
     * wrapped event). This is generally an {@link EventBus}.
     *
     * @return the source of this event.
     */
    public Object getSource() {
        return source;
    }

    /**
     * Returns the wrapped, 'dead' event, which the system was unable to deliver to any registered
     * subscriber.
     *
     * @return the 'dead' event that could not be delivered.
     */
    public Object getEvent() {
        return event;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("source", source).add("event", event).toString();
    }
}
