package com.ontologycentral.ldspider;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.Writer;

public class AppendableWriterAdapter extends Writer {

	private Appendable appendable;
	
	public AppendableWriterAdapter(Appendable appendable) {
		this.appendable = appendable;
	}
	
	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		appendable.append(String.valueOf(cbuf), off, len);
	}

	@Override
	public void flush() throws IOException {
		if (appendable instanceof Flushable) {
			((Flushable) appendable).flush();
		}
	}

	@Override
	public void close() throws IOException {
		flush();
		if (appendable instanceof Closeable) {
			((Closeable) appendable).close();
		}
	}
}