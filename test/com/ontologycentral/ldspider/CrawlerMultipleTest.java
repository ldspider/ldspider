package com.ontologycentral.ldspider;
import java.net.URI;

import org.semanticweb.yars.util.CallbackNQOutputStream;

import junit.framework.TestCase;

import com.ontologycentral.ldspider.frontier.BasicFrontier;
import com.ontologycentral.ldspider.frontier.Frontier;
import com.ontologycentral.ldspider.hooks.error.ErrorHandler;
import com.ontologycentral.ldspider.hooks.error.ErrorHandlerLogger;
import com.ontologycentral.ldspider.hooks.fetch.FetchFilter;
import com.ontologycentral.ldspider.hooks.fetch.FetchFilterRdfXml;
import com.ontologycentral.ldspider.hooks.fetch.FetchFilterSuffix;
import com.ontologycentral.ldspider.hooks.links.LinkFilterDummy;


public class CrawlerMultipleTest extends TestCase {
	public void testCrawl() throws Exception {
		Crawler c = new Crawler(CrawlerConstants.DEFAULT_NB_THREADS);

		ErrorHandler eh = new ErrorHandlerLogger(null, null);
		c.setErrorHandler(eh);
        c.setFetchFilter(new FetchFilterRdfXml());
        c.setLinkFilter(new LinkFilterDummy());
        c.setOutputCallback(new CallbackNQOutputStream(System.out));

		Frontier frontier = new BasicFrontier();
		frontier.add(new URI("http://harth.org/andreas/foaf.rdf"));
		frontier.add(new URI("http://dbpedia.org/resource/France"));

		c.evaluateLoadBalanced(frontier, 1);
		
		System.out.println("===============load balanced done====================");

		frontier = new BasicFrontier();
		frontier.add(new URI("http://harth.org/andreas/foaf.rdf"));
		frontier.add(new URI("http://umbrich.net/foaf.rdf"));
		frontier.add(new URI("http://dbpedia.org/resource/Germany"));

		FetchFilter BLACKLIST_FILTER = new FetchFilterSuffix(CrawlerConstants.BLACKLIST);
		c.setBlacklistFilter(BLACKLIST_FILTER);

		int breadthfirstdepth = 1;
		c.evaluateBreadthFirst(frontier, 1, CrawlerConstants.DEFAULT_NB_URIS,12);
	}
}

