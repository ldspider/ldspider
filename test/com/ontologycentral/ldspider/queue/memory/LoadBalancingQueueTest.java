package com.ontologycentral.ldspider.queue.memory;

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

public class LoadBalancingQueueTest extends TestCase {
	public void testPoll() throws Exception {
		long time = System.currentTimeMillis();

		TldManager tldm = new TldManager();
		
		LoadBalancingQueue fq = new LoadBalancingQueue(tldm);
		
		fq.setMinDelay(0);
		fq.setMaxDelay(Integer.MAX_VALUE);
		
		InputStream is = new GZIPInputStream(new FileInputStream("test/uris.txt.gz"));
		
		BufferedReader br = new BufferedReader(new InputStreamReader(is));

		int i = 0;
		
		Frontier f = new RankedFrontier(null);
		
		String line = br.readLine();
		while (line != null) {
			i++;

			URI u = new URI(line);
			
			f.add(u);
			
			line = br.readLine();
		}
		
		br.close();
		
		fq.schedule(f);
		
		//System.out.println(fq);
		
		URI u = fq.poll();
		
		int j = 0;
		
		while (u != null) {
			j++;
			u = fq.poll();
		}
		
		System.out.println("read " + i + " lines, polled " + j + " uris");
	}
}
