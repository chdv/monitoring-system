package com.dch.app.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by ִלטענטי on 23.06.2015.
 */
public class Server {

    private Logger logger = LoggerFactory.getLogger(Server.class);

    private BlockingQueue<Runnable> inboundQueue = new LinkedBlockingQueue<>(100_000);

    private ThreadPoolExecutor service;

    private volatile boolean run = true;

    private RejectedExecutionHandler handler;

    private AtomicLong rejectedCount = new AtomicLong(0L);

    private BlockingQueue<Runnable> rejectQueue = new LinkedBlockingQueue<>();

    private Executor rejectedExecutor;

    public Server() {
//        service = (ThreadPoolExecutor) Executors.newFixedThreadPool(poolSize);
        int poolSize = Integer.valueOf(JConfig.INSTANCE.getValue("pool-size"));
        int maxSize = Integer.valueOf(JConfig.INSTANCE.getValue("max-pool-size"));
        handler = new RejectedExecutionHandlerImpl();
        service = new ThreadPoolExecutor(
                poolSize,
                maxSize,
                5L,
                TimeUnit.SECONDS,
                inboundQueue,
                Executors.defaultThreadFactory(),
                handler);

        rejectedExecutor = new ThreadPoolExecutor(
                10, 10, 0L, TimeUnit.MILLISECONDS,
                rejectQueue, Executors.defaultThreadFactory());

        JConfig.INSTANCE.addConfigChangeListener("pool-size", new ConfigChangeListener() {
            @Override
            public void fieldChange(String newValue) {
                int val = Integer.valueOf(newValue);
                service.setCorePoolSize(val);
                logger.debug("ThreadPoolExecutor updated");
            }
        });

        JConfig.INSTANCE.addConfigChangeListener("max-pool-size", new ConfigChangeListener() {
            @Override
            public void fieldChange(String newValue) {
                int val = Integer.valueOf(newValue);
                service.setMaximumPoolSize(val);
                logger.debug("ThreadPoolExecutor updated");
            }
        });
    }

    private class RejectedExecutionHandlerImpl implements RejectedExecutionHandler {

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            if(r instanceof TaskWorker) {
                rejectedExecutor.execute(((TaskWorker)r).getRejectRunnable());
                rejectedCount.incrementAndGet();
            }
        }
    }

    public void processTask(Task task) {
        service.execute(new TaskWorker(task));
    }

    public Map<String, String> getInfoMap() {
        Map<String, String> map = new HashMap<>();
        map.put("server: pool: size", String.valueOf(service.getPoolSize()));
        map.put("server: pool: active count", String.valueOf(service.getActiveCount()));
        map.put("server: pool: max size", String.valueOf(service.getMaximumPoolSize()));
        map.put("server: pool: queue size", String.valueOf(inboundQueue.size()));
        map.put("server: executions in sec", String.valueOf(ExecutionCounter.INSTANCE.getExecutionsCount()));
        map.put("server: reject: tasks count", String.valueOf(rejectedCount.get()));
        map.put("server: reject: queue size", String.valueOf(rejectQueue.size()));
        return map;
    }

    private class TaskWorker implements Runnable {

        private Task task;

        TaskWorker(Task task) {
            this.task = task;
        }
        @Override
        public void run() {
//            logger.debug("run task");
            ExecutionCounter.INSTANCE.calcExecutionsCount();
            task.toString();
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {
            }
            task.setStatus(Task.Status.SUCCESS);
        }

        public Runnable getRejectRunnable() {
            return new Runnable() {
                @Override
                public void run() {
                    runReject();
                }
            };
        }

        public void runReject() {
            try {
//                Do simple work
                Thread.sleep(10);
            } catch (InterruptedException ignored) {
            }
            task.setStatus(Task.Status.REJECTED);
        };
    }

    public void stop() {
        run = false;
    }
}
