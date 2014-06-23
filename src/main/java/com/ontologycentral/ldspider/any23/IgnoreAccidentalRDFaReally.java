/*
 * Based on the Apache Any23 project's class
 * org.apache.any23.filter.IgnoreAccidentalRDFa which is licensed under
 * the Apache License, see http://www.apache.org/licenses/LICENSE-2.0 .
 */

// Copyright notice from the original class:

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ontologycentral.ldspider.any23;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.any23.extractor.ExtractionContext;
import org.apache.any23.extractor.rdfa.RDFa11ExtractorFactory;
import org.apache.any23.extractor.rdfa.RDFaExtractorFactory;
import org.apache.any23.filter.ExtractionContextBlocker;
import org.apache.any23.vocab.XHTML;
import org.apache.any23.writer.TripleHandler;
import org.apache.any23.writer.TripleHandlerException;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

/**
 * A {@link TripleHandler} that suppresses output of the RDFa parser if the
 * document only contains "accidental" RDFa, like stylesheet links and other
 * non-RDFa uses of HTML's
 * 
 * @author Richard Cyganiak (richard@cyganiak.de)
 * @author Tobias Kaefer
 */
public class IgnoreAccidentalRDFaReally implements TripleHandler {

	private static final XHTML vXHTML = XHTML.getInstance();

	/**
	 * Non-RDF values in HTML for &lt;link rel="..."&gt; or &lt;a rel="..."&gt;
	 * . Extracted on 2014-03-04 from
	 * http://microformats.org/wiki/existing-rel-values, excluding sections
	 * "brainstorming", "rejected", "non-HTML rel values". Plus "shortcut", see
	 * http://de.wikipedia.org/wiki/Favicon .
	 */
	private static final Set<String> nonRDFrelValues = new HashSet<String>(
			Arrays.asList(new String[] { "acquaintance", "alternate",
					"appendix", "bookmark", "chapter", "child", "colleague",
					"contact", "contents", "copyright", "co-resident",
					"co-worker", "crush", "date", "friend", "glossary", "help",
					"its-rules", "kin", "license", "me", "met", "muse",
					"neighbor", "next", "nofollow", "parent", "prev",
					"previous", "section", "sibling", "spouse", "start",
					"stylesheet", "subsection", "sweetheart", "tag", "toc",
					"transformation", "apple-touch-icon",
					"apple-touch-icon-precomposed",
					"apple-touch-startup-image", "attachment", "canonical",
					"category", "component", "chrome-webstore-item",
					"disclosure", "discussion", "dns-prefetch", "EditURI",
					"entry-content", "external", "home", "hub", "in-reply-to",
					"index", "indieauth", "issues", "lightbox", "meta",
					"openid.delegate", "openid.server", "openid2.local_id",
					"openid2.provider", "p3pv1", "pgpkey", "pingback",
					"prerender", "profile", "rendition", "service",
					"shortlink", "sidebar", "sitemap", "subresource",
					"syndication", "timesheet", "webmention", "widget",
					"wlwmanifest", "image_src",
					"http://docs.oasis-open.org/ns/cmis/link/200908/acl",
					"stylesheet/less", "archive", "archives", "author",
					"canonical", "comment", "contribution", "EditURI",
					"endorsed", "fan", "feed", "footnote", "icon",
					"kinetic-stylesheet", "lightbox", /*
													 * "lightbox[group_name]",
													 * // is treated otherwise
													 */
					"prettyPhoto", "clearbox", "made", "meta", "microsummary",
					"noreferrer", "openid.delegate", "openid.server",
					"permalink", "pgpkey", "pingback", "popover", "prefetch",
					"publickey", "publisher", "referral", "related", "replies",
					"resource", "search", "sitemap", "sponsor", "tooltip",
					"trackback", "unendorsed", "user", "wlwmanifest", "banner",
					"begin", "biblioentry", "bibliography", "child",
					"citation", "collection", "definition", "disclaimer",
					"editor", "end", "footnote", "navigate", "origin",
					"parent", "pointer", "publisher", "sibling", "top",
					"trademark", "translation", "urc", "first", "index",
					"last", "up", "pronunciation", "directory", "enclosure",
					"home", "payment", "shortcut" }));

