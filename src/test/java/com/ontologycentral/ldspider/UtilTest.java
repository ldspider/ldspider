package com.ontologycentral.ldspider;

import java.io.BufferedReader;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import com.ontologycentral.ldspider.Util;

import junit.framework.TestCase;

public class UtilTest extends TestCase {
	public void testdetermineFnameAndExtension() {
		String sep = System.getProperty("file.separator");

		Map<String, String[]> fnamesAndGoldStandard = new HashMap<String, String[]>();

		fnamesAndGoldStandard.put("a" + sep + "b", new String[] {
				"a" + sep + "b", "" });
		fnamesAndGoldStandard.put("a" + sep + "b.", new String[] {
				"a" + sep + "b", "" });
		fnamesAndGoldStandard.put("a" + sep + "b.c", new String[] {
				"a" + sep + "b", "c" });
		fnamesAndGoldStandard.put("a.c" + sep + "b", new String[] {
				"a.c" + sep + "b", "" });
		fnamesAndGoldStandard.put("a.c" + sep + "b.c", new String[] {
				"a.c" + sep + "b", "c" });
		fnamesAndGoldStandard.put("a", new String[] { "a", "" });
		fnamesAndGoldStandard.put("a.c", new String[] { "a", "c" });
		fnamesAndGoldStandard.put("a.", new String[] { "a", "" });
		fnamesAndGoldStandard.put("a." + sep + "b", new String[] {
				"a." + sep + "b", "" });
		fnamesAndGoldStandard.put("a." + sep + "b.", new String[] {
				"a." + sep + "b", "" });
		fnamesAndGoldStandard.put("a." + sep + "b.c", new String[] {
				"a." + sep + "b", "c" });

		for (Entry<String, String[]> e : fnamesAndGoldStandard.entrySet()) {
			String[] fnExt = Util.determineFnameAndExtension(e.getKey());

			System.out.println(e.getKey() + " " + Arrays.toString(e.getValue())
					+ " " + Arrays.toString(fnExt));

			assertEquals(fnExt[0], e.getValue()[0]);
			assertEquals(fnExt[1], e.getValue()[1]);
		}
	}

	public void testLineIterator() {
		String[] parts = { "a", "b", "c" };

		StringBuilder sb = new StringBuilder();
		for (String s : parts) {
			sb.append(s);
			if (!s.equals("c"))
				sb.append("\n");
		}

		BufferedReader br = new BufferedReader(new StringReader(sb.toString()));

		Iterator<String> it = new Util.LineByLineIterable(br).iterator();
		
		assertTrue(it.hasNext());
		assertTrue(it.hasNext());
		assertTrue(it.hasNext());

		for (int i = 0; i < parts.length; ++i) {
			assertEquals(it.next(), parts[i]);
		}

		assertFalse(it.hasNext());

		try {
			it.next();
		} catch (NoSuchElementException e) {
			
		} catch (Exception e) {
			fail();
		}

		assertFalse(it.hasNext());
	}
	public void testStrURIIterator() throws URISyntaxException {
		String[] parts = { "http://example.org", "http/example.org", "http://example.org/2", "http/example.org" };

		StringBuilder sb = new StringBuilder();
		for (String s : parts) {
			sb.append(s);
			if (!s.equals("c"))
				sb.append("\n");
		}

		BufferedReader br = new BufferedReader(new StringReader(sb.toString()));

		Iterator<URI> it = new Util.StringToURIiterable(
				new Util.LineByLineIterable(br)).iterator();

		assertTrue(it.hasNext());
		assertTrue(it.hasNext());
		assertTrue(it.hasNext());

		assertEquals(it.next(), new URI(parts[0]));
		assertTrue(it.hasNext());
		assertTrue(it.hasNext());
		assertTrue(it.hasNext());
		assertEquals(it.next(), new URI(parts[2]));

		assertFalse(it.hasNext());
		assertFalse(it.hasNext());
		assertFalse(it.hasNext());

		try {
			it.next();
		} catch (NoSuchElementException e) {
			
		} catch (Exception e) {
			fail();
		}

		assertFalse(it.hasNext());
	}
}
