package com.triplify.ui.shared.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public final class UiBackgroundExecutor {

    private static final int THREAD_COUNT = Math.max(2, Math.min(8, Runtime.getRuntime().availableProcessors()));
    private static final AtomicInteger THREAD_INDEX = new AtomicInteger(1);
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(
            THREAD_COUNT,
            daemonThreadFactory("triplify-ui-bg-")
    );

    private UiBackgroundExecutor() {
    }

    public static ExecutorService get() {
        return EXECUTOR;
    }

    private static ThreadFactory daemonThreadFactory(String prefix) {
        return runnable -> {
            Thread thread = new Thread(runnable, prefix + THREAD_INDEX.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        };
    }
}
