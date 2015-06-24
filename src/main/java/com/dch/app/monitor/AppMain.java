package com.dch.app.monitor;

import com.google.code.jconfig.ConfigurationManager;
import com.google.code.jconfig.listener.IConfigurationChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by ִלטענטי on 23.06.2015.
 */
public class AppMain {

    private static final int LOG_LIST_SIZE = 40;

    private Logger logger = LoggerFactory.getLogger(AppMain.class);

    private ScheduledExecutorService scheduler;

    private ScheduledFuture future;

    private Server server;

    private Client client;

    private List<Map<String, String>> logList = new LinkedList<>();

    public AppMain() {
        Map<String, IConfigurationChangeListener> listeners = new HashMap<>();
        listeners.put("general", JConfig.INSTANCE);
        ConfigurationManager.configureAndWatch(listeners, "config/config.xml", 200L);

        server = new Server();

        scheduler = Executors.newScheduledThreadPool(1);
        setStatWorkerDelay(Long.valueOf(JConfig.INSTANCE.getValue("stat-delay")));

        JConfig.INSTANCE.addConfigChangeListener("stat-delay", new ConfigChangeListener() {
            @Override
            public void fieldChange(String newValue) {
                long v = Long.valueOf(newValue);
                future.cancel(true);
                setStatWorkerDelay(Long.valueOf(newValue));
                logger.debug("Scheduler delay updated");
            }
        });

        client = new Client(server);

    }

    private void setStatWorkerDelay(long milisec) {
        future = scheduler.scheduleAtFixedRate(
                new StatWorker(),
                0,
                milisec,
                TimeUnit.MILLISECONDS);
    }

    private class StatWorker implements Runnable {

        private DateFormat format = new SimpleDateFormat("HH:mm:ss");

        @Override
        public void run() {
            Map<String, String> map = new TreeMap<>(stringComparator);
            map.put("_time", format.format(new Date()));
            map.putAll(server.getInfoMap());
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            map.put("system: heap usage", String.valueOf(memoryBean.getHeapMemoryUsage().getUsed()/(1024 * 1024)) );
            map.put("system: non heap usage", String.valueOf(memoryBean.getNonHeapMemoryUsage().getUsed()/(1024 * 1024)) );
            ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
            map.put("system: total thread count", String.valueOf(threadBean.getThreadCount()));

            logger.info("{}", map);
            addLog(map);
        }
    }

    private void addLog(Map<String, String> map) {
        logList.add(map);
        if(logList.size() == LOG_LIST_SIZE) {
            logList.remove(0);
        }
    }

    private Comparator<String> stringComparator = new StringComparator();

    public List<Map<String, String>> getLogList() {
        return logList;
    }

    public static void main(String[] args) throws IOException {
        AppMain main = new AppMain();
        HttpLogServer server = new HttpLogServer(main);
        server.start();

        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}
