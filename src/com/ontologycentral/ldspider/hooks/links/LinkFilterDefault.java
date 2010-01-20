package com.ontologycentral.ldspider.hooks.links;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Nodes;
import org.semanticweb.yars.nx.Resource;

import com.ontologycentral.ldspider.hooks.error.ErrorHandler;
import com.ontologycentral.ldspider.queue.SpiderQueue;

/**
 * Follow all links (subject, predicate, object).
 * 
 * @author aharth
 *
 */
public class LinkFilterDefault implements LinkFilter {
	Logger _log = Logger.getLogger(this.getClass().getSimpleName());

	Set<URI> _links;
	ErrorHandler _eh;
	
	public LinkFilterDefault(ErrorHandler eh) {
		_links = Collections.synchronizedSet(new HashSet<URI>());
		_eh = eh;
	}
	
	public Set<URI> getLinks() {
		return _links;
	}

	public void startDocument() {
		;
	}
	
	public void endDocument() {
		;
	}

	public void processStatement(Node[] nx) {
		for (int i = 0; i < nx.length-1; i++) {
			if (nx[i] instanceof Resource) {
				try {
					URI u = new URI(nx[i].toString());
					u = SpiderQueue.normalise(u);
					if (_links.add(u) == false) {
						_eh.handleStatus(u, 497, null, 0, 0);
					}
				} catch (URISyntaxException e) {
					try {
						_eh.handleError(new URI(nx[nx.length-1].toString()), e);
					} catch (URISyntaxException e1) {
						e1.printStackTrace();
					}
				}
			}
		}
	}
}
