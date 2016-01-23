package io.j99.idea.vue.cli.build;

public class VerifyMessage {
    public static final String WARN = "WARN";
    public static final String ERROR = "ERROR";
    public static final String INFO = "INFO";
    public String level;
    public String msg;
    public String file;
    public int line;
    public int column;
    public int index;
    public int startOffset;
    public int endOffset;
}
