package com.ontologycentral.ldspider;

import java.net.URI;

import junit.framework.TestCase;

import org.semanticweb.yars.util.CallbackSet;

import com.ontologycentral.ldspider.frontier.BasicFrontier;
import com.ontologycentral.ldspider.frontier.Frontier;
import com.ontologycentral.ldspider.hooks.fetch.FetchFilter;
import com.ontologycentral.ldspider.hooks.fetch.FetchFilterSuffix;

public class DBpediaTest extends TestCase {
	public void testCrawl() throws Exception {
		Frontier frontier = new BasicFrontier();
		
	  
	    frontier.add(new URI("http://dbpedia.org/resource/France"));

	    CallbackSet cb = new org.semanticweb.yars.util.CallbackSet();
	  
	    Crawler c = new Crawler(1);
	    c.setOutputCallback(cb);

		FetchFilter BLACKLIST_FILTER = new FetchFilterSuffix(CrawlerConstants.BLACKLIST);
		c.setBlacklistFilter(BLACKLIST_FILTER);
		
	    c.evaluateBreadthFirst(frontier, 1, -1,-1);

	    System.out.println(cb.getSet().size());
	    //    cb.getSet.foreach(nodes => println(nodes.mkString(" ")))
	    
	    frontier = new BasicFrontier();
	     frontier.add(new URI("http://dbpedia.org/resource/Germany"));

	    cb = new org.semanticweb.yars.util.CallbackSet();
	    
	    //c = new Crawler(1);

	    c.setOutputCallback(cb);

	    c.evaluateBreadthFirst(frontier, 1, -1,-1);
	    
	    System.out.println(cb.getSet().size());
	}
}

