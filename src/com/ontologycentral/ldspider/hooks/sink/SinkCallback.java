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
	private boolean _includeProvenance;
	
	public SinkCallback(Callback callback) {
		_callback = callback;
		_includeProvenance = true;
	}
	
	public SinkCallback(Callback callback, boolean includeProvenance) {
		_callback = callback;
		_includeProvenance = includeProvenance;
	}
	
	public Callback newDataset(Provenance prov) {
		if(_includeProvenance) {
			Headers.processHeaders(prov.getUri(), prov.getHttpStatus(), prov.getHttpHeaders(), _callback);
		}
		return _callback;
	}
}
