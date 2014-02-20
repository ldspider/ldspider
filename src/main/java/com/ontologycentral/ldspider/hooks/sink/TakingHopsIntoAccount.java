package com.ontologycentral.ldspider.hooks.sink;

import java.io.IOException;

/**
 * In Breadth-first style crawling, sometimes it is important to know when the
 * crawling of one hop has finished, and the next hop starts. Those classes who
 * care about that should implement this interface.
 * 
 * @author Tobias Kaefer
 * 
 */
public interface TakingHopsIntoAccount {

	/**
	 * Off to the next hop. In most implementation, this calls
	 * {@link #nextHop(int)} with an updated counter.
	 */
	public void nextHop() throws Exception;

	/**
	 * Off to the next hop, with the number of the next hop supplied.
	 * 
	 * @param hopnumber
	 *            the number of the next hop.
	 * @throws Exception 
	 */
	public void nextHop(int hopnumber) throws Exception;

}
