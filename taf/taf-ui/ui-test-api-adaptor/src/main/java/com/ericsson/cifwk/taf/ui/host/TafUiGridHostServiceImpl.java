package com.ericsson.cifwk.taf.ui.host;

import com.ericsson.cifwk.meta.API;
import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.HostType;
import com.ericsson.cifwk.taf.data.Ports;
import com.ericsson.cifwk.taf.ui.spi.UiGridPropertyService;
import com.google.common.base.Optional;

import static com.ericsson.cifwk.meta.API.Quality.Internal;

@API(Internal)
public class TafUiGridHostServiceImpl extends UiGridPropertyService {

    @Override
    public String getGridHost() {
        Optional<Host> host = getHost();
        if (host.isPresent()) {
            return host.get().getIp();
        }
        return super.getGridHost();
    }

    @Override
    public Integer getGridPort() {
        Optional<Host> host = getHost();
        if (host.isPresent()) {
            return host.get().getPort(Ports.HTTP);
        }
        return super.getGridPort();
    }

    @Override
    public boolean isGridDefined() {
        return getHost().isPresent();
    }

    private static Optional<Host> getHost() {
        return Optional.fromNullable(DataHandler.getHostByType(HostType.SELENIUM_GRID));
    }
}
