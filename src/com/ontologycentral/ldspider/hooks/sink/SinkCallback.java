package com.ontologycentral.ldspider.hooks.sink;

import org.semanticweb.yars.nx.parser.Callback;
import com.ontologycentral.ldspider.http.Headers;

/**
 * A Sink which uses an NxParser Callback to write the statements.
 * 
 * @author RobertIsele
 */
public class SinkCallback implements Sink {

	private Callback _callback;
	
	public SinkCallback(Callback callback) {
		_callback = callback;
	}
	
	public Callback newDataset(Provenance prov) {
		Headers.processHeaders(prov.getUri(), prov.getHttpStatus(), prov.getHttpHeaders(), _callback);
		return _callback;
	}
}
