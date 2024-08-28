package com.ericsson.cifwk.taf.maven.plugin.constants;

public enum ArchetypeConstants {

    GIT_IGNORE("# Compiled source #\n"
            + "*.class\n"
            + "*/bin/*\n\n"
            + "# Eclipse files\n"
            + ".project\n"
            + ".classpath\n"
            + ".settings\n"
            + ".metadata\n\n"
            + "# IntelliJ IDEA files\n"
            + ".idea\n"
            + "*.iws\n"
            + "*.ipr\n"
            + "*.iml\n\n"
            + "# Test output files\n"
            + "*/test-output/*\n"
            + "/test-output/\n"
            + "/test-output\n"
            + "test-output/\n"
            + "test-output/**/*\n"
            + "*/test-output/**/*\n\n"
            + "# Packages\n"
            + "*.jar\n\n"
            + "# Maven build files\n"
            + "target\n"
            + "reports\n"
            + "application.properties\n"
            + "avs-rest-service/application.properties\n\n\n"
            + "# PMD\n"
            + ".pmd\n\n"
            + "# Merge files\n"
            + "*.orig\n\n"
            + "#tmp irectories\n"
            + "tmp/\n"
            + "test-output/\n\n"
            + "# Windows binaries\n"
            + "*.exe\n"
            + "*.dll\n");

    private String name;
    ArchetypeConstants(String defaultName) {
        this.name = defaultName;
    }

    public String toString() {
        return name;
    }
}