	private final ExtractionContextBlocker blocker;

	private final boolean alwaysSuppressCSSTriples;

	/**
	 * Constructor.
	 * 
	 * @param wrapped
	 *            the decorated triple handler.
	 * @param alwaysSuppressCSSTriples
	 *            if <code>true</code> the <i>CSS</i> triples will be always
	 *            suppressed even if the document is not empty. If
	 *            <code>false</code> then the <i>CSS</i> triples will be
	 *            suppressed only if document is empty.
	 */
	public IgnoreAccidentalRDFaReally(TripleHandler wrapped,
			boolean alwaysSuppressCSSTriples) {
		this.blocker = new ExtractionContextBlocker(wrapped);
		this.alwaysSuppressCSSTriples = alwaysSuppressCSSTriples;
	}

	public IgnoreAccidentalRDFaReally(TripleHandler wrapped) {
		this(wrapped, false);
	}

	public void startDocument(URI documentURI) throws TripleHandlerException {
		blocker.startDocument(documentURI);
	}

	public void openContext(ExtractionContext context)
			throws TripleHandlerException {
		blocker.openContext(context);
		if (isRDFaContext(context)) {
			blocker.blockContext(context);
		}
	}

	public void receiveTriple(Resource s, URI p, Value o, URI g,
			ExtractionContext context) throws TripleHandlerException {
		// Suppress stylesheet triples.
		if (alwaysSuppressCSSTriples
				&& p.stringValue().equals(vXHTML.stylesheet.stringValue())) {
			return;
		}
		if (isRDFaContext(context)
				&& !(p.stringValue().startsWith(XHTML.NS) || isDocumentURIplusSomeText(
						p, context.getDocumentURI()))) {
			blocker.unblockContext(context);
		}
		if (isRDFaContext(context)
				&& isDocumentURIplusSomeText(p, context.getDocumentURI()))
			return;
		blocker.receiveTriple(s, p, o, g, context);
	}

	public void receiveNamespace(String prefix, String uri,
			ExtractionContext context) throws TripleHandlerException {
		blocker.receiveNamespace(prefix, uri, context);
	}

	public void closeContext(ExtractionContext context) {
		blocker.closeContext(context);
	}

	public void close() throws TripleHandlerException {
		blocker.close();
	}

	private boolean isRDFaContext(ExtractionContext context) {
		return context.getExtractorName().equals(RDFaExtractorFactory.NAME)
				|| context.getExtractorName().equals(
						RDFa11ExtractorFactory.NAME);
	}

	public void endDocument(URI documentURI) throws TripleHandlerException {
		blocker.endDocument(documentURI);
	}

	public void setContentLength(long contentLength) {
		// Ignore.
	}

	/**
	 * Tells if a URI is composed of another URI plus some well-known HTML
	 * rel-values, see {@link #nonRDFrelValues}. To deal with URIs like
	 * http://example.org/blashortcut derived from a document
	 * http://example.org/bla containing &lt;link rel="shortcut icon"&gt; in the
	 * HTML header.
	 * 
	 * Some RDFa parsers like pyRdfa drop them straight away, but not the one
	 * that is in any23 at the moment (as of any23 version 0.9.0).
	 * 
	 * @param u
	 *            the URI
	 * @param document
	 *            the other URI
	 * @return true if the URI is composed of the other URI plus some well-known
	 *         HTML rel-values.
	 */
	public static boolean isDocumentURIplusSomeText(URI u, URI document) {
		String uString = u.stringValue();
		String docString = document.stringValue();
		if (!uString.startsWith(docString))
			return false;
		else if (uString.length() >= docString.length()) {
			String uURIbeyondDocumentURI = uString.substring(
					docString.length(), uString.length());
			if ((nonRDFrelValues.contains(uURIbeyondDocumentURI) || uURIbeyondDocumentURI
					.startsWith("lightbox[")))
				return true;
		}
		return false;
	}
}
