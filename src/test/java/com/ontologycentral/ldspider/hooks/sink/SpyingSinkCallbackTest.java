package com.ontologycentral.ldspider.hooks.sink;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

import org.apache.http.Header;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.parser.Callback;

public class SpyingSinkCallbackTest {

	public static void main(String[] args) throws URISyntaxException {
		SpyingSinkCallback s = new SpyingSinkCallback(new SinkCallback(
				new Callback() {
					public void startDocument() {
					}

					public void endDocument() {
					}

					public void processStatement(Node[] nx) {
						System.out.println(Arrays.toString(nx));
					}
				}));
		Header[] h = new Header[0];
		s.newDataset(new Provenance(new URI("http://blubb1.com"), h, 200));
		Callback cb = s.newDataset(new Provenance(new URI("http://blubb2.com"),
				h, 200));

		Node[] nodes = { new Resource("http://subj.com"),
				new Resource("http://pred.com"),
				new Resource("http://obj.com"),
				new Resource("http://context.com") };

		cb.processStatement(nodes);
		
		System.out.println(s.whoWasLast());

		nodes[3] = new Resource("http://context.com/2");

		cb.processStatement(nodes);

		System.out.println(s.whoWasLast());

	}

}
