package com.ontologycentral.ldspider.hooks.sink;

import java.io.IOException;
import java.io.OutputStream;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.Callback;

public class CallbackNxBufferedOutputStream implements Callback {
	final OutputStream _out;
	
	final static int BUF_SIZE = 8*1024;

	long _cnt = 0;
	long _time, _time1;
	final boolean _close;
	
	public final static byte[] SPACE = " ".getBytes();
	public final static byte[] DOT_NEWLINE = ("."+System.getProperty("line.separator")).getBytes();
	
	Node[][] _buffer;
	int _i;
	
	public CallbackNxBufferedOutputStream(OutputStream out) {
		this(out, false);
	}
	
	public CallbackNxBufferedOutputStream(OutputStream out, boolean close) {
		_out = out;
		_close = close;
		
		_buffer = new Node[BUF_SIZE][];
		_i = 0;
	}
	
	public synchronized void processStatement(Node[] nx) {
		_buffer[_i] = nx;
		_i++;

		if (_i >= (BUF_SIZE-1)) {
			try {
				flush();
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			} finally {
				_i = 0;
			}
		}
		
		_cnt++;
	}
	
	void flush() throws IOException {
		for (int i = 0; i < _i; i++) {
			for (Node n:_buffer[i]) {
				_out.write(n.toN3().getBytes());
				_out.write(SPACE);
			}
			_out.write(DOT_NEWLINE);
		}
	}

	public void startDocument() {
		_time = System.currentTimeMillis();
	}

	public void close() {
		try {
			flush();

			if(_close) {
				_out.close();
			} else {
				_out.flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		_time1 = System.currentTimeMillis();
	}

	public void endDocument() {
		;
	}
	
	public String toString() {
		return _cnt + " tuples in " + (_time1-_time) + " ms";
	}
}