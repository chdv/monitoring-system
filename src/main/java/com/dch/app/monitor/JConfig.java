package com.dch.app.monitor;

import com.google.code.jconfig.listener.IConfigurationChangeListener;
import com.google.code.jconfig.model.BasicConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by ִלטענטי on 23.06.2015.
 */
public enum JConfig implements IConfigurationChangeListener {

    INSTANCE;

    private Logger logger = LoggerFactory.getLogger(JConfig.class);

    private BasicConfiguration configuration;
    // a simple and brutal lock avoiding concurrency between different thread consumers of this container properties
    private AtomicBoolean suspend = new AtomicBoolean(true);

    private Map<String, List<ConfigChangeListener>> listenersMap = new HashMap<>();

    private Map<String, String> configMap = new HashMap<>();

    private Executor listenerPoolExecutor = Executors.newCachedThreadPool();

    JConfig() {
    }

    public String getValue(String key) {
        while(suspend.get());
        String result = configuration.getProperty(key);
        configMap.put(key, result);
        return result;
    }

    public void addConfigChangeListener(String fieldName, ConfigChangeListener listener) {
        List<ConfigChangeListener> list = listenersMap.get(fieldName);
        if(list == null) {
            list = new ArrayList<>();
            list.add(listener);
            listenersMap.put(fieldName, list);
        } else {
            list.add(listener);
        }
    }

    private void fireFieldChanged(String fieldName, String newValue) {
        logger.debug("field {} change to value {}", fieldName, newValue);
        List<ConfigChangeListener> list = listenersMap.get(fieldName);
        if(list != null) {
            for(ConfigChangeListener listener : list) {
                Runnable e = () -> listener.fieldChange(newValue);
                listenerPoolExecutor.execute(e);
            }
        }
    }

    @Override
    public <T> void loadConfiguration(T configuration) {
        while (suspend.compareAndSet(false, true)) ;
        logger.debug("new configuration loading..");
        BasicConfiguration newConfiguration = (BasicConfiguration) configuration;
        this.configuration = newConfiguration;
        fireListeners();
        suspend.compareAndSet(true, false);
    }

    private void fireListeners() {
        Map<String, String> newValues = new HashMap<>();
        for (String key : configMap.keySet()) {
            String newValue = this.configuration.getProperty(key);
            if (!newValue.equals(configMap.get(key))) {
                newValues.put(key, newValue);
                configMap.put(key, newValue);
            }
        }

        for (Map.Entry<String, String> entry : newValues.entrySet()) {
            fireFieldChanged(entry.getKey(), entry.getValue());
        }
    }

}
