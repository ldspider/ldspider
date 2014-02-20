package com.ontologycentral.ldspider.hooks.sink.filter;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.yars.nx.Literal;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.parser.Callback;
import org.semanticweb.yars.util.CallbackCount;
import org.apache.http.Header;

import com.ontologycentral.ldspider.hooks.sink.Provenance;
import com.ontologycentral.ldspider.hooks.sink.Sink;
import com.ontologycentral.ldspider.hooks.sink.SinkCallback;
import junit.framework.TestCase;

public class FilterSinkPredicateTest extends TestCase {

	public void test() throws Exception {
		String[] predicates = new String[] { "http://xmlns.com/foaf/0.1/name", "http://xmlns.com/foaf/0.1/homepage" };

		Node[] statement = new Node[] { new Resource("http://example.com/JohnDoe"), new Resource("http://xmlns.com/foaf/0.1/name"), new Literal("John Doe") };
		assertTrue(doFilter(statement, predicates));

		statement[1] = new Resource("http://dbpedia.org/resource/name");
		assertFalse(doFilter(statement, predicates));

		statement[1] = new Resource("http://xmlns.com/foaf/0.1/nick");
		assertFalse(doFilter(statement, predicates));
	}

	/**
	 * Applies the filter sink to a specific statement.
	 */
	private boolean doFilter(Node[] statement, String[] predicates) {

		// Convert predicates array to a set of nodes
		Set<Node> predicateNodes = new HashSet<Node>();
		for (String predicate : predicates) {
			predicateNodes.add(new Resource(predicate));
		}

		// Create a predicate filter sink which writes to a counting callback
		CallbackCount countingCallback = new CallbackCount();
		Sink sink = new FilterSinkPredicate(new SinkCallback(countingCallback, false), predicateNodes);

		// Write statement using dummy provenance
		Callback outputCallback = sink.newDataset(new Provenance(URI.create("http://dummy.com"), new Header[0], 200));
		outputCallback.processStatement(statement);

		// Check if the statement has been written
		return countingCallback.getStmts() != 0;
	}
}
