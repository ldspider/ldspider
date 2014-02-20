package com.ontologycentral.ldspider.hooks.sink;

import org.semanticweb.yars.nx.Resource;

public interface LastReporter {
	/**
	 * Answers the question by whom the last statement was.
	 * 
	 * @return The culprit, or null if the last statement didn't end in a
	 *         {@link org.semanticweb.yars.nx.Resource Resource}.
	 */
	public Resource whoWasLast();
}
