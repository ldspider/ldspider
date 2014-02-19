package com.ontologycentral.ldspider.seen;

import java.net.URI;
import java.util.Collection;

/**
 * A class to represent the URIs already visited.
 * 
 * @author Tobias Kaefer
 * 
 */
public interface Seen {

	/**
	 * Check if a URI has already been seen.
	 * 
	 * @param u
	 *            the URI
	 * @return if it has already been seen
	 */
	public boolean hasBeenSeen(URI u);

	/**
	 * Adds a number of URIs to seen.
	 * 
	 * @param uris
	 *            the URIs
	 * @return if all the URIs supplied have not been seen
	 */
	public boolean add(Collection<URI> uris);

	/**
	 * Adds a URI to seen. Thus marking it as seen.
	 * 
	 * @param uri
	 *            the URI
	 * @return if the URI supplied has not been seen
	 */
	public boolean add(URI uri);

}
