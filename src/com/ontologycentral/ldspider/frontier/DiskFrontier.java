package com.ontologycentral.ldspider.frontier;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;

public class DiskFrontier extends Frontier {
	Logger _log = Logger.getLogger(this.getClass().getSimpleName());

	File _f;
	PrintWriter _ps;
	
	public DiskFrontier(File f) {
		super();
		_f = f;
		
		try {
			open(false);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	void open(boolean append) throws IOException {
		_ps = new PrintWriter(new FileOutputStream(_f, append));
	}
	
	public void close() {
		_ps.flush();
		_ps.close();
	}
	
	public void add(URI u) {
		u = process(u);
		_log.fine("processed " + u);
		if (u != null) {
			synchronized(this) {
				_ps.println(u.toString());
				_ps.flush();
			}
		}
	}
	
	public Iterator<URI> iterator() {
		_ps.flush();
//		try {
//			open(true);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		return new DiskFrontierIterator(_f);
	}

	public void removeAll(Collection<URI> c) {
		throw new UnsupportedOperationException("remove not supported on file, use in-memory queue");
	}
	
	public void reset() {
		_f.delete();
		try {
			open(false);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String toString() {
		return "disk frontier " + _f.getName();
	}
}

class DiskFrontierIterator implements Iterator<URI> {
	BufferedReader _br = null;
	URI _next = null;
	Set<URI> _unique;
	
	public DiskFrontierIterator(File f) {
		_unique = new HashSet<URI>();
		
		try {
			_br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));

			readNext();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
	
	public boolean hasNext() {
		if (_next == null) {
			try {
				_br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return (_next != null);
	}

	public URI next() {
		URI next = _next;
		
		try {
			readNext();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
		return next;
	}

	public void remove() {
		;
	}

	void readNext() throws IOException, URISyntaxException {
		String line;
		URI next = null;
		
		while ((line = _br.readLine()) != null) {
			URI u = new URI(line);
			if (u != null && !_unique.contains(u)) {
				next = u;
				_unique.add(u);
				break;
			}
		}
		
		_next = next;
	}
}