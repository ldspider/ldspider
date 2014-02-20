package com.ontologycentral.ldspider;
import java.net.URI;

import junit.framework.TestCase;

import com.ontologycentral.ldspider.frontier.BasicFrontier;
import com.ontologycentral.ldspider.frontier.Frontier;
import com.ontologycentral.ldspider.hooks.error.ErrorHandler;
import com.ontologycentral.ldspider.hooks.error.ErrorHandlerLogger;


public class CrawlerStatsTest extends TestCase {
	public void testCrawl() throws Exception {
		Crawler c = new Crawler(1);

		Frontier frontier = new BasicFrontier();
		frontier.add(new URI("http://harth.org/andreas/foaf.rdf"));

		ErrorHandler eh = new ErrorHandlerLogger(System.out, null);
		c.setErrorHandler(eh);

		c.evaluateBreadthFirst(frontier, 1, CrawlerConstants.DEFAULT_NB_URIS, 12);
	}
}

