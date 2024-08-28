/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2012
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.cifwk.taf.maven.plugin;

import com.ericsson.cifwk.taf.maven.plugin.constants.ArchetypeConstants;
import com.ericsson.cifwk.taf.tools.http.HttpEndpoint;
import com.ericsson.cifwk.taf.tools.http.HttpResponse;
import com.ericsson.cifwk.taf.tools.http.HttpTool;
import com.ericsson.cifwk.taf.tools.http.HttpToolBuilder;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.Scm;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.twdata.maven.mojoexecutor.MojoExecutor.ExecutionEnvironment;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringBufferInputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
import static org.twdata.maven.mojoexecutor.MojoExecutor.element;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;
import static org.twdata.maven.mojoexecutor.MojoExecutor.name;
import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;

@Mojo(name = "archetype", defaultPhase = LifecyclePhase.GENERATE_SOURCES, requiresProject = false)
public class TafArchetypeMojo extends AbstractMojo {

    @Parameter(property = "artifactId", required = true)
    String artifactId;

    @Parameter(property = "parentGroupId", required = true)
    String parentGroupId;

    @Parameter(property = "parentArtifactId", required = true)
    String parentArtifactId;

    @Parameter(property = "parentVersion", required = true)
    String parentVersion;

    @Parameter(property = "archetypeCatalog",
            defaultValue = "https://arm1s11-eiffel004.eiffel.gic.ericsson.se:8443/nexus/content/repositories/releases/archetype-catalog.xml")
    String catalog;

    @Parameter(property = "version", required = true)
    String version;

    @Parameter(property = "cxpNumber", required = true)
    String cxpNumber;

    ExecutionEnvironment pluginEnv;
    public DirUtils dirUtils = new DirUtils();

    private final String NEXUS_URI = "nexus/content/repositories/releases/com/ericsson/cifwk/ERICtaf_util/maven-metadata.xml";
    private final String NEXUS_IP = "arm1s11-eiffel004.eiffel.gic.ericsson.se";
    private final int NEXUS_PORT = 8443;
    private final String TAF_PROPERTY = "taf_version";
    private final String TESTWARE_IDENTIFIER = "-testware";
    private final Map<String, String> searchAndReplace = new HashMap<>();
    private final String dirSep = File.separator;
    public static final String SCM_DEVELOPERCONNECTION = "${ericsson.scm.url}";
    public static final String GIT_IGNOREFILE = ".gitignore";

    @Component
    protected MavenProject project;
    @Component
    protected MavenSession mavenSession;
    @Component
    protected BuildPluginManager pluginManager;

