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
 * @author RobertIsele
 *
 */
public class LinkFilterDefault implements LinkFilter {
	private final Logger _log = Logger.getLogger(this.getClass().getSimpleName());

	protected final Frontier _f;
	protected ErrorHandler _eh;
	
	protected boolean _followABox;
	protected boolean _followTBox;
	
	private static final String RDF_TYPE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
	
	public LinkFilterDefault(Frontier f) {
		_f = f;
		_followABox = true;
		_followTBox = true;
	}
	
	public void setErrorHandler(ErrorHandler eh) {
		_eh = eh;
	}
	
	public void setFollowABox(boolean follow) {
		_followABox = follow;
	}

	public void setFollowTBox(boolean follow) {
		_followTBox = follow;
	}

	public void startDocument() {
		;
	}
	
	public void endDocument() {
		;
	}

	public void processStatement(Node[] nx) {
		for (int i = 0; i < Math.min(nx.length, 3); i++) {
			if (nx[i] instanceof Resource) {
				//Subject
				if(i == 0 && _followABox) {
					if(_followABox) addABox(nx, i);
				}
				//Predicate
				else if(i == 1) {
					if(_followTBox) addTBox(nx, i);
				}
				//Object (TBox)
				else if(i == 2 && nx[1].toString().equals(RDF_TYPE)) {
					if(_followTBox) addTBox(nx, i);
				}
				//Object (ABox)
				else if(i == 2) {
					if(_followABox) addABox(nx, i);
				}
			}
		}
	}
	
	/**
	 * Adds a new ABox node.
	 * Override this in sub classes to modify ABox handling.
	 * 
	 */
	protected void addABox(Node[] nx, int i) {
		addUri(nx, i);
	}
	
	/**
	 * Adds a new TBox node.
	 * Override this in sub classes to modify TBox handling.
	 * 
	 */
	protected void addTBox(Node[] nx, int i) {
		addUri(nx, i);
	}
	
	/**
	 * Adds a new uri to the frontier.
	 */
	protected void addUri(Node[] nx, int i) {
		try {
			_f.add(new URI(nx[i].toString()));
			_log.fine("adding " + nx[i].toString() + " to frontier");
		} catch (URISyntaxException e) {
			try {
				_eh.handleError(new URI(nx[nx.length-1].toString()), e);
			} catch (URISyntaxException e1) {
				e1.printStackTrace();
			}
		}
	}
}
