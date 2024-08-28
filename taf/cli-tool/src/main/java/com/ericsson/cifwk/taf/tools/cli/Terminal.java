package com.ericsson.cifwk.taf.tools.cli;

import static com.ericsson.cifwk.meta.API.Quality.Deprecated;

import java.io.Serializable;

import com.ericsson.cifwk.meta.API;

/**
 * This class provides SSH Terminal with description
 * <p>Usage is deprecated, please use
 * <a href="https://taf.seli.wh.rnd.internal.ericsson.com/cli-tool/">cli-tool</a> instead.</p>
 *
 * @deprecated
 */
@Deprecated
@API(Deprecated)
@API.Since(2.35)
public class Terminal implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * Description VT100 terminal with default parameters
     */
    public static final transient Terminal VT100 = new Terminal("vt100", 80, 24, 0, 0);
    /**
     * Description XTERM terminal with default parameters
     */
    public static final transient Terminal XTERM = new Terminal("xterm", 80, 24, 0, 0);

    /**
     * Description NULL terminal with all null parameters
     */
    public static final transient Terminal NULL = null;

    String ttype;
    int columns;
    int rows;
    int width;
    int height;

    protected Terminal(String ttype, int columns, int rows, int width, int height) {
        this.ttype = ttype;
        this.columns = columns;
        this.rows = rows;
        this.width = width;
        this.height = height;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Terminal that = (Terminal) o;
        if (columns != that.columns) return false;
        if (height != that.height) return false;
        if (rows != that.rows) return false;
        if (width != that.width) return false;
        if (!ttype.equals(that.ttype)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = ttype.hashCode();
        result = 31 * result + columns;
        result = 31 * result + rows;
        result = 31 * result + width;
        result = 31 * result + height;
        return result;
    }

    /**
     * Return the name of the terminal (for example, "vt100")
     *
     * @return terminal name
     */
    public String getType() {
        return ttype;
    }

    /**
     * Return the terminal width in columns
     *
     * @return terminal width, columns
     */
    public int getColumns() {
        return columns;
    }

    /**
     * Return the terminal height in rows
     *
     * @return terminal height, rows
     */
    public int getRows() {
        return rows;
    }

    /**
     * Return the terminal width in pixels
     *
     * @return terminal width, pixels
     */
    public int getWidth() {
        return width;
    }

    /**
     * Return the terminal height in pixels
     *
     * @return terminal height, pixels
     */
    public int getHeight() {
        return height;
    }

    @Override
    public String toString() {
        return ttype + '[' +
                columns + ',' +
                rows + ',' +
                width + ',' +
                height + ']';
    }
}
