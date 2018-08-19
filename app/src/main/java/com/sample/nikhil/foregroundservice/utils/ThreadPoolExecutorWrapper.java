package com.sample.nikhil.foregroundservice.utils;

import android.support.annotation.NonNull;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Nikhil on 18-08-2018.
 */

public class ThreadPoolExecutorWrapper {

    private static final String TAG = "ThreadPoolExecutorWrapper";
    public static final int FILE_LIST_THREAD_POOL_EXECUTOR = 100;
    public static final int MP3FILE_LIST_THREAD_POOL_EXECUTOR = 101;
    private static ThreadPoolExecutor mMP3ThreadPoolExecutor;

    private static ThreadPoolExecutorWrapper mThreadPoolExecutorWrapper;

    private ThreadPoolExecutorWrapper() {

    }

    public static ThreadPoolExecutorWrapper getInstance() {
        if (mThreadPoolExecutorWrapper == null) {
            synchronized (ThreadPoolExecutorWrapper.class) {
                if (mThreadPoolExecutorWrapper == null) {
                    mThreadPoolExecutorWrapper = new ThreadPoolExecutorWrapper();
                }
            }
        }
        return mThreadPoolExecutorWrapper;
    }

    public ThreadPoolExecutor getThreadPoolExecutor(int threadPoolExecutorType) {
        switch (threadPoolExecutorType) {
            case MP3FILE_LIST_THREAD_POOL_EXECUTOR:
                if (mMP3ThreadPoolExecutor != null) {
                    return mMP3ThreadPoolExecutor;
                }
                mMP3ThreadPoolExecutor = createThreadPoolExecutor(new FileThreadPool());
                return mMP3ThreadPoolExecutor;
        }
        return null;
    }

    private ThreadPoolExecutor createThreadPoolExecutor(ThreadPool threadPool) {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(threadPool.getCorePoolSize(),
                threadPool.getMaxWorkerThreads(), threadPool.getKeepAliveTime(), TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(), new ThreadFactoryImpl(threadPool.getThreadName(), threadPool.getThreadPriority()));
        return threadPoolExecutor;
    }

    private interface ThreadPool {
        int getMaxWorkerThreads();

        int getCorePoolSize();

        String getThreadName();

        int getThreadPriority();

        int getKeepAliveTime();

        boolean allowCoreThreadTimeout();
    }

    private static class FileThreadPool implements ThreadPool {

        private static final int MAX_WORKER_THREADS = 1;
        private static final int CORE_POOL_SIZE = 1;
        private static final String THREAD_NAME = "FileThreadPool";
        private static final int THREAD_PRIORITY = Thread.NORM_PRIORITY;
        private static final int KEEP_ALIVE_TIME = 5;
        private static final boolean ALLOW_CORE_THREAD_TIMEOUT = true;


        @Override
        public int getMaxWorkerThreads() {
            return MAX_WORKER_THREADS;
        }

        @Override
        public int getCorePoolSize() {
            return CORE_POOL_SIZE;
        }

        @Override
        public String getThreadName() {
            return THREAD_NAME;
        }

        @Override
        public int getThreadPriority() {
            return THREAD_PRIORITY;
        }

        @Override
        public int getKeepAliveTime() {
            return KEEP_ALIVE_TIME;
        }

        @Override
        public boolean allowCoreThreadTimeout() {
            return ALLOW_CORE_THREAD_TIMEOUT;
        }
    }

    private final class ThreadFactoryImpl implements ThreadFactory {

        String threadName;
        int threadPriority;

        public ThreadFactoryImpl(String threadName, int threadPriority) {
            this.threadName = threadName;
            this.threadPriority = threadPriority;
        }

        @Override
        public Thread newThread(@NonNull Runnable runnable) {
            Thread thread = new Thread(runnable);
            return thread;
        }
    }
}