#!/bin/sh

# Example job - https://fem119-eiffel004.lmera.ericsson.se:8443/jenkins/view/All/job/JqAssistant_Remote_Scan/configure
# To be replaced with Job DSL

# stopping Neo4j + removing previously downloaded jars
/proj/lciadm100/tools/neo4j-community-2.2.0/bin/neo4j stop;
rm -rf /proj/lciadm100/tools/jqassistantData/jars;

# since now any command failure should fail job
set -e

# installing unresolvable maven dependencies
touch empty.txt
mvn install:install-file -Dfile=empty.txt -DgroupId=org.forgerock.opendj -DartifactId=i18n-core -Dversion=2.6.0 -Dpackaging=jar
mvn install:install-file -Dfile=empty.txt -DgroupId=com.ericsson.oss.services.xstream -DartifactId=xstream-base -Dversion=1.0.1-SNAPSHOT -Dpackaging=jar
mvn install:install-file -Dfile=empty.txt -DgroupId=com.ericsson.oss.services.xstream -DartifactId=xstream-loading -Dversion=1.0.1-SNAPSHOT -Dpackaging=jar

# fetching all dependencies of latest testware according to CI Portal
perl /proj/lciadm100/tools/Nexus_Script/retrieveJars.pl;
echo "All testware dependencies downloaded successfully"

# purging artificial maven dependencies installed previously
cd /proj/lciadm100/tools/jqassistantData/
mvn dependency:purge-local-repository -DmanualInclude=com.ericsson.oss.services.xstream:xstream-loading
mvn dependency:purge-local-repository -DmanualInclude=com.ericsson.oss.services.xstream:xstream-base
mvn dependency:purge-local-repository -DmanualInclude=org.forgerock.opendj:i18n-core

# cleaning up DB
rm -rf /jqassistant;

# launching jQAssistant scanner
/proj/lciadm100/tools/jqassistant.distribution-1.0.0/bin/jqassistant.sh scan -f /proj/lciadm100/tools/jqassistantData -s /jqassistant/store;

# starting Neo4j
/proj/lciadm100/tools/neo4j-community-2.2.0/bin/neo4j start;