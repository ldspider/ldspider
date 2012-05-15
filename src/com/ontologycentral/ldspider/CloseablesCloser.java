package com.ontologycentral.ldspider;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Thread that closes some Closeables upon run.
 * 
 * @author Tobias Käfer
 * 
 */
class CloseablesCloser extends Thread implements Set<Closeable> {
	Logger _log = Logger.getLogger(this.getClass().getName());

	private Set<Closeable> _streams = new HashSet<Closeable>();

	public void run() {
		_log.info("Closing down some closeables...");
		for (Closeable c : _streams) {
			try {
				c.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public boolean add(Closeable e) {
		return _streams.add(e);
	}

	public boolean addAll(Collection<? extends Closeable> c) {
		return _streams.addAll(c);
	}

	public void clear() {
		_streams.clear();

	}

	public boolean contains(Object o) {
		return _streams.contains(o);
	}

	public boolean containsAll(Collection<?> c) {
		return _streams.containsAll(c);
	}

	public boolean isEmpty() {
		return _streams.isEmpty();
	}

	public Iterator<Closeable> iterator() {
		return _streams.iterator();
	}

	public boolean remove(Object o) {
		return _streams.remove(o);
	}

	public boolean removeAll(Collection<?> c) {
		return _streams.removeAll(c);
	}

	public boolean retainAll(Collection<?> c) {
		return _streams.retainAll(c);
	}

	public int size() {
		return _streams.size();
	}

	public Object[] toArray() {
		return _streams.toArray();
	}

	public <T> T[] toArray(T[] a) {
		return _streams.toArray(a);
	}

}