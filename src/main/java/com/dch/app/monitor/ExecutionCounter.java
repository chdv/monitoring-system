package com.dch.app.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by ִלטענטי on 23.06.2015.
 */
public enum ExecutionCounter {

    INSTANCE;

    private final AtomicLong opCount = new AtomicLong();
    private volatile long lastTime = 0;
    private final ReentrantLock lock = new ReentrantLock();
    private final Logger logger = LoggerFactory.getLogger(ExecutionCounter.class);

    public void calcExecutionsCount() {
        if(lastTime == 0) {
            lastTime = System.currentTimeMillis();
        } else {
            long currTime = System.currentTimeMillis();
            if(currTime - lastTime >= 1000) {
                lock.lock();
                try {
                    if(currTime - lastTime >= 1000) {
                        lastTime = currTime;
//                        logger.debug("server count in sec: {}", opCount);
                        opCount.set(0);
                    }
                } finally {
                    lock.unlock();
                }
            }
        }

        opCount.getAndIncrement();
    }

    public long getExecutionsCount() {
        return opCount.get();
    }
}
