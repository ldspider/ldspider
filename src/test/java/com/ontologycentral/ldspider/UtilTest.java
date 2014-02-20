package com.ontologycentral.ldspider;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

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
			String[] fnExt = Util
					.determineFnameAndExtension(e.getKey());

			System.out.println(e.getKey() + " " + Arrays.toString(e.getValue())
					+ " " + Arrays.toString(fnExt));

			assertEquals(fnExt[0], e.getValue()[0]);
			assertEquals(fnExt[1], e.getValue()[1]);
		}
	}
}
