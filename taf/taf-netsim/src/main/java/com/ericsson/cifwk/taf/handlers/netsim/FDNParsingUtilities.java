package com.ericsson.cifwk.taf.handlers.netsim;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;

public final class FDNParsingUtilities {

    private static final String ME_CONTEXT = "MeContext";
    private static final String MANAGED_ELEMENT = "ManagedElement";
    private static final String COMMA = ",";
    private static final String UNDERSCORE = "_";
    private static final String EQUALS = "=";
    private static final String NETWORK_ELEMENT = "NetworkElement";

    private FDNParsingUtilities(){}

    /**
     * Extract the id of the Network Element from the given fdn.
     * The id is retrieved from one of the following MO's if present and in this order of preference:
     * 1. MeContext
     * 2. ManagedElement
     * 3. NetworkElement
     * @param fdn the fdn to parse for the Network Element Id
     * @return the id of the network element if found otherwise
     * @throws NullPointerException if the fdn doesn't contain any of the MO's listed above.
     */
    public static String getNetworkElementId(final String fdn){
        String networkElementId = null;
        if (fdn.contains(ME_CONTEXT)) {
            networkElementId = getMoValue(ME_CONTEXT, fdn);
        } else if (fdn.contains(MANAGED_ELEMENT)) {
            networkElementId = getMoValue(MANAGED_ELEMENT, fdn);
        } else if (fdn.contains(NETWORK_ELEMENT)){
            networkElementId = getMoValue(NETWORK_ELEMENT, fdn);
        }
        Preconditions.checkNotNull(networkElementId, String.format("It was not possible to get the NetworkElement Id from this fdn , %s", fdn));
        return networkElementId;
    }

    /**
     * Extract the value for the given Managed Object in the given FDN
     * @param mo The ManagedObject to find the value of
     * @param fdn The FDN to search
     * @return The value of the Managed Object if found otherwise
     * @throws NullPointerException if the fdn doesn't contain the MO
     */
    public static String getMoValue(final String mo, final String fdn) {
        Preconditions.checkState(StringUtils.containsIgnoreCase(fdn,mo), String.format("The MO [%s] was not found in the FDN [%s]", mo, fdn));
        final String[] rdns = fdn.split(COMMA);
        String moValue = null;
        for (final String rdn : rdns) {
            if (StringUtils.containsIgnoreCase(rdn, mo)) {
                moValue = getMoValue(rdn);
                if (moValue.contains(UNDERSCORE)) {
                    final String[] splitValue = moValue.split(UNDERSCORE);
                    moValue = splitValue[splitValue.length - 1];
                }
                break;
            }
        }
        return moValue;
    }

    private static String getMoValue(final String rdn) {
        final String[] splitRdn = rdn.split(EQUALS);
        return splitRdn[splitRdn.length - 1];
    }

    /**
     * Extracts the LDN from the FDN
     * e.g. For FDN 'MeContext=LTE01ERBS1,ManagedElement=1,ENodeBFunction=1' the LDN is
     * 'ManagedElement=1,ENodeBFunction=1'
     * e.g. For Subnetwork and MeContext:
     * 'SubNetwork=SGSN01,MeContext=SGSN01,ManagedElement=1,SgsnMme=1' the LDN is
     * 'ManagedElement=1,SgsnMme=1'
     * For Com/Ecim Nodes this method will not remove the namespace if provided
     * e.g. FDN 'SubNetwork=SGSN01,SgsnMmeTop:ManagedElement=SGSN01,SgsnMmeTop:SystemFunctions=1'
     * the LDN is 'SgsnMmeTop:ManagedElement=SGSN01,SgsnMmeTop:SystemFunctions=1'
     */
    public static String getLdn(final String fdn) {
        return fdn.replaceFirst(".*SubNetwork=(([^,=\"]*),)", "").replaceFirst(".*MeContext=(([^,=\"]*),)", "");
    }
}
