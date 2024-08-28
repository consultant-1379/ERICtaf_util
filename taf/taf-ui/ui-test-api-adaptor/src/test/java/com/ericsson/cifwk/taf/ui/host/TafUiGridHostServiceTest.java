package com.ericsson.cifwk.taf.ui.host;

import com.ericsson.cifwk.taf.ui.spi.UiGridService;
import org.junit.Test;

import java.util.ServiceLoader;

import static org.assertj.core.api.Assertions.assertThat;

public class TafUiGridHostServiceTest {

    @Test
    public void singleGridHostServiceSpiProviderDetected() {
        ServiceLoader<UiGridService> gridServices = ServiceLoader.load(UiGridService.class);

        assertThat(gridServices)
            .as("{%s} SPI Grid services provider", TafUiGridHostServiceImpl.class.getSimpleName())
            .hasSize(1);
        assertThat(gridServices.iterator().next())
            .as("Instance of SPI provider")
            .isInstanceOf(TafUiGridHostServiceImpl.class);
    }
}
