package com.ontologycentral.ldspider;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.util.Set;

import junit.framework.TestCase;

public class SeedReadTest extends TestCase {
	public void testSeed() throws FileNotFoundException {
		File f = new File("btc/seeds.txt");
		
		Set<URI> s = Main.readSeeds(f);
		
		System.out.println(s.size());
	}
}
