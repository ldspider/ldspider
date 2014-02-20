package com.ontologycentral.ldspider.hooks.sink;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.Callback;

/**
 * A dummy sink which discards all statements.
 * 
 * @author RobertIsele
 */
public class SinkDummy implements Sink {
	
	private static final Callback callback = new CallbackDummy();
	
	public Callback newDataset(Provenance provenance) {
		return callback;
	}
	
	public Callback newDataset(Provenance provenance, boolean header) {
		return callback;
	}
	

	public void close() {
		;
	}

	/**
	 * Do nothing
	 * 
	 * @author aharth
	 */
	private static class CallbackDummy implements Callback {
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
}
