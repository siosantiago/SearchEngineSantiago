package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import edu.usfca.cs272.InvertedIndex.PageScore;

/**
 * Class that handles sorting and parsing the raw file of queries and creates a functionally query data structure.
 * @author santiago jaramillo
 *
 */
public class QueryHandler implements QueryHandlerInterface{
	
	/** data structure that holds the page scores and the query line for output*/
	private final Map<String, Collection<PageScore>> searchResults; 
	
	/** the inverted Index needed to json */
	private final InvertedIndex invertedIndex;
	
	/**
	 * Constructor for the class it takes in a inverted index and creates a new instance for the search result data structure.
	 * @param invertedIndex inverted index to create the json file.
	 */
	public QueryHandler(InvertedIndex invertedIndex) {
		this.invertedIndex = invertedIndex;
		this.searchResults = new TreeMap<>();
	}
	
	@Override
	/**
	 * Method that parses the queries with unique stems of word cleaner, it creates a string version 
	 * and it adds it to the search results data structure. 
	 * @param input the line needed to be parsed
	 * @param exact if we are going to do a partial or exact search
	 * @throws IOException reading a file can sometimes throw.
	 */
	public void parseQueryLine(String input, boolean exact) throws IOException {
		TreeSet<String> stems = WordCleaner.uniqueStems(input);
		if(!stems.isEmpty()) {
			String strQuery = String.join(" ", stems);
			if(!searchResults.containsKey(strQuery)) {
				searchResults.put(strQuery, invertedIndex.search(stems, exact));
			}
		}
	}
	
	@Override
	/**
	 * Method that creates the JSON file output from the JsonWriter
	 * @param output file to write the JSON output to.
	 * @throws IOException writing to a file can throw exceptions.
	 */
	public void toJSON(Path output) throws IOException {
		PrettyJsonWriter.writePrettyQueryOutput(searchResults, output);
	}
	
	@Override 
	/**
	 * Method to String the class printing the search result, and the inverted index
	 */
	public String toString() {
		return searchResults.toString() + invertedIndex.toString();
	}
	
	@Override
	/**
	 * Method that return an unmodifiable set of all the query lines.
	 * @return an unmodifiable set of all the search result queries.
	 */
	public Set<String> getQueryLines() {
		return Collections.unmodifiableSet(searchResults.keySet());
	}
	
	@Override
	/**
	 * Method that runs the process of get the search for a line given.
	 * @param unprocessedLine the query that needs to be handled and processed to make the search.
	 * @return a collection of all the page score solutions.
	 */
	public Collection<PageScore> getQueryResults(String unprocessedLine) {
		TreeSet<String> stems = WordCleaner.uniqueStems(unprocessedLine);
		if(!stems.isEmpty()) {
			String strQuery = String.join(" ", stems);
			if(searchResults.containsKey(strQuery)) {
				return searchResults.get(strQuery);
			}
		}
		return null;
	}
}
