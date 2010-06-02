package com.ontologycentral.ldspider;
import java.net.URI;

import junit.framework.TestCase;

import com.ontologycentral.ldspider.frontier.BasicFrontier;
import com.ontologycentral.ldspider.frontier.Frontier;
import com.ontologycentral.ldspider.hooks.error.ErrorHandler;
import com.ontologycentral.ldspider.hooks.error.ErrorHandlerLogger;
import com.ontologycentral.ldspider.hooks.fetch.FetchFilterRdfXml;
import com.ontologycentral.ldspider.hooks.links.LinkFilterDummy;


public class CrawlLoadBalancedTest extends TestCase {
	public void testCrawl1() throws Exception {
		Crawler c = new Crawler(1);

		Frontier frontier = new BasicFrontier();
		frontier.add(new URI("http://harth.org/andreas/foaf.rdf"));

		ErrorHandler eh = new ErrorHandlerLogger(null, null);
		c.setErrorHandler(eh);
	
		c.evaluateLoadBalanced(frontier, 1);
	}
	
	public void testCrawl2() throws Exception {
		Crawler c = new Crawler(1);

		Frontier frontier = new BasicFrontier();
		frontier.add(new URI("http://www.biz-nitch.com/index.rdf"));

		ErrorHandler eh = new ErrorHandlerLogger(null, null);
		c.setErrorHandler(eh);
	
		c.evaluateLoadBalanced(frontier, 1);
	}
	
	public void testCrawl3() throws Exception {
		Crawler c = new Crawler(1);

		Frontier frontier = new BasicFrontier();
		frontier.add(new URI("http://localhost:8888/company/facebook"));

		ErrorHandler eh = new ErrorHandlerLogger(null, null);
		c.setErrorHandler(eh);
	
		c.evaluateLoadBalanced(frontier, 1);
	}

}

