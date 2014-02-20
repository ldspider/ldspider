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
	private Callback _headerCallback = null;

	private boolean _includeProvenance;
	
	
	/**
	 * Constructs a new SinkCallback which does include provenance.
	 * 
	 * @param callback The NxParser callback to receive the statements.
	 */
	public SinkCallback(Callback callback) {
		this (callback, true);
	}
	
	/**
	 * Constructs a new SinkCallback which optionally includes provenance.
	 * 
	 * @param callback The NxParser callback to receive the statements.
	 * @param includeProvenance If true, provenance information will be included in the output. 
	 */
	public SinkCallback(Callback callback, boolean includeProvenance) {
		this(callback, includeProvenance, null);
	}

	public SinkCallback(Callback callback, boolean includeProvenance,
			Callback headerCallback) {
		_callback = callback;
		_includeProvenance = includeProvenance;
		_headerCallback = headerCallback;
	}
	
	public Callback newDataset(Provenance prov) {
		if (_includeProvenance)
			Headers.processHeaders(prov.getUri(), prov.getHttpStatus(), prov
					.getHttpHeaders(), _headerCallback == null ? _callback
					: _headerCallback);

		return _callback;
	}
}
