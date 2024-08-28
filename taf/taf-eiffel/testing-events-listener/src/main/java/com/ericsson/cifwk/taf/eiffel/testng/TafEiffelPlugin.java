package com.ericsson.cifwk.taf.eiffel.testng;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.ServiceRegistry;
import com.ericsson.cifwk.taf.configuration.TafConfiguration;
import com.ericsson.cifwk.taf.configuration.TafConfigurationProvider;
import com.ericsson.cifwk.taf.eiffel.EiffelAdapter;
import com.ericsson.cifwk.taf.eventbus.TestEventBus;
import com.ericsson.cifwk.taf.spi.TafPlugin;
import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

@API(Internal)
public class TafEiffelPlugin implements TafPlugin {

    private static Logger LOGGER = LoggerFactory.getLogger(TafEiffelPlugin.class);

    private EiffelAdapter eiffelAdapter;

    public TafEiffelPlugin() {
    }

    @Override
    public void init() {
        String mbHost = getMessageBusHost();
        if (mbHost != null) {
            initEventSender(mbHost);
        } else {
            LOGGER.info("Eiffel configuration settings not defined - no Eiffel events will be sent during test execution");
        }
    }

    @VisibleForTesting
    String getMessageBusHost() {
        TafConfiguration tafConfiguration = getTafConfiguration();
        return tafConfiguration.getString(EiffelAdapter.MB_HOST);
    }

    @VisibleForTesting
    void initEventSender(String mbHost) {
        eiffelAdapter = new EiffelAdapter();
        LOGGER.info("Eiffel events will be sent to the message bus on {}", mbHost);

        TestEventBus testEventBus = ServiceRegistry.getTestEventBus();
        TafEiffelListener tafEiffelListener = new TafEiffelListener(eiffelAdapter);
        testEventBus.register(tafEiffelListener);
    }

    @Override
    public void shutdown() {
        if (eiffelAdapter != null) {
            try {
                LOGGER.info("Shutting down Eiffel adapter");
                eiffelAdapter.shutdown();
            } catch (Exception e) {
                LOGGER.error("Failed to shut down Eiffel adapter", e);
            }
        }
    }

    private static TafConfiguration getTafConfiguration() {
        return TafConfigurationProvider.provide();
    }

}
