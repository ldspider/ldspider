package com.ontologycentral.ldspider.hooks.links;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.Iterator;

import junit.framework.TestCase;

import org.semanticweb.yars.nx.parser.NxParser;

import com.ontologycentral.ldspider.frontier.BasicFrontier;
import com.ontologycentral.ldspider.frontier.Frontier;

public class LinkFilterPrefixTest extends TestCase {
	
	public void test() throws Exception {
		drugbankTest();
	}
	
	private void drugbankTest() throws Exception {
		String prefix = "http://www4.wiwiss.fu-berlin.de/drugbank";

		String statements = 
			"<http://www4.wiwiss.fu-berlin.de/drugbank/resource/enzymes/1> " +
      "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type> " +
      "<http://www4.wiwiss.fu-berlin.de/drugbank/resource/enzymes> ." +
		  "\n" + 
			"<http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugs/DB00623> " +
			"<http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/enzyme> " +
			"<http://www4.wiwiss.fu-berlin.de/drugbank/resource/enzymes/1> ." +
	    "\n" + 
			"<http://www4.wiwiss.fu-berlin.de/drugbank/resource/enzymes/1> " +
			"<http://www4.wiwiss.fu-berlin.de/drugbank/resource/swissprotPage> " +
			"<http://www.uniprot.org/uniprot/P20815> .";

		//ABox
		assertEquals("Add all 3 unique ABox URIs", 3, count(statements, "http://", true, false));
		assertEquals("Add all 2 unique ABox URIs from Drugbank", 2, count(statements, prefix, true, false));
		assertEquals("Don't add ABox URIs with unknown prefixes", 0, count(statements, "http://UnknownPrefix", true, false));
		
		//TBox
		assertEquals("Add all 4 unique TBox URIs", 4, count(statements, prefix, false, true));
		assertEquals("Add all TBox URIs, even with unknown prefixes", 4, count(statements, "http://UnknownPrefix", false, true));
	}
	
	private int count(String statements, String prefix, boolean followABox, boolean followTBox) throws Exception {
		Frontier frontier = new BasicFrontier();
		LinkFilterPrefix filter = new LinkFilterPrefix(frontier);
		filter.setFollowABox(followABox);
		filter.setFollowTBox(followTBox);
		filter.addPrefix(prefix);
		
		NxParser parser = new NxParser(new ByteArrayInputStream(statements.getBytes()));
		
		//Process statements with filter
		while(parser.hasNext()) {
			filter.processStatement(parser.next());
		}
		
		//Count filtered URIs
		int c = 0;
		Iterator<URI> iter = frontier.iterator();
		while(iter.hasNext()) {
			iter.next();
			c++;
		}
		
		return c;
	}
}
