package com.madlib.json;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.json.Json;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParsingException;

/**
 * A madlib generator using a given json-formatted word file and story file.
 * Produces an output story file using randomly-generated words (from json
 * file).
 * <p>
 * Date: 24 Jan 2015
 * </p>
 * @author Todd Paschal
 * 
 */
public final class MadLibJson {
	
	// Member variables
	private static final String TYPE = "type";
	private static final String WORD = "word";
	private final String jsonFilePath;
	private final String storyFilePath;
	private final String outputFilePath;

	private final Map<Type, List<String>> jsonValues = new HashMap<Type, List<String>>();

	private enum Type {
		NUMBER, ADJECTIVE, ADVERB, PLACE, PERSON, NOUN, VERB;
	}
	
	public static MadLibJson getInstance(String jsonFilePath,
			String storyFilePath, String outputFilePath) {
		return new MadLibJson(jsonFilePath, storyFilePath, outputFilePath);
	}

	private MadLibJson(String jsonFilePath, String storyFilePath,
			String outputFilePath) {
		this.jsonFilePath = jsonFilePath;
		this.storyFilePath = storyFilePath;
		this.outputFilePath = outputFilePath;
	}

	public static void main(String[] args) {
		if (args == null || args.length != 3) {
			System.err
					.println("USAGE: java MadLib <JSON file path>,<Story File Path>,<Output File Path>\n"
							+ "All paths are absolute.");
			return;
		}
		// Do the madlib thing
		MadLibJson madlib = MadLibJson.getInstance(args[0], args[1], args[2]);
		madlib.createMadLib();
	}

	/**
	 * Creates the output file with random words inserted into the story file
	 */
	public void createMadLib() {
		Long lineCounter = 0L;
		try {
			// parse the JSON word and Story file
			parseJSON(lineCounter);
			// Populate the story file with random words by type
			populateStory();
		} catch (IllegalStateException ex) {
			System.out.format(
					"ERROR - JSON file %s is missing a name/value pair."
							+ " See line %d.", jsonFilePath, lineCounter + 1);
		} catch (JsonParsingException ex) {
			System.out.format(
					"ERROR - JSON file %s is not formatted correctly. %s.",
					jsonFilePath, ex.getMessage());
		} catch (IllegalArgumentException ex) {
			System.out.format(
					"ERROR - JSON file %s contains an unknown type. %s. "
					+ "See line %d.",
					jsonFilePath, ex.getMessage(), lineCounter +1);			
		} catch (FileNotFoundException e) {
			System.out.format(
					"ERROR - The file '%s', '%s', or '%s' could not be "
					+ "found. Please use an absolute path.", jsonFilePath, storyFilePath, outputFilePath);
		} catch (IOException e) {
			System.out.format(
					"ERROR - There was a problem reading the file '%s' "
					+ "or writing to file '%s'.", storyFilePath, outputFilePath);
		} catch (Exception e) {
			System.out.format(
					"ERROR - The following error occured: %n%s", e.getMessage());
		}
	}

	private void populateStory() throws IOException {
		String line;
		String token;
		String randomWord;
		Pattern tokenPattern = Pattern.compile("\\[([^]]*)\\]");  
		// Tokens being:  [person] [adverb] [verb] [place] [noun] [adjective] [number]
		// Example:   My friend [person] was [adverb] [verb]ing until arriving at [place].
		
		try (BufferedReader reader = new BufferedReader(new FileReader(storyFilePath));
			 BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {
			
			while ((line = reader.readLine()) != null) {
				if (!line.isEmpty()) {
					// Replace tokens in line with random values
					StringBuffer sb = new StringBuffer();
					Matcher myMatcher = tokenPattern.matcher(line);
					while (myMatcher.find()) {
						token = myMatcher.group(1);
						myMatcher.appendReplacement(sb, "");
						randomWord = getRandomWordByType(Type.valueOf(token.toUpperCase()));
						sb.append(randomWord);
					} // while
					myMatcher.appendTail(sb);
					writer.write(sb.toString());
					writer.newLine(); 
				} // if
			} // outer while
			System.out.format("Output file generated '%s'", outputFilePath);
		}
	}

	/**
	 *  Parses the JSON file and builds a Map of words based on type.
	 *  <p>
	 *  NOTE: This method is designed based on a given format of an array
	 *  of objects. Each object defines a word and the type of word.
	 *  </p>
	 *  <p>
	 *  Example:
	 *    [
	 *        {"word": "<string>", "type": "noun|verb|adjective|number|adverb|place|person"},
	 *            . . .
	 *    ]
	 *  </p>
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	private void parseJSON(Long lineCounter) throws IOException {
		try (FileReader file = new FileReader(jsonFilePath);
			 JsonParser parser = Json.createParser(file);) {
			
			// Loop over each object in array
			while (parser.hasNext() && parseObject(parser)) { 
				lineCounter = parser.getLocation().getLineNumber(); 
			} 			
			System.out.format("Parsed JSON: %s%n", jsonValues);
		} 
	}

	private boolean parseObject(JsonParser parser) {
		String word = null;
		String type = null;
		// Word key-value pair
		word = getValue(parser, WORD);
		// Type key-value pair
		type = getValue(parser, TYPE);
		return addToMap(word, type);
	}

	private JsonParser.Event moveNext(JsonParser parser) {
		// Moves parser to next KEY_NAME or END_ARRAY (end of file)
		JsonParser.Event event = null;
		while (parser.hasNext() && !JsonParser.Event.KEY_NAME.equals(event)) {
			event = parser.next();
		}
		return event;
	}

	private String getValue(JsonParser parser, String key) {
		String value = null;
		// Move to key
		if(JsonParser.Event.KEY_NAME.equals(moveNext(parser))) {  
			// We are at the key, now get the value
			if (parser.hasNext()) {
				switch (parser.next()) {
				case VALUE_STRING:
					value = parser.getString();
					break;
				case VALUE_NUMBER:
					value = parser.getBigDecimal().toPlainString();
					break;
				default:
					break;
				}
			}
		}
		// At the end of file, value must be null
		return value;
	}

	private boolean addToMap(String word, String type) {
		if (word != null && type != null) {
			Type typeOf = Type.valueOf(type.toUpperCase());
			// Add key-value pairs to Map
			if (jsonValues.containsKey(typeOf)) {
				jsonValues.get(typeOf).add(word);
			} else {
				List<String> list = new ArrayList<String>();
				list.add(word);
				jsonValues.put(typeOf, list);
			}
			return true;
		} else if (word == null && type == null) {
			// End of file reached
			return false;
		} else {
			// Word or Type is missing
			throw new IllegalStateException("Word or Type is missing.");
		} 
	}
	
	private String getRandomWordByType(Type type) {
		List<String> wordList = jsonValues.get(type);
		return wordList.get(new Random().nextInt(wordList.size()));
	}
}
