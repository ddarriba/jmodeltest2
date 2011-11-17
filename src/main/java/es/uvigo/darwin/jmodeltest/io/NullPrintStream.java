package es.uvigo.darwin.jmodeltest.io;

import java.io.OutputStream;

public class NullPrintStream extends OutputStream {
	public void write(byte[] buf, int off, int len) {
	}

	public void write(int b) {
	}

	public void write(byte[] b) {
	}
}
