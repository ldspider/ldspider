package com.ontologycentral.ldspider.hooks.links;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.yars.nx.Node;

import com.ontologycentral.ldspider.frontier.Frontier;

/**
 * Add only uris with matching host to queue
 * @@@ Should be pld-level not host-level.
 * 
 * @author aharth
 *
 */
public class LinkFilterDomain extends LinkFilterDefault {
	
	Set<String> _hosts;
	
	public LinkFilterDomain(Frontier f) {
		super(f);
		_hosts = new HashSet<String>();
	}
	
	public void addHost(String pld) {
		_hosts.add(pld);
	}

	public void startDocument() {
		;
	}
	
	public void endDocument() {
		;
	}
	
	@Override
	protected void addABox(Node[] nx, int i) {
		try {
			URI u = new URI(nx[i].toString());
			if (_hosts.contains(u.getHost())) {
				super.addABox(nx, i);
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
