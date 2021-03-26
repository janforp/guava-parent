package com.google.common.util.concurrent;

import com.google.common.annotations.GwtCompatible;

import java.util.concurrent.Executor;

/**
 * An {@link Executor} that runs each task in the thread that invokes {@link Executor#execute execute}.
 * -- 一个{@link Executor}，该线程在调用{@link Executor＃execute execute}的线程中运行每个任务。
 */
@GwtCompatible
enum DirectExecutor implements Executor {

    INSTANCE;

    @Override
    public void execute(Runnable command) {
        //直接运行，没有启动新线程
        command.run();
    }

    @Override
    public String toString() {
        return "MoreExecutors.directExecutor()";
    }
}
