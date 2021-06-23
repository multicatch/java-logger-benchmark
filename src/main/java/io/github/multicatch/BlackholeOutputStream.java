package io.github.multicatch;

import java.io.IOException;
import java.io.OutputStream;

public class BlackholeOutputStream extends OutputStream {

    private int variable = 0;

    @Override
    public void write(int i) throws IOException {
        if ((long) variable + i > Integer.MAX_VALUE) {
            variable = 0;
        }
        variable += i;
    }

    @Override
    public void write(byte[] b) throws IOException {
        this.write(b.length);
    }

    @Override
    public void write(byte[] bytes, int i, int i1) throws IOException {
        this.write(i + i1);
    }

    @Override
    public void flush() throws IOException {
        variable %= 255;
    }
}
