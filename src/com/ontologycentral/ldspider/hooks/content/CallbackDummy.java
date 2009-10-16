package com.ontologycentral.ldspider.hooks.content;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.Callback;

/**
 * Do nothing
 * 
 * @author aharth
 *
 */
public class CallbackDummy implements Callback {
	public void startDocument() {
		;
	}
	
	public void endDocument() {
		;
	}

	public void processStatement(Node[] nx) {
		;
	}
}
