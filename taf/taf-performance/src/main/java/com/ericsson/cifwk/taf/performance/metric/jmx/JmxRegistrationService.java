package com.ericsson.cifwk.taf.performance.metric.jmx;

import com.google.common.base.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JmxRegistrationService<T> {

    private static final Logger logger = LoggerFactory.getLogger(JmxRegistrationService.class);

    private final MBeanServer mBeanServer;
    private final Map<String, T> mBeans;

    public JmxRegistrationService() {
        mBeanServer = ManagementFactory.getPlatformMBeanServer();
        mBeans = new ConcurrentHashMap<>();
    }

    public synchronized T findOrRegister(String name, Supplier<T> mBeanSupplier) {
        T mBean = find(name);
        if (mBean == null) {
            mBean = mBeanSupplier.get();
            register(mBean, name);
        }
        return mBean;
    }

    public T find(String name) {
        return mBeans.get(name);
    }

    public void register(T mBean) {
        register(mBean, null);
    }

    public void register(T mBean, String name) {
        try {
            ObjectName objectName = getObjectName(name);
            ObjectInstance instance = mBeanServer.registerMBean(mBean, objectName);
            if (objectName == null) {
                objectName = instance.getObjectName();
            }
            mBeans.put(objectName.toString(), mBean);
        } catch (InstanceAlreadyExistsException ignored) {
            logger.warn("MBean instance already exists: " + ignored.getMessage());
        } catch (JMException e) {
            throw new RuntimeException(e);
        }
    }

    private static ObjectName getObjectName(String name) {
        if (name == null) {
            return null;
        }
        try {
            return new ObjectName(name);
        } catch (MalformedObjectNameException e) {
            return null;
        }
    }

    public void shutdown() {
        for (String mBeanName : mBeans.keySet()) {
            try {
                mBeanServer.unregisterMBean(new ObjectName(mBeanName));
            } catch (JMException ignored) {
            }
        }
    }

}
