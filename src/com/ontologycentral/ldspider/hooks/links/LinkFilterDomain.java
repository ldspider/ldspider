package com.ontologycentral.ldspider.hooks.links;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Resource;

import com.ontologycentral.ldspider.frontier.Frontier;
import com.ontologycentral.ldspider.hooks.error.ErrorHandler;

/**
 * Add only uris with matching host to queue
 * @@@ Should be pld-level not host-level.
 * 
 * @author aharth
 *
 */
public class LinkFilterDomain implements LinkFilter {
	Logger _log = Logger.getLogger(this.getClass().getSimpleName());

	Frontier _f;
	ErrorHandler _eh;
	
	Set<String> _hosts;
	
	public LinkFilterDomain(Frontier f) {
		_f = f;
		_hosts = new HashSet<String>();
	}
	
	public void addHost(String pld) {
		_hosts.add(pld);
	}
	
	public void setErrorHandler(ErrorHandler eh) {
		_eh = eh;
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
					if (_hosts.contains(u.getHost())) {
						_f.add(u);
						_log.fine("adding " + u + " to frontier");
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
