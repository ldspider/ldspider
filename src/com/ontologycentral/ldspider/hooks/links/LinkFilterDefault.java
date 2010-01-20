package com.ontologycentral.ldspider.hooks.links;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Resource;

import com.ontologycentral.ldspider.frontier.Frontier;
import com.ontologycentral.ldspider.hooks.error.ErrorHandler;

/**
 * Follow all links (subject, predicate, object).
 * 
 * @author aharth
 *
 */
public class LinkFilterDefault implements LinkFilter {
	Logger _log = Logger.getLogger(this.getClass().getSimpleName());

	Frontier _f;
	ErrorHandler _eh;
	
	public LinkFilterDefault(ErrorHandler eh) {
		_eh = eh;
	}
	
	public void setFrontier(Frontier f) {
		_f = f;
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
					_f.add(u);
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
