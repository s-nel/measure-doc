package com.snacktrace.measuredoc.javafiles;

import java.io.IOException;

public class MethodPartial {
    /**
     *
     * @param param p
     * @param <T> t
     * @throws IOException e
     * @return i
     */
    public <T> int noSummary(int param) throws IOException {
        return 0;
    }

    /**
     * blah
     * @param param
     * @param <T> t
     * @throws IOException e
     * @return i
     */
    public <T> int noParam(int param) throws IOException {
        return 0;
    }

    /**
     * blah
     * @param param p
     * @param <T>
     * @throws IOException e
     * @return i
     */
    public <T> int noTypeParam(int param) throws IOException {
        return 0;
    }

    /**
     * blah
     * @param param p
     * @param <T> t
     * @throws IOException
     * @return i
     */
    public <T> int noThrows(int param) throws IOException {
        return 0;
    }

    /**
     * blah
     * @param param p
     * @param <T> t
     * @throws IOException e
     * @return
     */
    public <T> int noReturn(int param) throws IOException {
        return 0;
    }
}
