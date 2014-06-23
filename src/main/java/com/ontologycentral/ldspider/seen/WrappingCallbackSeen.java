package com.ontologycentral.ldspider.seen;

import java.net.URI;
import java.util.Collection;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.parser.Callback;
import org.semanticweb.yars.nx.util.NxUtil;

/**
 * A {@link Seen} implementation that passes the seen {@link URI}s to a wrapped
 * {@link Seen} implementation and a {@link Callback} implementation.
 * 
 * @author Tobias Kaefer
 */
public class WrappingCallbackSeen implements Seen {

	Seen _seen;
	Callback _cb;

	public WrappingCallbackSeen(Seen seen, Callback callback) {
		_seen = seen;
		_cb = callback;
	}

	public boolean hasBeenSeen(URI u) {
		return _seen.hasBeenSeen(u);
	}

	public boolean add(Collection<URI> uris) {
		boolean ret = false;
		for (URI u : uris) {
			ret = add(u) || ret;
		}
		return ret;
	}

	public boolean add(URI uri) {
		boolean ret = _seen.add(uri);
		if (ret)
			_cb.processStatement(new Node[] { new Resource(NxUtil
					.escapeForNx(uri.toString())) });
		return ret;
	}

}
