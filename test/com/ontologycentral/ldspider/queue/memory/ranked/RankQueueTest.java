package com.ontologycentral.ldspider.queue.memory.ranked;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Random;
import java.util.zip.GZIPInputStream;

import junit.framework.TestCase;

import com.ontologycentral.ldspider.queue.SpiderQueue;
import com.ontologycentral.ldspider.tld.TldManager;

public class RankQueueTest extends TestCase {
	public void testPoll() throws Exception {
		long time = System.currentTimeMillis();

		TldManager tldm = new TldManager();
		
		RankQueue fq = new RankQueue(tldm);
		
		fq.setMinDelay(0);
		fq.setMaxDelay(Integer.MAX_VALUE);
		
		InputStream is = new GZIPInputStream(new FileInputStream("test/uris.txt.gz"));
		
		BufferedReader br = new BufferedReader(new InputStreamReader(is));

		int i = 0;
		
		String line = br.readLine();
		while (line != null) {
			i++;

			URI u = new URI(line);
			
			fq.addFrontier(u);
			
			line = br.readLine();
		}
		
		br.close();
		
		fq.schedule();
		
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