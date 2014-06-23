package com.ontologycentral.ldspider.any23;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.apache.any23.extractor.ExtractionContext;
import org.apache.any23.writer.TripleHandler;
import org.apache.any23.writer.TripleHandlerException;
import org.junit.Test;
import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.Callback;

public class CallbackNQuadTripleHandlerTest {

	@Test
	public void test() throws TripleHandlerException {
		Callback cb = new Callback() {
			public void startDocument() {
				;
			}

			public void endDocument() {
				;
			}

			public void processStatement(Node[] nx) {
				System.out.println(Arrays.toString(nx));
				for (Node n : nx)
					System.out.println(n.toN3());
			}
		};

		TripleHandler th = new CallbackNQuadTripleHandler(cb);

		th.receiveTriple(new BNodeImpl("subjBnode"), new URIImpl(
				"http://blubb.de/prädikat"), new LiteralImpl("aaääßßá",
				new URIImpl("http://blöbb.de/dt")), null,
				new ExtractionContext("bla", new URIImpl("http://blübb.de/c")));

	}

}
