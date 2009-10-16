package com.ontologycentral.ldspider.hooks.links;

import java.util.List;

import org.semanticweb.yars.nx.Node;

import com.ontologycentral.ldspider.hooks.error.ErrorHandler;

public class LinkFilterSelect extends LinkFilterDefault{
	
	private List<Node> _predicates;
	private boolean _isAllowRule;

	public LinkFilterSelect(List<Node> linkPredicates, boolean isAllowRule, ErrorHandler eh) {
		super(eh);
		_predicates =linkPredicates;
		System.err.println(linkPredicates);
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
	    
	    if(_predicates.size()==0){
		//no predicates specified
		super.processStatement(nx);
		System.out.println("Select link from "+nx[1]);
	    }
	    if(_isAllowRule == _predicates.contains(nx[1])){
		super.processStatement(nx);
		System.out.println("Select link from "+nx[1]);
	    }
	    else{
		return;
	    }
	    
	    
	}
	
	public static void main(String[] args) {
	    System.out.println(true == true);
	    System.out.println(true == false);
	    System.out.println(false == false);
	}
}
