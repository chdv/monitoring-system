package com.dch.app.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by ִלטענטי on 23.06.2015.
 */
public class Client {

    private Logger logger = LoggerFactory.getLogger(Client.class);

    private ScheduledExecutorService scheduler;
    private Server server;
    private ScheduledFuture future;
    private ClientWorker worker = new ClientWorker();

    public Client(Server server) {
        scheduler = Executors.newScheduledThreadPool(20);
        this.server = server;

        setWorkerDelay(Integer.valueOf(JConfig.INSTANCE.getValue("client-delay")));

        JConfig.INSTANCE.addConfigChangeListener("client-delay", new ConfigChangeListener() {
            @Override
            public void fieldChange(String newValue) {
                long v = Long.valueOf(newValue);
                future.cancel(true);
                setWorkerDelay(Long.valueOf(newValue));
                logger.debug("client delay updated");
            }
        });
    }

    private void setWorkerDelay(long t) {
        future = scheduler.scheduleAtFixedRate(
                worker,
                0,
                t,
                TimeUnit.MICROSECONDS);
    }

    private class ClientWorker implements Runnable {

        @Override
        public void run() {
            server.processTask(new Task());
        }
    }
}
