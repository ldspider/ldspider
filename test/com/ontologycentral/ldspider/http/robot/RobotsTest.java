package com.ontologycentral.ldspider.http.robot;

import java.net.MalformedURLException;
import java.net.URL;

import junit.framework.TestCase;

import org.apache.http.util.EntityUtils;
import org.osjava.norbert.NoRobotClient;
import org.osjava.norbert.NoRobotException;

import com.ontologycentral.ldspider.CrawlerConstants;
import com.ontologycentral.ldspider.hooks.error.ErrorHandler;
import com.ontologycentral.ldspider.hooks.error.ErrorHandlerLogger;
import com.ontologycentral.ldspider.http.ConnectionManager;

public class RobotsTest extends TestCase {
//	public void testRobots() throws Exception {
//		ErrorHandler eh = new ErrorHandlerLogger(System.out, null);
//		ConnectionManager cm = new ConnectionManager(null, 0, null, null, 10);
//
//		Robot r = new Robot(cm, eh, "lj.rossia.org");
//
//		URL u = new URL("http://lj.rossia.org/interests.bml?int");
//		
//		System.out.println(r.isUrlAllowed(u));
//	}
//	
	public void testRobots2() throws Exception {
		NoRobotClient nrc = new NoRobotClient(CrawlerConstants.USERAGENT_NAME);
		String content = "User-Agent: ldspider\nDisallow: /\n";
		System.out.println(content);
		
		nrc.parse(content, new URL("http://example.org/"));

		System.out.println(nrc);
		
		URL u = new URL("http://example.org/interests");
		
		System.out.println(nrc.isUrlAllowed(u));
	}
	
	public void testRobots3(){
		NoRobotClient nrc = new NoRobotClient(CrawlerConstants.USERAGENT_NAME);
		try {
			nrc.parse(new URL("http://www.bbc.co.uk/"));
			System.err.println(nrc.isUrlAllowed(new URL("http://www.bbc.co.uk/programmes/p002hb5m")));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoRobotException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
}
