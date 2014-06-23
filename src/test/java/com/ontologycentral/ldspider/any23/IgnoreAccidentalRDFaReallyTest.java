package com.ontologycentral.ldspider.any23;

import org.openrdf.model.impl.URIImpl;

import junit.framework.TestCase;
import com.ontologycentral.ldspider.any23.IgnoreAccidentalRDFaReally;

public class IgnoreAccidentalRDFaReallyTest extends TestCase {
	public static void testisHTMLlinkAttributeInDocument() {
		assertTrue(IgnoreAccidentalRDFaReally.isDocumentURIplusSomeText(
				new URIImpl("http://example.org/blanofollow"), new URIImpl(
						"http://example.org/bla")));
		assertTrue(IgnoreAccidentalRDFaReally.isDocumentURIplusSomeText(
				new URIImpl("http://example.org/blashortcut"), new URIImpl(
						"http://example.org/bla")));
		assertTrue(IgnoreAccidentalRDFaReally.isDocumentURIplusSomeText(
				new URIImpl("http://example.org/bla/nofollow"), new URIImpl(
						"http://example.org/bla/")));
		assertTrue(IgnoreAccidentalRDFaReally.isDocumentURIplusSomeText(
				new URIImpl("http://example.org/bla/nofollow"), new URIImpl(
						"http://example.org/bla/")));
		assertTrue(IgnoreAccidentalRDFaReally.isDocumentURIplusSomeText(
				new URIImpl("http://example.org/bla/lightbox[id-of-group]"), new URIImpl(
						"http://example.org/bla/")));
		assertFalse(IgnoreAccidentalRDFaReally.isDocumentURIplusSomeText(
				new URIImpl("http://example.org/bla#nofollow"), new URIImpl(
						"http://example.org/bla")));
	}
}
