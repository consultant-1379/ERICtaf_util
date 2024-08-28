#!/usr/bin/perl

# Script is responsible for getting all the dependencies of latest testware
# returned by CI Portal (https://cifwk-oss.lmera.ericsson.se/getLatestTestware/).
#
# It parses result XML, fetches all URL of returned test POMs from Nexus
# (first it tries test POM URL as it was returned by CI Portal, 
# e.g. https://arm1s11-eiffel004.eiffel.gic.ericsson.se:8443/nexus/content/groups/public/com/ericsson/oss/rv/test-pom-system-test-cdb-server-checks/1.0.2552/test-pom-system-test-cdb-server-checks-1.0.2552.pom, 
# if POM wasn't found it replaces URL '/groups/public/' with '/repositories/ossrc/', in this case
# e.g. https://arm1s11-eiffel004.eiffel.gic.ericsson.se:8443/nexus/content/repositories/ossrc/com/ericsson/oss/rv/test-pom-system-test-cdb-server-checks/1.0.2552/test-pom-system-test-cdb-server-checks-1.0.2552.pom)
# 
# On pom.xml being downloaded script executes maven command to download all dependencies of POM.
#
# Finally script outputs how many POM files were successfully processed. In case of any error script should fail fast.

use strict;
use XML::Simple;
use LWP::Simple;

system("mkdir /proj/lciadm100/tools/jqassistantData/jars");
my $ciPortalUrl = "https://cifwk-oss.lmera.ericsson.se/getLatestTestware/";
# For testing purposes CI Portal can be mocked
#my $xmlfile = "/proj/lciadm100/tools/Nexus_Script/enmOssrcAndBadTestware.xml";
my $xmlfile = get($ciPortalUrl);
my $pomList = XMLin($xmlfile);
my $processedTestPomsCount = 0;
my $processedWrongUrlPomsCount = 0;
my @processedWrongUrls = ();
my %testwaresToIgnore = map { $_ => 1 } ("ERICTAFmediation_CXP9030292", "ERICTAFtorfm_CXP9030464", "ERICTAFkpispecificationserviceapitestware_CXP90317", "ERICTAFNSS_CXP9030937", "ERICTAFonboardingone_CXP9031891","ERICTAFkvmmaintracktestsuite_CXP9031820");
my @ignoredTestwares = ();

foreach my $testPom (@{$pomList->{testwareArtifact}}) {

    # some testwares can't be found in Nexus and therefore are in ignore list
    my $artifactId = $testPom->{artifactId};
    if(exists($testwaresToIgnore{$artifactId})) {
        push @ignoredTestwares, $artifactId;
        next;
    }

    # trying different repositories to download POM or fail fast
    my $pomUrl = getPomUrl($testPom);
    my $jarUrl = getJarUrl($testPom);
    storeNexusArtifactIn($pomUrl, "/proj/lciadm100/tools/jqassistantData/", "pom.xml");
    storeNexusArtifactIn($jarUrl, "/proj/lciadm100/tools/jqassistantData/jars/");
	print "SUCCESS\n\n";

	# storing all dependencies in folder 'jars'
	my $mavenCommandsToExecute = "set -e; cd /proj/lciadm100/tools/jqassistantData; mvn dependency:copy-dependencies -DexcludeTransitive=false -DoutputDirectory=jars -DincludeGroupIds=com.ericsson -DincludeTypes=jar; cd ..";
	my $errorMessage = "\n\nBUILD FAILED: there were errors executing maven goal\n\n\n";
	print "Executing commands: '$mavenCommandsToExecute'";
    0 == system($mavenCommandsToExecute) or die $errorMessage;
	
	$processedTestPomsCount += 1;
}

# some statistics on POM files processed
print "\nTEST POMS PROCESSED: '$processedTestPomsCount'";
print "\nTEST POMS PROCESSED BUT HAVING WRONG URL IN CI PORTAL: '$processedWrongUrlPomsCount'\n";
foreach my $processedWrongUrl (@processedWrongUrls) {
    print "Wrong URL in CI Portal: " . $processedWrongUrl . "\n";
}
foreach my $ignoredTestware (@ignoredTestwares) {
    print "Testware " . $ignoredTestware . " was ignored (it wasn't found in Nexus previously and is currently in ignore list) \n";
}
print "\n\n\n";

