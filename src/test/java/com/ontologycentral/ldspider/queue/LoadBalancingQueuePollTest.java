package com.ontologycentral.ldspider.queue;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.zip.GZIPInputStream;

import junit.framework.TestCase;

import com.ontologycentral.ldspider.frontier.Frontier;
import com.ontologycentral.ldspider.frontier.RankedFrontier;
import com.ontologycentral.ldspider.tld.TldManager;

public class LoadBalancingQueuePollTest extends TestCase {
	public void testNormalise() throws Exception {
		TldManager tldm = new TldManager();
		
		SpiderQueue fq = new LoadBalancingQueue(tldm);
		
		InputStream is = new GZIPInputStream(new FileInputStream("test/uris.txt.gz"));
		
		BufferedReader br = new BufferedReader(new InputStreamReader(is));

		int i = 0;
		
		Frontier f = new RankedFrontier();
		
		String line = br.readLine();
		while (line != null) {
			i++;

			URI u = new URI(line);
			
			f.add(u);
			
			line = br.readLine();
		}
		
		br.close();
		
		fq.schedule(f);
		
		System.out.println(fq);
	}
}
