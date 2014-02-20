package com.ontologycentral.ldspider.hooks.sink.filter;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.Callback;

import com.ontologycentral.ldspider.hooks.sink.Provenance;
import com.ontologycentral.ldspider.hooks.sink.Sink;

/**
 * A Sink which filters statements based on a list of allowed predicates.
 * All allowed statements are passed to an output sink.
 * 
 * @author RobertIsele
 */
public class FilterSinkPredicate implements Sink {

	private final Sink _sink;
	private final Set<Node> _predicates;
	private final Logger _log = Logger.getLogger(getClass().getName());

	public FilterSinkPredicate(Sink sink, Set<Node> predicates) {
		_sink = sink;
		_predicates = new HashSet<Node>(predicates);
	}

	public Callback newDataset(Provenance provenance) {
		return new FilterCallback(_sink.newDataset(provenance));
	}

	private class FilterCallback implements Callback {

		private final Callback _callback;

		public FilterCallback(Callback callback) {
			_callback = callback;
		}

		public void startDocument() {
			_callback.startDocument();
		}

		public void endDocument() {
			_callback.endDocument();
		}

		public void processStatement(Node[] nodes) {
			if (nodes.length >= 1 && _predicates.contains(nodes[1])) {
				_log.fine("Allowing statement with predicate " + nodes[1]);
				_callback.processStatement(nodes);
			} else {
				_log.fine("Rejecting statement with predicate " + nodes[1]);
			}
		}
	}
}
