package com.ericsson.cifwk.taf.handlers.netsim.domain;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.handlers.netsim.FDNParsingUtilities;
import com.ericsson.cifwk.taf.handlers.netsim.NetSimCommand;
import com.ericsson.cifwk.taf.handlers.netsim.NetSimContext;
import com.ericsson.cifwk.taf.handlers.netsim.NetSimException;
import com.ericsson.cifwk.taf.handlers.netsim.NetSimResult;
import com.ericsson.cifwk.taf.handlers.netsim.commands.CreatemoCommand;
import com.ericsson.cifwk.taf.handlers.netsim.commands.DeletemoCommand;
import com.ericsson.cifwk.taf.handlers.netsim.commands.DumpmotreeCommand;
import com.ericsson.cifwk.taf.handlers.netsim.commands.NetSimCommands;
import com.ericsson.cifwk.taf.handlers.netsim.commands.SetmoattributeCommand;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.attribute.SimpleAttribute;
import com.googlecode.cqengine.query.option.QueryOptions;


public class NetworkElementImpl extends AbstractCommandBatchExecutor implements NetworkElement {

    static final Attribute<NetworkElement, String> NE_NAME = new SimpleAttribute<NetworkElement, String>() {
        @Override
        public String getValue(NetworkElement networkElement, QueryOptions queryOptions) {
            return networkElement.getName();
        }
    };

    private static final Logger LOGGER = getLogger(NetworkElementImpl.class);
    private static final int DEFAULT_START_TIMEOUT = 30;

    private final NetSimContext context;
    private final Simulation simulation;

    private String name;
    private String type;
    private String techType;
    private String nodeType;
    private String mim;
    private String ip;
    private String notStarted = "NotStarted";

    public NetworkElementImpl(NetSimContext context, Simulation simulation) {
        this.context = context;
        this.simulation = simulation;
    }

    public String getName() {
        return name;
    }

    @Override
    public String getSimulationName() {
        return simulation.getName();
    }

    public String getHostName() {
        return context.getHostName();
    }

    public Host getHost() {
        return context.getHost();
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTechType() {
        return techType;
    }

    public void setTechType(String techType) {
        this.techType = techType;
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public String getMim() {
        return mim;
    }

    public void setMim(String mim) {
        this.mim = mim;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    @Override
    public boolean isStarted() {
        NetSimResult result = exec(NetSimCommands.isstarted());
        return !StringUtils.contains(result.getRawOutput(), notStarted);
    }

    @Override
    public NetSimResult exec(NetSimCommand... commands) {
        return runWithAdditionalCommands(commands, getAdditionalCommands(), context);
    }

    @Override
    public NetSimResult exec(List<NetSimCommand> commands) {
        return runWithAdditionalCommands(commands, getAdditionalCommands(), context);
    }

    @Override
    public boolean start() {
        runWithAdditionalCommands(new NetSimCommand[]{NetSimCommands.start()}, getAdditionalCommands(), context);
        return start(DEFAULT_START_TIMEOUT);
    }

    @Override
    public boolean start(final int timeout) {
        final long commandStartTime = System.currentTimeMillis();
        while (hasNotTimedOut(commandStartTime, TimeUnit.SECONDS.toMillis(timeout))) {
            LOGGER.info("Checking if NetworkElement {} has started...", this.name);
            if (this.isStarted()) {
                LOGGER.info("{} has been started", this.name);
                return true;
            }
            sleep();
        }
        throw new NetSimException(this.name + " has failed to start");
    }

    @VisibleForTesting
    static boolean hasNotTimedOut(final long startTimeInMilliseconds, final long timeout) {
        return (System.currentTimeMillis() - startTimeInMilliseconds) < timeout;
    }

    private static void sleep() {
        try {
            Thread.sleep(TimeUnit.SECONDS.toMillis(1));
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public boolean stop() {
        runWithAdditionalCommands(new NetSimCommand[]{NetSimCommands.stop()}, getAdditionalCommands(), context);
        return !isStarted();
    }

    @Override
    public boolean createManagedObject(final String parentFdn, final String moType, final String moName, final String attributesKeyValues,
            final int quantity) {
        CreatemoCommand createmoCommand = buildCreatemoCommand(parentFdn, moType, moName, attributesKeyValues, quantity);
        NetSimResult result = exec(createmoCommand);
        logDebugMessage("Raw output received from create MO execution: {}", result.getRawOutput());
        return result.getRawOutput().contains("OK");
    }

    @Override
    public boolean deleteManagedObject(final String fdn) {
        DeletemoCommand deletemoCommand = NetSimCommands.deletemo(FDNParsingUtilities.getLdn(fdn));
        NetSimResult result = exec(deletemoCommand);
        logDebugMessage("Raw output received from delete MO execution: {}", result.getRawOutput());
        return result.getRawOutput().contains("OK");
    }

    @Override
    public boolean setManagedObjectAttributes(final String fdn, final String attributeKeyValues) {
        SetmoattributeCommand setmoattributeCommand = NetSimCommands.setmoattribute(FDNParsingUtilities.getLdn(fdn), attributeKeyValues);
        NetSimResult result = exec(setmoattributeCommand);
        logDebugMessage("Raw output received from set MO execution: {}", result.getRawOutput());
        return result.getRawOutput().contains("OK");
    }

    @Override
    public NetSimResult getManagedObject(final String fdn, final boolean printAttributes, final int scope, final String... attributes) {
        DumpmotreeCommand dumpmotreeCommand = NetSimCommands.dumpmotree();
        dumpmotreeCommand.setMoid(FDNParsingUtilities.getLdn(fdn));
        dumpmotreeCommand.setScope(scope);
        dumpmotreeCommand.setPrintattrs(printAttributes);
        if(ArrayUtils.isNotEmpty(attributes)){
            dumpmotreeCommand.setIncludeattrs(attributes);
        }
        NetSimResult result = exec(dumpmotreeCommand);
        logDebugMessage("Raw output received from get MO execution: {}", result.getRawOutput());
        return result;
    }

    private void logDebugMessage(final String message, final String... messageArguments) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(message, messageArguments);
        }
    }

    @VisibleForTesting
    protected CreatemoCommand buildCreatemoCommand(final String parentFdn, final String moType, final String moName, final String attributesKeyValues,
            final int quantity) {
        String ldn = FDNParsingUtilities.getLdn(parentFdn);
        CreatemoCommand createmoCommand = NetSimCommands.createmo(ldn, moType, moName, quantity);
        if(!Strings.isNullOrEmpty(attributesKeyValues)) {
            createmoCommand.setAttributes(attributesKeyValues);
        }
        return createmoCommand;
    }

    private List<NetSimCommand> getAdditionalCommands() {
        return Lists.newArrayList(
                NetSimCommands.open(this.getSimulationName()),
                NetSimCommands.selectnocallback(this.getName())
        );
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(simulation, name, type, techType, nodeType, mim, ip);
    }

    @Override
    public String toString() {
        return "NetsimNEImpl [name=" + name + ", type=" + type
                + ", techType=" + techType + ", nodeType=" + nodeType
                + ", mim=" + mim + ", ip=" + ip + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NetworkElementImpl other = (NetworkElementImpl) obj;

        return Objects.equal(this.simulation, other.simulation) &&
                Objects.equal(this.name, other.name) &&
                Objects.equal(this.type, other.type) &&
                Objects.equal(this.techType, other.techType) &&
                Objects.equal(this.nodeType, other.nodeType) &&
                Objects.equal(this.mim, other.mim) &&
                Objects.equal(this.ip, other.ip);
    }

    @Override
    public Simulation getSimulation() {
        return simulation;
    }
}
