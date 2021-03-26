package com.google.common.eventbus;

import com.google.common.collect.Lists;
import junit.framework.Assert;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;

/**
 * A simple EventSubscriber mock that records Strings.
 * -- 一个简单的EventSubscriber模拟程序，它记录字符串。
 *
 * <p>For testing fun, also includes a landmine method that EventBus tests are required <em>not</em>
 * to call ({@link #methodWithoutAnnotation(String)}).
 *
 * @author Cliff Biffle
 */
public class StringCatcher {

    private List<String> events = Lists.newArrayList();

    @Subscribe
    public void hereHaveAString(@Nullable String string) {
        events.add(string);
    }

    public void methodWithoutAnnotation(@Nullable String string) {
        Assert.fail("Event bus must not call methods without @Subscribe!");
    }

    public List<String> getEvents() {
        return events;
    }
}