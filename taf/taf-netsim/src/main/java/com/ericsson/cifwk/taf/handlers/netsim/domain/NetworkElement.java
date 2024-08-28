package com.ericsson.cifwk.taf.handlers.netsim.domain;

import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.handlers.netsim.NetSimCommandExecutor;
import com.ericsson.cifwk.taf.handlers.netsim.NetSimResult;

/**
 * NetSim network element (aka NE) representation
 */
public interface NetworkElement extends NetSimCommandExecutor {
    /**
     * Returns the NE name
     * @return	NE name
     */
    String getName();

    /**
     * Returns the NE type
     * @return	NE type
     */
    String getType();

    /**
     * Returns the NE tech type
     * @return	NE tech type
     */
    String getTechType();

    /**
     * Returns the NE node type
     * @return	NE node type
     */
    String getNodeType();

    /**
     * Returns the NE mim
     * @return	NE mim
     */
    String getMim();

    /**
     * Returns the NE IP address
     * @return	IP address
     */
    String getIp();

    /**
     * Checks if NE is started
     * @return	<code>true</code> if NE is started, <code>false</code> otherwise.
     */
    boolean isStarted();

    /**
     * Returns the name of the {@link Host} to which this NE belongs to
     * @return	name of the {@link Host} to which this NE belongs to
     */
    String getHostName();

    /**
     * Returns the {@link Host} to which this NE belongs
     * @return	 {@link Host} to which this NE belongs
     */
    Host getHost();

    /**
     * Returns the name of simulation that this NE belongs to
     * @return	name of simulation that this NE belongs to
     */
    String getSimulationName();

    /**
     * Returns the instance of simulation that this NE belongs to
     * @return	instance of simulation that this NE belongs to
     */
    Simulation getSimulation();

    /**
     * Attempts to start this NE.
     * @return	<code>true</code> on success, <code>false</code> on failure
     */
    boolean start();

    /**
     * Attempts to start this NE within a given time
     * @param timeout the length of time to wait in seconds
     * @return true if the NetworkElement started
     * @throws com.ericsson.cifwk.taf.handlers.netsim.NetSimException if the {@link NetworkElement} doesn't reach the started state within the time
     * out
     */
    boolean start(int timeout);

    /**
     * Attempts to stop this NE.
     * @return	<code>true</code> on success, <code>false</code> on failure
     */
    boolean stop();

    /**
     * Create the specified Managed Object on this NetworkElement
     *
     * @param parentFdn the fdn of the MO which this new MO will be created under.
     * @param moType the type of the MO to be created
     * @param moName the name of the MO to be created
     * @param attributesKeyValues the attributes to be created on this new MO,
     *                            This string will take the form attr1=value1,attr2=value2
     *                            null or blank string for default attributes
     * @param quantity the number of these MO's to create
     * @return whether the MO was created successfully or not
     */
    boolean createManagedObject(String parentFdn, String moType, String moName, String attributesKeyValues,
            int quantity);

    /**
     * Delete the specified Managed Object on this NetworkElement
     *
     * @param fdn the fdn of the MO to delete
     * @return whether the MO was delete successfully or not
     */
    boolean deleteManagedObject(String fdn);

    /**
     * Set the specified attributes on the specified Managed Object
     *
     * @param fdn the fdn of the MO to set the attributes on
     * @param attributeKeyValues the attributes to be set on this MO,
     *                           This string will take the form attr1=value1||attr2=value2
     * @return whether the attributes were successfully set on the MO or not.
     */
    boolean setManagedObjectAttributes(String fdn, String attributeKeyValues);

    /**
     * Get the details of the specified Managed Object on this Network Element
     *
     * @param fdn the fdn of the MO to get the details of
     * @param printAttributes whether to print the attributes of this MO or not
     * @param scope The depth to which the MO tree will be traversed. -1 for the full MO tree
     * @param attributes the specific attributes to print,
     *                   all will be printed if none are specified here.
     * @return A {@link NetSimResult} containing the details of the MO
     */
    NetSimResult getManagedObject(String fdn, boolean printAttributes, int scope, String... attributes);
}