    /*
    * (non-Javadoc)
    *
    * @see org.apache.maven.plugin.Mojo#execute()
    */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        pluginEnv = executionEnvironment(project, mavenSession, pluginManager);
        Plugin mavenArchetypePlugin = plugin("org.apache.maven.plugins",
                "maven-archetype-plugin", "2.2");
        Xpp3Dom cfgArchetype = configuration(element(name("archetypeCatalog"),
                catalog));
        try {
            executeMojo(mavenArchetypePlugin, "generate", cfgArchetype, pluginEnv);
        } catch (MojoExecutionException e) {
            getLog().error(e);
        }
        setSearchAndReplace();
        addGitignoreFileToProject();
        addEmptyGitIgnoreFileToEmptyDirectories();
        updateParentPom();
        updateSubModuleDirs();
        searchAndReplacePoms();
        addTestWareIdentifierToSubPoms();
        prettyPrintPomFiles();
        moveAllContentToParentFolder();
    }

    /**
     * This method writes the contents of the gitignore constant into the .gitignore file in the project.
     */
    private void addGitignoreFileToProject() {
        String projectPath = getWorkingDir() + dirSep;
        try {
            FileUtils.fileWrite(new File(projectPath + GIT_IGNOREFILE), ArchetypeConstants.GIT_IGNORE.toString());
        } catch (IOException e) {
            getLog().error("Could not write gitignore contents to file", e);
        }
    }

    /**
     * Add an empty gitignore file to empty directories so that the directory structure will be pushed to gerrit
     */
    private void addEmptyGitIgnoreFileToEmptyDirectories() {
        Path baseDirPath = Paths.get(getWorkingDir());
        Collection<Path> dirPaths = new ArrayList<>();

        try {
            dirPaths = dirUtils.scanForEmptyDirectories(baseDirPath);
        } catch (IOException e) {
            getLog().error(e);
        }

        for (Path dir : dirPaths) {
            try {
                FileUtils.fileWrite(new File(dir.toString() + File.separator + GIT_IGNOREFILE), "");
            } catch (IOException e) {
                getLog().error("Could not write empty gitignore file to empty directory", e);
            }
        }
    }

    /**
     * Convenience method to load the pom into memory for editing
     *
     * @param path
     *         The path to the pom file to be read
     * @return the model of the pom
     */
    private Model readPom(String path) {
        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model = null;
        try {
            model = reader.read(new FileInputStream(new File(path)));
        } catch (IOException | XmlPullParserException e) {
            getLog().error(e);
        }
        return model;
    }

    /**
     * Convenience method to write the updated pom to the file.
     *
     * @param path
     *         The path to where the pom file will be written to
     * @param model
     *         The maven model to be used to write the pom file from
     */
    private void writePom(String path, Model model) {
        MavenXpp3Writer writer = new MavenXpp3Writer();
        try {
            writer.write(new FileOutputStream(new File(path)), model);
        } catch (IOException e) {
            getLog().error(e);
        }
    }

    /**
     * Method to make updates to the parent
     */
    private void updateParentPom() {
        String path = getWorkingDir() + dirSep + "pom.xml";
        Model model = readPom(path);
        model.setArtifactId(model.getArtifactId() + TESTWARE_IDENTIFIER);
        Parent parent = model.getParent();
        parent.setArtifactId(parentArtifactId);
        parent.setGroupId(parentGroupId);
        parent.setVersion(parentVersion);
        model.setParent(parent);
        Properties props = model.getProperties();
        props.remove("taf_sdk");

        String latestRelease = getLatestVersionFromNexus();
        if (latestRelease != null) {
            props.setProperty(TAF_PROPERTY, latestRelease);
        }

        Scm scm = new Scm();
        scm.setDeveloperConnection(SCM_DEVELOPERCONNECTION);
        scm.setConnection(SCM_DEVELOPERCONNECTION);
        model.setScm(scm);
        model.setProperties(props);
        writePom(path, model);
    }

    /**
     * Update parent section in sub module poms to point to parent with testware identifier
     */
    private void addTestWareIdentifierToSubPoms() {
        String[] dirs = dirUtils.getSubDirectories(Paths.get(getWorkingDir()));

        for (String eachDir : dirs) {
            String pomPath = getWorkingDir() + File.separator + eachDir + File.separator + "pom.xml";
            Model model = readPom(pomPath);
            Parent parent = model.getParent();
            parent.setArtifactId(parent.getArtifactId() + TESTWARE_IDENTIFIER);
            model.setParent(parent);
            writePom(pomPath, model);
        }
    }

    /**
     * Get the latest release version of TAF from Nexus
     *
     * @return latestRelease
     */
    protected String getLatestVersionFromNexus() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Document doc;
        String latest = null;
        try {
            doc = factory.newDocumentBuilder().parse(
                    new StringBufferInputStream(getInfoFromNexus()));
            NodeList list = doc.getElementsByTagName("release");
            latest = getReleasedVersionFromNexusInfo(list);
        } catch (SAXException | IOException | ParserConfigurationException e) {
            getLog().error(e);
        }
        return latest;
    }

    /**
     * Change the latest version from nexus to the latest Released version according to TAF.
     * i.e. if latest from nexus is 2.1.25, return 2.1.1
     *
     * @return String representation of latest version of TAF
     */
    private String getReleasedVersionFromNexusInfo(NodeList list) {
        String latestVersion = list.item(0).getTextContent();
        String buildNumber = latestVersion.split("\\.")[2];
        int index = latestVersion.lastIndexOf(buildNumber);
        return new StringBuilder(latestVersion).replace(index, index + buildNumber.length(), "1").toString();
    }

    /**
     * Make the call to nexus to get all the version info for ERICtaf_util
     *
     * @return A String representation of all the versions of TAF from Nexus
     */
    protected String getInfoFromNexus() {
        HttpEndpoint host = new HttpEndpoint() {
            @Override
            public String getHostname() {
                return null;
            }

            @Override
            public String getIp() {
                return NEXUS_IP;
            }

            @Override
            public String getIpv6() {
                return null;
            }

            @Override
            public Integer getHttpPort() {
                return null;
            }

            @Override
            public Integer getHttpsPort() {
                return NEXUS_PORT;
            }
        };

        HttpTool tool = HttpToolBuilder.newBuilder(host).useHttpsIfProvided(true).trustSslCertificates(true).build();
        HttpResponse resp = tool.get(NEXUS_URI);
        return resp.getBody();
    }

    /**
     * Method to set the list of items that will be used for search and replace
     * Note: Adding an entry to the 'searchAndReplace' map in this method will drive the renaming of any module
     * subfolder or instance of text in any pom i.e if a new module is added, then just add the entry for it here and
     * the renaming will be done automatically.
     */
    private void setSearchAndReplace() {
        searchAndReplace.put("modulename", artifactId);
        searchAndReplace.put("CXPxxxxxxxx", cxpNumber);
    }

    /**
     * Utility method to perform a search & replace on any string based on the elements in the searchAndReplace map
     *
     * @param searchMe
     *         String to be searched
     * @return String resulting from the search and replace
     */
    private String searchAndReplace(String searchMe) {
        for (String each : searchAndReplace.keySet()) {
            searchMe = searchMe.replace(each, searchAndReplace.get(each)); //need to make this dynamic on start up
        }
        return searchMe;
    }

    /**
     * Utility method to get the working directory
     *
     * @return String path of the working directory
     */
    private String getWorkingDir() {
        return System.getProperty("user.dir") + dirSep + artifactId;
    }

    /**
     * Rename sub module directories based on the 'searchAndReplace' map elements
     */
    private void updateSubModuleDirs() {
        String[] directories = dirUtils.getSubDirectories(Paths.get(getWorkingDir()));
        for (String each : directories) {
            File oldDir = new File(getWorkingDir() + dirSep + each);
            File newDir = new File(getWorkingDir() + dirSep
                    + searchAndReplace(each));
            oldDir.renameTo(newDir);
        }
    }

    /**
     * Method to do search & replace on all poms in the project
     * Search and replace will be performed based on the elements in the searchAndReplace map
     */
    private void searchAndReplacePoms() {
        Path baseDirPath = Paths.get(getWorkingDir());
        Collection<Path> paths = new ArrayList<>();
        try {
            paths = dirUtils.scanForFiles(baseDirPath);
        } catch (IOException e) {
            getLog().error(e);
        }
        for (Path each : paths) {
            if (each.toString().endsWith("pom.xml")) {
                Charset charset = StandardCharsets.UTF_8;
                try {
                    String content = new String(Files.readAllBytes(each), charset);
                    String newContent = searchAndReplace(content);
                    Files.write(each, newContent.getBytes(charset));
                } catch (IOException e) {
                    getLog().error(e);
                }
            }
        }
    }

    /**
     * Method to move the content generated from calling the maven archetype up a level
     */
    private void moveAllContentToParentFolder() {
        final Path fromPath = Paths.get(getWorkingDir());
        final Path toPath = fromPath.getParent();

        try {
            dirUtils.copyFiles(fromPath, toPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            getLog().error(e);
        }

        //Extra stubbornness required to delete folder because maven seems to have a hook in pom files on windows
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    org.codehaus.plexus.util.FileUtils.deleteDirectory(new File(System.getProperty("user.dir") + dirSep + artifactId));
                } catch (IOException e) {
                    getLog().error(e);
                }
            }
        }));
    }

    /**
     * Private method to pretty print the pom files with indentation set to four spaces
     * (required because MavenXpp3Writer implementation used in writePom method has indentation
     * hard coded to two spaces)
     */
    private void prettyPrintPomFiles() {
        Path baseDirPath = Paths.get(getWorkingDir());
        Collection<Path> paths = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\S");
        try {
            paths = dirUtils.scanForFiles(baseDirPath);
        } catch (IOException e) {
            getLog().error(e);
        }

        for (Path pomPath : paths) {
            if (pomPath.toString().endsWith("pom.xml")) {
                File inFile = new File(pomPath.toString());
                List<String> newFileLines = new ArrayList<>();
                try {
                    List<String> lines = org.apache.commons.io.FileUtils.readLines(inFile);
                    for (String each : lines) {
                        if (!each.startsWith(" ") && !(each.trim().length() == 0)) {
                            newFileLines.add(each);
                        } else {
                            Matcher matcher = pattern.matcher(each);
                            int start = matcher.find() ? matcher.start() : -1;

                            String space = "";
                            for (int x = 0; x < (start * 2); x++) {
                                space += " ";
                            }
                            String newString = space.concat(each.trim());
                            newFileLines.add(newString);
                        }
                    }
                    Files.write(pomPath, newFileLines, Charset.defaultCharset());
                } catch (IOException e) {
                    getLog().error(e);
                }
            }
        }
    }

}
