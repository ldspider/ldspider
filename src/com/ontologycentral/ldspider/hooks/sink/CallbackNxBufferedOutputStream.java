package com.ontologycentral.ldspider.hooks.sink;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.Callback;

public class CallbackNxBufferedOutputStream implements Callback {
	private final Logger _log = Logger.getLogger(this.getClass().getSimpleName());

	final OutputStream _out;
	
	final static int BUF_SIZE = 8*1024;

	long _cnt = 0;
	long _time, _time1;
	final boolean _close;
	
	public final static byte[] SPACE = " ".getBytes();
	public final static byte[] DOT_NEWLINE = ("."+System.getProperty("line.separator")).getBytes();
	
	public CallbackNxBufferedOutputStream(OutputStream out) {
		this(out, false);
	}
	
	public CallbackNxBufferedOutputStream(OutputStream out, boolean close) {
		_out = new BufferedOutputStream(out);
		_close = close;
		
	}
	
	public synchronized void processStatement(Node[] nx) {
		try {
			for (Node n : nx) {
				_out.write(n.toN3().getBytes());
				_out.write(SPACE);
			}
			_out.write(DOT_NEWLINE);
		} catch (IOException e) {
			_log.severe(e.getMessage());
		}
		
		_cnt++;
	}
	
	public void startDocument() {
		_time = System.currentTimeMillis();
	}

	public void close() {
		try {
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