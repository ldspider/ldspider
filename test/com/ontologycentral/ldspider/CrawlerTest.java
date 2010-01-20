package com.ontologycentral.ldspider;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import com.ontologycentral.ldspider.frontier.BasicFrontier;
import com.ontologycentral.ldspider.frontier.Frontier;
import com.ontologycentral.ldspider.hooks.error.ErrorHandler;
import com.ontologycentral.ldspider.hooks.error.ErrorHandlerLogger;


public class CrawlerTest extends TestCase {
	public void testCrawl() throws Exception {
		Crawler c = new Crawler(1);

		Frontier frontier = new BasicFrontier();
		frontier.add(new URI("http://harth.org/andreas/foaf.rdf"));

		ErrorHandler eh = new ErrorHandlerLogger(null, null);
		c.setErrorHandler(eh);
	
		c.evaluate(frontier, 1);
	}
	
	public void testCrawl2() throws Exception {
		Crawler c = new Crawler(1);

		Frontier frontier = new BasicFrontier();
		frontier.add(new URI("http://harth.org/andreas/foaf.rdf"));

		ErrorHandler eh = new ErrorHandlerLogger(null, null);
		c.setErrorHandler(eh);

		c.evaluate(frontier, 0);

		frontier = new BasicFrontier();
		frontier.add(new URI("http://umbrich.net/foaf.rdf"));

		c.evaluate(frontier, 0);
	}
}

