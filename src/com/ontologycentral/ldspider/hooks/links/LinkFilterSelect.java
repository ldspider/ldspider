package com.ontologycentral.ldspider.hooks.links;

import java.util.List;
import java.util.logging.Logger;

import org.semanticweb.yars.nx.Node;

import com.ontologycentral.ldspider.hooks.error.ErrorHandler;

public class LinkFilterSelect extends LinkFilterDefault{
	Logger _log = Logger.getLogger(this.getClass().getName());

	private List<Node> _predicates;
	private boolean _isAllowRule;

	public LinkFilterSelect(List<Node> linkPredicates, boolean isAllowRule, ErrorHandler eh) {
		super(eh);
		_predicates =linkPredicates;
		_log.info("link predicate is " + linkPredicates);
		_isAllowRule = isAllowRule;
	}

	public void startDocument() {
		;
	}
	
	public void endDocument() {
		;
	}

	/**
	 *  isAllowed predMatch Crawl
	 *      T	  T	  T
	 *      T	  F	  F
	 *      F	  T       F
	 *      F         F       T
	 */
	public void processStatement(Node[] nx) {

		if(_predicates.size()==0) {
			//no predicates specified
			super.processStatement(nx);
			_log.info("Select link from "+nx[1]);
		}
		
		if(_isAllowRule == _predicates.contains(nx[1])){
			super.processStatement(nx);
			_log.info("Select link from "+nx[1]);
		} else {
			return;
		}


	}
}
