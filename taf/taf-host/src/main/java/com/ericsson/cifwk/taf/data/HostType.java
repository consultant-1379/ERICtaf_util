package com.ericsson.cifwk.taf.data;

import com.ericsson.cifwk.meta.API;
import com.google.gson.annotations.SerializedName;

import static com.ericsson.cifwk.meta.API.Quality.Stable;

/**
 * Enum to specify the types of hosts
 */
@API(Stable)
public enum HostType {

    @SerializedName("ms")
    MS("MS"),
    @SerializedName("sc1")
    SC1("SC1"),
    @SerializedName("sc2")
    SC2("SC2"),
    @SerializedName("pl")
    PL("PL"),
    @SerializedName("nfs")
    NFS("NFS"),
    @SerializedName("sfs")
    SFS("SFS"),
    @SerializedName("san")
    SAN("SAN"),
    @SerializedName("rc")
    RC("RC"),
    @SerializedName("netsim")
    NETSIM("NETSIM"),
    @SerializedName("jboss")
    JBOSS("JBOSS"),
    @SerializedName("graphite")
    GRAPHITE("GRAPHITE"),
    @SerializedName("rabbitmq")
    RABBITMQ("RABBITMQ"),
    @SerializedName("selenium_grid")
    SELENIUM_GRID("SELENIUM_GRID"),
    @SerializedName("unknown")
    UNKNOWN("UNKNOWN"),
    @SerializedName("unexpected")
    UNEXPECTED("UNEXPECTED"),
    @SerializedName("cifwk")
    CIFWK("CIFWK"),
    @SerializedName("ebas")
    EBAS("EBAS"),
    @SerializedName("nedss")
    NEDSS("NEDSS"),
    @SerializedName("peer")
    PEER("PEER"),
    @SerializedName("omsas")
    OMSAS("OMSAS"),
    @SerializedName("omsrvm")
    OMSRVM("OMSRVM"),
    @SerializedName("omsrvs")
    OMSRVS("OMSRVS"),
    @SerializedName("uas")
    UAS("UAS"),
    @SerializedName("gateway")
    GATEWAY("GATEWAY"),
    @SerializedName("http")
    HTTP("HTTP"),
    @SerializedName("httpd")
    HTTPD("HTTPD"),
    @SerializedName("db")
    DB("db"),
    @SerializedName("svc")
    SVC("svc"),
    @SerializedName("scp")
    SCP("scp"),
    @SerializedName("ombs")
    OMBS("ombs"),
    @SerializedName("evt1")
    EVT1("evt1"),
    @SerializedName("evt2")
    EVT2("evt2"),
    @SerializedName("LVS_Router")
    LVS_ROUTER("LVS_Router"),
    @SerializedName("ilo")
    ILO("ilo"),
    @SerializedName("workload")
    WORKLOAD("workload"),
    @SerializedName("client")
    CLIENT("client"),
    @SerializedName("str1")
    STR1("str1"),
    @SerializedName("str2")
    STR2("str2"),
    @SerializedName("str3")
    STR3("str3"),
    @SerializedName("str4")
    STR4("str4"),
    @SerializedName("str5")
    STR5("str5"),
    @SerializedName("str6")
    STR6("str6"),
    @SerializedName("str7")
    STR7("str7"),
    @SerializedName("str8")
    STR8("str8"),
    @Deprecated
    @SerializedName("db1")
    DB1("db1"),
    @Deprecated
    @SerializedName("db2")
    DB2("db2"),
    @Deprecated
    @SerializedName("db3")
    DB3("db3"),
    @Deprecated
    @SerializedName("db4")
    DB4("db4"),
    @Deprecated
    @SerializedName("svc1")
    SVC1("svc1"),
    @Deprecated
    @SerializedName("svc2")
    SVC2("svc2"),
    @Deprecated
    @SerializedName("svc3")
    SVC3("svc3"),
    @Deprecated
    @SerializedName("svc4")
    SVC4("svc4"),
    @Deprecated
    @SerializedName("svc5")
    SVC5("svc5"),
    @Deprecated
    @SerializedName("svc6")
    SVC6("svc6"),
    @Deprecated
    @SerializedName("svc7")
    SVC7("svc7"),
    @Deprecated
    @SerializedName("svc8")
    SVC8("svc8"),
    @Deprecated
    @SerializedName("svc9")
    SVC9("svc9"),
    @Deprecated
    @SerializedName("svc10")
    SVC10("svc10"),
    @Deprecated
    @SerializedName("scp1")
    SCP1("scp1"),
    @Deprecated
    @SerializedName("scp2")
    SCP2("scp2"),
    @Deprecated
    @SerializedName("scp3")
    SCP3("scp3"),
    @Deprecated
    @SerializedName("scp4")
    SCP4("scp4"),
    @SerializedName("director")
    DIRECTOR("DIRECTOR"),
    @SerializedName("master")
    MASTER("MASTER"),
    @SerializedName("worker")
    WORKER("WORKER"),
    @SerializedName("eventlistener")
    EVENTLISTENER("EVENTLISTENER");
    
    private String name;

    HostType(String defaultName) {
        this.name = defaultName;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    public HostType getNext() {
        int index = (this.ordinal() + 1) % HostType.values().length;
        return HostType.values()[index];
    }

    public static HostType getByName(String name) {
        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }
}
