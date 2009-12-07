package com.ontologycentral.ldquery;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.semanticweb.yars.util.CallbackSet;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import com.hp.hpl.jena.sparql.engine.main.StageGenerator;
import com.ontologycentral.ldquery.bgp.BGPMatcher;
import com.ontologycentral.ldspider.Crawler;
import com.ontologycentral.ldspider.Main;
import com.ontologycentral.ldspider.hooks.fetch.FetchFilterDeny;

public class StageGeneratorHTTP implements StageGenerator {
	private final static Logger _log = Logger.getLogger(Main.class.getSimpleName());

	Crawler _c;
	
	public StageGeneratorHTTP(Crawler c) {
		_c = c;
	}
	
	public QueryIterator execute(BasicPattern bgp, QueryIterator qi, ExecutionContext ec) {
		_log.info("evaluating " + bgp);
		
		List<URI> seed = new ArrayList<URI>();
		
		Triple t = bgp.get(0);
		if (t.getSubject().isURI()) {
			Node s = t.getSubject();
			URI u;
			try {
				u = new URI(s.getURI());
			} catch (URISyntaxException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
			seed.add(u);
		} else {
			throw new UnsupportedOperationException("only allows for bgp's with subject specified");
		}

		CallbackSet cb = new CallbackSet();
		_c.setOutputCallback(cb);
		_c.setFetchFilter(new FetchFilterDeny());
		_c.evaluate(seed, QueryEngineConstants.QE_HOPS, QueryEngineConstants.QE_THREADS);

		BGPMatcher bgpm = new BGPMatcher(bgp);
		List<Binding> results = bgpm.filter(cb.getSet());
		
		return new QueryIterPlainWrapper(results.iterator(), ec) ;
	}
}