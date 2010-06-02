package com.ontologycentral.ldspider;

import java.net.URI;

import junit.framework.TestCase;

import org.semanticweb.yars.util.CallbackSet;

import com.ontologycentral.ldspider.frontier.BasicFrontier;
import com.ontologycentral.ldspider.frontier.Frontier;

public class DBpediaTest extends TestCase {
	public void testCrawl() throws Exception {
		Frontier frontier = new BasicFrontier();
		
	    frontier.setBlacklist(CrawlerConstants.BLACKLIST);
	    frontier.add(new URI("http://dbpedia.org/resource/France"));

	    CallbackSet cb = new org.semanticweb.yars.util.CallbackSet();
	  
	    Crawler c = new Crawler(1);
	    c.setOutputCallback(cb);

	    c.evaluateBreadthFirst(frontier, 1, -1);

	    System.out.println(cb.getSet().size());
	    //    cb.getSet.foreach(nodes => println(nodes.mkString(" ")))
	    
	    frontier = new BasicFrontier();
	    frontier.setBlacklist(CrawlerConstants.BLACKLIST);
	    frontier.add(new URI("http://dbpedia.org/resource/Germany"));

	    cb = new org.semanticweb.yars.util.CallbackSet();
	    
	    //c = new Crawler(1);

	    c.setOutputCallback(cb);

	    c.evaluateBreadthFirst(frontier, 1, -1);
	    
	    System.out.println(cb.getSet().size());
	}
}

