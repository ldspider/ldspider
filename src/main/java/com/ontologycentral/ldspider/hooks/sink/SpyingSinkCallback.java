package com.ontologycentral.ldspider.hooks.sink;

import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.parser.Callback;
import org.semanticweb.yars.util.Callbacks;

/**
 * A wrapping Sink that can report by whom the last statement was.
 * 
 * @author Tobias Käfer
 * 
 */
public class SpyingSinkCallback implements Sink, LastReporter {

	Sink _sink;

	LastReportingCallback _cb;

	Callbacks _cbs;
	Callback[] _bareCbs;

	/**
	 * Create a new SpyingSinkCallback.
	 * 
	 * @param sink
	 *            The Sink to be wrapped.
	 */
	public SpyingSinkCallback(Sink sink) {
		_sink = sink;
		_cb = new LastReportingCallback();
		_bareCbs = new Callback[2];
		_bareCbs[1] = _cb;
		_cbs = new Callbacks(_bareCbs);
	}

	public Callback newDataset(Provenance provenance) {
		_bareCbs[0] = _sink.newDataset(provenance);
		return _cbs;
	}

	public Resource whoWasLast() {
		return _cb.whoWasLast();
	}

}
