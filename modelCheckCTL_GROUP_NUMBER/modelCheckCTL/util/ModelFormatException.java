package modelCheckCTL.util;

/** Error whose line number refers to the original input file. */
public final class ModelFormatException extends Exception {
    private final int line;
    public ModelFormatException(int line, String message) {
        super("Line " + line + ": " + message);
        this.line = line;
    }
    public int line() { return line; }
}
