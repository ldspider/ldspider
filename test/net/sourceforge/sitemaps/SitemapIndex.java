/**
 * Sitemap.java - Represents a Sitemap Index (http://www.sitemaps.org/)
 * 
 * Copyright 2009 Frank McCown
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sourceforge.sitemaps;

import java.net.URL;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map.Entry;

/**
 * The SitemapIndex represents a Sitemap Index (collection of Sitemap URLs).
 * @author fmccown
 *
 */
public class SitemapIndex {

	/** Sitemap's URL */
	private URL url;
	
	/** URLs found in this Sitemap Index */
	private Hashtable<String,Sitemap> sitemaps;

	/**
	 * @param url - the URL of this Sitemap Index
	 */
	public void setUrl(URL url) {
		this.url = url;
	}

	/**
	 * @return the URL of this Sitemap Index
	 */
	public URL getUrl() {
		return url;
	}	
	
	/**
	 * @return a Collection of Sitemaps in this Sitemap Index.
	 */
	public Collection<Sitemap>getSitemapList() {
		return sitemaps.values();		
	}
	
	/** 
	 * @return the number of Sitemaps in the Sitemap Index.
	 */
	public int getSitemapListSize() {
		return sitemaps.size();
	}
	
	public SitemapIndex() {
		sitemaps = new Hashtable<String,Sitemap>();
	}
	
	public SitemapIndex(URL url) {
		this();
		setUrl(url);
	}
	
	/**
	 * Add this Sitemap to the list of Sitemaps,
	 * @param sitemap - Sitemap to be added to the list of Sitemaps
	 */
	public void addSitemap(Sitemap sitemap) {
		sitemaps.put(sitemap.getUrl().toString(), sitemap);
	}
	
	/**
	 * Returns the Sitemap that has the given URL. Returns null if the
	 * URL cannot be found.
	 * @param url - The Sitemap's URL
	 * @return
	 */
	public Sitemap getSitemap(URL url) {
		return sitemaps.get(url.toString());
	}
	
	/**
	 * Removes the Sitemap with this URL from the list of Sitemaps.
	 * @param url
	 */
	public void remoteSitemap(URL url) {
		sitemaps.remove(url.toString());
	}
	
	/**
	 * Removes this Sitemap (using it's URL) from the list of Sitemaps.
	 * @param sitemap
	 */
	public void removeSitemap(Sitemap sitemap) {
		sitemaps.remove(sitemap.getUrl().toString());
	}
	
	/**
	 * @return true if there are Sitemaps in this index that have not
	 * been processed yet, false otherwise.
	 */
	public boolean unprocessedSitemapsAvailable() {
		
		// Find an unprocessed Sitemap
		for (Entry<String, Sitemap> sitemap : sitemaps.entrySet()) {
			Sitemap s = sitemap.getValue();
			if (!s.isProcessed()) {				
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * @return an unprocessed Sitemap or null if no unprocessed Sitemaps
	 * could be found.
	 */
	public Sitemap getUnprocessedSitemap() {
		for (Entry<String, Sitemap> sitemap : sitemaps.entrySet()) {
			Sitemap s = sitemap.getValue();
			if (!s.isProcessed()) {
				return s;
			}
		}
		
		return null;
	}
	
	/**
	 * Free memory occupied by Sitemap's URL list. This list could contain
	 * 50K URLs, so if memory consumption is an issue, it's a good idea to 
	 * free each Sitemap's list before populating (processing) another. 
	 * @param s
	 */
	public void freeSitemap(Sitemap s) {
		if (s == null) return;
		
		Sitemap sitemap = sitemaps.get(s.getUrl().toString());
		if (sitemap != null) {
			sitemap.clearUrlList();
		}	
	}
	
	public String toString() {
		return "url=\"" + url + "\",sitemapListSize=" + sitemaps.size(); 
	}
}
