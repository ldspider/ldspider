package com.ontologycentral.ldspider.hooks.content;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;

import org.semanticweb.yars.nx.parser.Callback;
import org.semanticweb.yars.util.CallbackCount;
import org.semanticweb.yars.util.CallbackNQOutputStream;
import org.semanticweb.yars.util.Callbacks;

import com.ontologycentral.ldspider.Crawler;
import com.ontologycentral.ldspider.CrawlerConstants;
import com.ontologycentral.ldspider.frontier.BasicFrontier;
import com.ontologycentral.ldspider.frontier.Frontier;
import com.ontologycentral.ldspider.hooks.sink.Sink;
import com.ontologycentral.ldspider.hooks.sink.SinkCallback;

import junit.framework.TestCase;

public class ContentHandlerAny23Test extends TestCase {

	private static final String seedUri = "http://www.ifi.uzh.ch/pax/index.php/publication/rdfalist";
	
	private static final String outputFile = "ContentHandlerAny23Test.nq";
	
	private static final boolean includeProvenance = false;
	
	private static final boolean includeABox = true;
	
	private static final boolean includeTBox = false;
	
	public void testCrawl() throws Exception {
	    Crawler c = new Crawler(1);
	    
	    //Frontier
	    Frontier frontier = new BasicFrontier();
	    frontier.setBlacklist(CrawlerConstants.BLACKLIST);
	    frontier.add(new URI(seedUri));
	    
	    //ContentHandler
	    ContentHandlerAny23 handler = new ContentHandlerAny23();
	    if(!handler.checkServer()) throw new IOException("Any23 server is not running");
	    c.setContentHandler(handler);

	    //Sink
	    Callback fileCb = new CallbackNQOutputStream(new FileOutputStream(outputFile));
	    CallbackCount countCb = new CallbackCount();
	    Sink sink = new SinkCallback(new Callbacks(new Callback[] { fileCb, countCb }), includeProvenance);
	    c.setOutputCallback(sink);
	    
	    //Crawl
	    c.evaluateBreadthFirst(frontier, 1, 10, includeABox, includeTBox);
	    System.out.println(countCb.getStmts() + " statements written to " + outputFile);
	}
}

