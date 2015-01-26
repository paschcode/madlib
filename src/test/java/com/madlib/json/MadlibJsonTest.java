package com.madlib.json;

import org.junit.Test;

public class MadlibJsonTest 
{
	private static String getFile(String fileName) {
		return "src/test/resources/" + fileName;
	}
	
	@Test
	public void successTest() {
		System.out.println("\nSuccess test...");
		MadLibJson.getInstance(getFile("words.json")
								, getFile("phrases.txt")
								, getFile("output.txt")).createMadLib();
		 // TODO: Validate number of types and number elements in list type
	}
	
	@Test // tests IOException
	public void missingJsonFileTest() {
		System.out.println("\n\nMissing JSON file test...");
		 MadLibJson.getInstance(getFile("missing-file.json")
					, getFile("phrases.txt")
					, getFile("output.txt")).createMadLib();
	}
	
	@Test // tests IllegalStateException 
	public void missingTypeKeyTest() {
		System.out.println("\n\nMissing type key test...");
		MadLibJson.getInstance(getFile("words-missing-type-key.json")
				, getFile("phrases.txt")
				, getFile("output.txt")).createMadLib();
	}
	
	@Test // tests IllegalStateException 
	public void missingWordKeyTest() {
		System.out.println("\n\nMissing word key test...");
		MadLibJson.getInstance(getFile("words-missing-word-key.json")
				, getFile("phrases.txt")
				, getFile("output.txt")).createMadLib();
	}
	
	@Test // tests JsonParsingException 
	public void invalidStructureTest() {
		System.out.println("\n\nInvalid structure test...");
		MadLibJson.getInstance(getFile("words-invalid-structure.json")
				, getFile("phrases.txt")
				, getFile("output.txt")).createMadLib();
	}
	
	@Test // tests IllegalArgumentException 
	public void unknownTypeTest() {
		System.out.println("\n\nUnknown type test...");
		MadLibJson.getInstance(getFile("words-unknown-type.json")
				, getFile("phrases.txt")
				, getFile("output.txt")).createMadLib();
	}
}