sub storeNexusArtifactIn() {

    my $artifactUrl = $_[0];
    my $targetFolder = $_[1];
    my $targetFile = $_[2];

    # by default target file is taken from URL
    if ($artifactUrl =~ /(.+\/)(.+)/ & $targetFile eq "") {
        $targetFile = $2;
    }
    $targetFile = $targetFolder . $targetFile;

    # trying default repository
	print "\nDownloading\n$artifactUrl\n";
    my $responseCode = getstore($artifactUrl, $targetFile);
	if ($responseCode eq 200) {
	    return;
	}

	# if not found in default repository - trying out following repositories:
    # ENM repository:   https://arm1s11-eiffel004.eiffel.gic.ericsson.se:8443/nexus/content/groups/public/
    # OSSRC repository: https://arm1s11-eiffel004.eiffel.gic.ericsson.se:8443/nexus/content/repositories/oss_releases/
	$processedWrongUrlPomsCount += 1;
	push @processedWrongUrls, $artifactUrl;

	if ($artifactUrl =~ /(.+\/nexus\/content\/)([^\/]+\/[^\/]+\/)(.+)/ ) {
        # e.g. https://arm1s11-eiffel004.eiffel.gic.ericsson.se:8443/nexus/content/repositories/oss_releases/com/ericsson/taf
        # is splitted into:
        my $nexusBaseUrl = $1;  # https://arm1s11-eiffel004.eiffel.gic.ericsson.se:8443/nexus/content/
        my $pomPath = $3;       # com/ericsson/taf
        my @repositoryNames = ("repositories/oss_releases/", "groups/public/");
        foreach my $repositoryName (@repositoryNames) {
            $artifactUrl = $nexusBaseUrl . $repositoryName . $pomPath;
            print "failed - trying another repository:\n$artifactUrl\n";
            my $responseCode = getstore($artifactUrl, $targetFile);
            if ($responseCode eq 200) {
                return;
            }
        }
    }

    # Artifact not found in any repository - fail fast
	my $errorMessage = " FAILED\n";
    print $errorMessage;
    die $errorMessage;
}

sub getPomUrl() {
    my $testPom = $_[0];
    return getTestwareUrl($testPom, "pom");
}

sub getJarUrl() {
    my $testPom = $_[0];
    return getTestwareUrl($testPom, "jar");
}

sub getTestwareUrl() {

    # assembling testware URL from testware GAV
    my $testPom = $_[0];
    my $artifactType = $_[1];
    my $pomURL = trim($testPom->{testPom});
    my $groupId = $testPom->{groupId};
    $groupId =~ s/\./\//g;  # replace all "." with "/"
    my $artifactId = $testPom->{artifactId};
    my $version = $testPom->{version};
    if ($pomURL =~ /(.+\/nexus\/content\/)([^\/]+\/[^\/]+\/)(.+)/ ) {
        # e.g. https://arm1s11-eiffel004.eiffel.gic.ericsson.se:8443/nexus/content/repositories/oss_releases/com/ericsson/taf
        # is splitted into:
        my $nexusBaseUrl = $1;  # https://arm1s11-eiffel004.eiffel.gic.ericsson.se:8443/nexus/content/
        my $nexusRepoPath = $2; # repositories/oss_releases/
        my $pomPath = $3;       # com/ericsson/taf
        return $nexusBaseUrl . $nexusRepoPath . $groupId . "/" . $artifactId . "/" . $version . "/" . $artifactId . "-" . $version . "." . $artifactType;
    }
    die "\nBad Nexus URL: '$pomURL'\n";
}

sub trim() {
    my $str = "@_";
    $str =~ s/^\s+//;
    $str =~ s/\s+$//;
    return $str;
}
