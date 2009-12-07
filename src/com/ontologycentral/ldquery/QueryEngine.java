package com.ontologycentral.ldquery;

import java.util.Iterator;

import org.semanticweb.yars.nx.parser.Callback;

import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.engine.main.StageBuilder;
import com.ontologycentral.ldspider.Crawler;
import com.ontologycentral.ldspider.hooks.error.ErrorHandler;

public class QueryEngine {
	Callback _output;
	ErrorHandler _eh;
	Crawler _c;

	public QueryEngine() {
		_c = new Crawler();
	}
	
	public void setErrorHandler(ErrorHandler eh) {
		_eh = eh;
		_c.setErrorHandler(eh);
	}
	
	public void setOutputCallback(Callback cb) {
		_output = cb;
	}
	
	public void evaluate(String queryString) {
		Query query = QueryFactory.create(queryString);
		
		StageGeneratorHTTP msg = new StageGeneratorHTTP(_c);
		
		StageBuilder.setGenerator(ARQ.getContext(), msg) ;

		QueryExecution engine = QueryExecutionFactory.create(query, ModelFactory.createDefaultModel()) ;

		try {
		    Iterator<QuerySolution> results = engine.execSelect() ;
		    for ( ; results.hasNext() ; ) {
		        QuerySolution soln = results.next() ;
		        System.out.println(soln);
		    }
		} finally {
			engine.close() ;
		}

	}
}
