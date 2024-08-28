package com.ericsson.cifwk.taf.tools.cli.jsch;

import java.util.regex.Pattern;

public class Expect {

    String stringNoWhitesapce;
    String string;
    Pattern pattern;

    public Expect(String string) {
        this.stringNoWhitesapce = string.replaceAll(" ","");
        this.string=string;
    }

    public Expect(Pattern pattern) {

        this.pattern = pattern;
        this.string = pattern.pattern();
    }

    public boolean in(String string) {
        if (pattern != null) {
            return pattern.matcher(string).find();
        } else {
            return string.contains(this.stringNoWhitesapce);
        }
    }

    public String value() {
        return string;
    }
}
