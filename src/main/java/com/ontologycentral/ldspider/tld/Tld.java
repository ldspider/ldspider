package com.ontologycentral.ldspider.tld;

import java.io.Serializable;
import java.util.ArrayList;

public class Tld implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private boolean hasOneLvlSffxes; // tld generally allows one-level suffixes e.g. ie, de
	private boolean hasTwoLvlSffxes; // tld generally allows two-level suffices e.g. co.uk
	
	// LISTS FOR STORING SPECIAL CASES
	
	// 2-level suffixes in addition to 1-level
	// e.g. com.fr (as well as .fr)
	private ArrayList<String> addlTwoLvlSffxes;
	
	// 3-level suffixes in addition to 1 or 2-level
	// e.g. act.edu.au (as well as *.au)
	private ArrayList<String> addlThreeLvlSffxes;
	
	// 3-level suffixes in addition to 1 or 2-level, but with wildcards
	// e.g. *.sch.uk
	private ArrayList<String> addlWildcardThreeLvlSffxes;
	
	// domains with 1-level suffixes in exception to 2 or 3-level
	// e.g. bl.uk has uk instead of *.uk
	private ArrayList<String> excptnlTwoLvlDomains;
	
	// domains with 2-level suffixes in exception to 3-level 
	// e.g. metro.tokyo.jp has tokyo.jp instead of *.tokyo.uk
	private ArrayList<String> excptnlThreeLvlDomains;
	
	public Tld(String tld) {
		addlTwoLvlSffxes = new ArrayList<String>();
		addlThreeLvlSffxes = new ArrayList<String>();
		addlWildcardThreeLvlSffxes  = new ArrayList<String>();
		excptnlTwoLvlDomains = new ArrayList<String>();
		excptnlThreeLvlDomains = new ArrayList<String>();
		hasOneLvlSffxes = false;
	}
	
	public void setHasOneLvlSffxes() {
		hasOneLvlSffxes = true;
	}
	
	public void setHasTwoLvlSffxes() {
		hasTwoLvlSffxes = true;
	}
	
	public void addAddlTwoLvlSffx(String s) {
		addlTwoLvlSffxes.add(s);
	}
	
	public void addAddlThreeLvlSffx(String s) {
		addlThreeLvlSffxes.add(s);
	}

	public void addExcptnlTwoLvlDomain(String s) {
		excptnlTwoLvlDomains.add(s);
	}
	
	public void addAddlWildcardThreeLvlSffx(String s) {
		addlWildcardThreeLvlSffxes.add(s);
	}

	public void addExcptnlThreeLvlDomain(String s) {
		excptnlThreeLvlDomains.add(s);
	}
	
	public boolean getHasOneLvlSffxes() {
		return hasOneLvlSffxes;
	}
	
	public boolean getHasTwoLvlSffxes() {
		return hasTwoLvlSffxes;
	}
	
	public ArrayList<String> getAddlTwoLvlSffxes() {
		return addlTwoLvlSffxes;
	}
	
	public ArrayList<String> getAddlThreeLvlSffxes() {
		return addlThreeLvlSffxes;
	}

	public ArrayList<String> getExcptnlTwoLvlDomains() {
		return excptnlTwoLvlDomains;
	}
	
	public ArrayList<String> getAddlWildcardThreeLvlSffxes() {
		return addlWildcardThreeLvlSffxes;
	}

	public ArrayList<String> getExcptnlThreeLvlDomains() {
		return excptnlThreeLvlDomains;
	}
}