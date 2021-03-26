package com.google.common.eventbus;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.j2objc.annotations.Weak;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Executor;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A subscriber method on a specific object, plus the executor that should be used for dispatching
 * events to it.
 *
 * <p>Two subscribers are equivalent when they refer to the same method on the same object (not
 * class). This property is used to ensure that no subscriber method is registered more than once.
 *
 * @author Colin Decker
 */
@SuppressWarnings("all")
class Subscriber {

    /**
     * Creates a {@code Subscriber} for {@code method} on {@code listener}.
     */
    static Subscriber create(EventBus bus, Object listener, Method method) {
        return isDeclaredThreadSafe(method)
                ? new Subscriber(bus, listener, method)//共享对象
                : new SynchronizedSubscriber(bus, listener, method);//同步对象
    }

    /**
     * The event bus this subscriber belongs to.
     * 当前事件总线
     */
    @Weak
    private EventBus bus;

    /**
     * The object with the subscriber method.
     * 该对象为{@link Subscribe}方法所在的对象
     *
     * @see Subscribe
     */
    @VisibleForTesting
    final Object target;

    /**
     * Subscriber method.
     * 使用{@link Subscribe}注解标注的方法
     * 该方法属于对象{@link Subscriber#target}
     * 而方法的参数其实就是当前订阅者感兴趣的事件
     * 这样的话，就可以通过反射执行了!!!!!!
     */
    private final Method method;

    /**
     * Executor to use for dispatching events to this subscriber.
     */
    private final Executor executor;

    private Subscriber(EventBus bus, Object target, Method method) {
        this.bus = bus;
        this.target = checkNotNull(target);
        this.method = method;
        method.setAccessible(true);

        this.executor = bus.executor();
    }

    /**
     * Dispatches {@code event} to this subscriber using the proper executor.
     * -- 使用适当的执行程序将{@code event}分发给此订阅者。
     */
    final void dispatchEvent(final Object event) {
        executor.execute(
                /**
                 * 实例化一个 Runnable 对象交给 executor
                 * 但是不一定是在新线程中执行
                 * @see com.google.common.util.concurrent.MoreExecutors
                 * @see MoreExecutors#directExecutor() 这就是在调用线程执行
                 */
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            /**
                             * 通过反射的反射去执行目标方法
                             */
                            invokeSubscriberMethod(event);
                        } catch (InvocationTargetException e) {
                            /**
                             * 当前订阅者方法有bug，异常交给事件总线中定义的异常处理器处理
                             * @see EventBus#exceptionHandler 异常处理器
                             */
                            bus.handleSubscriberException(e.getCause(), context(event));
                        }
                    }
                });
    }

    /**
     * Invokes the subscriber method. This method can be overridden to make the invocation synchronized.
     * -- 调用订阅者方法。可以重写此方法以使调用同步。
     *
     * @see SynchronizedSubscriber#invokeSubscriberMethod(java.lang.Object) 同步调用
     */
    @VisibleForTesting
    void invokeSubscriberMethod(Object event) throws InvocationTargetException {
        try {
            method.invoke(target, checkNotNull(event));
        } catch (IllegalArgumentException e) {
            throw new Error("Method rejected target/argument: " + event, e);
        } catch (IllegalAccessException e) {
            throw new Error("Method became inaccessible: " + event, e);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof Error) {
                throw (Error) e.getCause();
            }
            throw e;
        }
    }

    /**
     * Gets the context for the given event.
     */
    private SubscriberExceptionContext context(Object event) {
        return new SubscriberExceptionContext(bus, event, target, method);
    }

    @Override
    public final int hashCode() {
        return (31 + method.hashCode()) * 31 + System.identityHashCode(target);
    }

    @Override
    public final boolean equals(@Nullable Object obj) {
        if (obj instanceof Subscriber) {
            Subscriber that = (Subscriber) obj;
            // Use == so that different equal instances will still receive events.
            // We only guard against the case that the same object is registered
            // multiple times
            return target == that.target && method.equals(that.method);
        }
        return false;
    }

    /**
     * Checks whether {@code method} is thread-safe, as indicated by the presence of the {@link
     * AllowConcurrentEvents} annotation.
     */
    private static boolean isDeclaredThreadSafe(Method method) {
        return method.getAnnotation(AllowConcurrentEvents.class) != null;
    }

    /**
     * Subscriber that synchronizes invocations of a method to ensure that only one thread may enter
     * the method at a time.
     */
    @VisibleForTesting
    static final class SynchronizedSubscriber extends Subscriber {

        private SynchronizedSubscriber(EventBus bus, Object target, Method method) {
            super(bus, target, method);
        }

        @Override
        void invokeSubscriberMethod(Object event) throws InvocationTargetException {
            synchronized (this) {
                super.invokeSubscriberMethod(event);
            }
        }
    }
}