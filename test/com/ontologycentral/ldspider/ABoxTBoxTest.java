package com.ontologycentral.ldspider;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.URI;

import junit.framework.TestCase;

import org.semanticweb.yars.nx.parser.Callback;
import org.semanticweb.yars.util.CallbackNxOutputStream;
import org.semanticweb.yars.util.CallbackSet;
import org.semanticweb.yars.util.Callbacks;

import com.ontologycentral.ldspider.frontier.BasicFrontier;
import com.ontologycentral.ldspider.frontier.Frontier;
import com.ontologycentral.ldspider.hooks.error.ErrorHandler;
import com.ontologycentral.ldspider.hooks.error.ErrorHandlerLogger;
import com.ontologycentral.ldspider.hooks.links.LinkFilterDomain;
import com.ontologycentral.ldspider.hooks.sink.SinkCallback;

/**
 * Crawls the same seed URI with different settings for ABox/TBox crawling and whether to stay on the domain.
 * For each setting, it writes a output file.
 * 
 * @author RobertIsele
 */
public class ABoxTBoxTest extends TestCase {
	
	private URI uri = URI.create("http://dbpedia.org/resource/Steve_Jobs");
	
	public void testCrawl() throws Exception {
		//crawl(1, Crawler.Mode.ABOX_ONLY, false);

		crawl(0, Crawler.Mode.ABOX_AND_TBOX_EXTRAROUND, false);
	  
		//crawl(2, Crawler.Mode.ABOX_ONLY, false);
		//crawl(2, Crawler.Mode.ABOX_AND_TBOX, false);
		//crawl(2, Crawler.Mode.ABOX_ONLY, true);
		//crawl(2, Crawler.Mode.ABOX_AND_TBOX, true);
	}
 
	private void crawl(int rounds, Crawler.Mode crawlingMode, boolean stayOnDomain) throws IOException {	
		Crawler c = new Crawler();
	    
		//Frontier
		Frontier frontier = new BasicFrontier();
		frontier.add(uri);
	    
		//Link Filter
		if(stayOnDomain) {
			LinkFilterDomain linkFilter = new LinkFilterDomain(frontier);
			linkFilter.addHost(uri.getHost());
			c.setLinkFilter(linkFilter);
		}
	    
		//Output
		CallbackSet cb = new org.semanticweb.yars.util.CallbackSet();
		Callback cbFile = new CallbackNxOutputStream(new FileOutputStream("output_rounds=" + rounds + "_mode=" + crawlingMode + "_stay=" + stayOnDomain + ".nx"));
		c.setOutputCallback(new SinkCallback(new Callbacks(cb, cbFile)));
		
		PrintStream accesslog = new PrintStream(new FileOutputStream("/tmp/access.log"));
		
		ErrorHandler eh = new ErrorHandlerLogger(accesslog, null, false);
		c.setErrorHandler(eh);
		
		//Run
		c.evaluateBreadthFirst(frontier, rounds, 10000, 10000, crawlingMode);
		
		accesslog.close();
		
		System.out.println(eh);
	}
}
