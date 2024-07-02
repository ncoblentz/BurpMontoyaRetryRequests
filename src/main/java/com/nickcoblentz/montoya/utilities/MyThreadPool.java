package com.nickcoblentz.montoya.utilities;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyThreadPool {
    private ExecutorService executorService;
    public static MyThreadPool myThreadPool;

    private MyThreadPool()
    {
        //this.executorService = Executors.newFixedThreadPool(3);
        this.executorService = Executors.newVirtualThreadPerTaskExecutor();
    }

    public void addRunnable(Runnable runnable) {
        this.executorService.submit(runnable);
    }

    public static MyThreadPool getInstance() {
        if (MyThreadPool.myThreadPool == null) {
            MyThreadPool.myThreadPool = new MyThreadPool();
        }
        return MyThreadPool.myThreadPool;
    }
}
