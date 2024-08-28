package com.ericsson.cifwk.taf.performance.metric;

public class MetricsName {

    private final String group;
    private final String suite;
    private final String test;
    private final String protocol;

    private MetricsName(Builder builder) {
        group = builder.group;
        suite = builder.suite;
        test = builder.test;
        protocol = builder.protocol;
    }

    public void handleParts(PartHandler partHandler) {
        handlePart(partHandler, "group", group);
        handlePart(partHandler, "suite", suite);
        handlePart(partHandler, "test", test);
        handlePart(partHandler, "protocol", protocol);
    }

    private void handlePart(PartHandler partHandler, String key, String value) {
        if (value != null) {
            partHandler.handle(key, value);
        }
    }

    public interface PartHandler {
        void handle(String key, String value);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String group;
        private String suite;
        private String test;
        private String protocol;

        private Builder() {
        }

        public Builder group(String group) {
            this.group = group;
            return this;
        }

        public Builder suite(String suite) {
            this.suite = suite;
            return this;
        }

        public Builder test(String test) {
            this.test = test;
            return this;
        }

        public Builder protocol(String protocol) {
            this.protocol = protocol;
            return this;
        }

        public MetricsName build() {
            return new MetricsName(this);
        }
    }

}
