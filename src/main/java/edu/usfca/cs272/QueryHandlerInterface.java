package edu.usfca.cs272;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;

import edu.usfca.cs272.InvertedIndex.PageScore;

/**
 * Interface that handles sorting and parsing the raw file of queries and creates a functionally query data structure.
 * @author santiago jaramillo
 *
 */
public interface QueryHandlerInterface {

	/**
	 * Method that goes through the file and gets all the queries to call parse query line
	 * and with this it creates the needed data structure for the search
	 * @param input the file to look at
	 * @param exact checks what type of search it will be
	 * @throws IOException reading a file can cause exceptions
	 */
	public default void parseQueryFile(Path input, boolean exact) throws IOException {
		try(BufferedReader bufferReader = Files.newBufferedReader(input)) {
			String line;
			while((line = bufferReader.readLine()) != null) {
				parseQueryLine(line, exact);
			}
		}
	}
	
	/**
	 * Method that parses the queries with unique stems of word cleaner, it creates a string version 
	 * and it adds it to the search results data structure. 
	 * @param input the line needed to be parsed
	 * @param exact if we are going to do a partial or exact search
	 * @throws IOException reading a file can sometimes throw.
	 */
	public void parseQueryLine(String input, boolean exact) throws IOException;
	
	
	/**
	 * Method that creates the JSON file output from the JsonWriter
	 * @param output file to write the JSON output to.
	 * @throws IOException writing to a file can throw exceptions.
	 */
	public void toJSON(Path output) throws IOException;
	
	@Override 
	/**
	 * Method to String the class printing the search result, and the inverted index
	 */
	public String toString();
	
	/**
	 * Method that return an unmodifiable set of all the query lines.
	 * @return an unmodifiable set of all the search result queries.
	 */
	public Set<String> getQueryLines();
	
	/**
	 * Method that runs the process of get the search for a line given.
	 * @param unprocessedLine the query that needs to be handled and processed to make the search.
	 * @return a collection of all the page score solutions.
	 */
	public Collection<PageScore> getQueryResults(String unprocessedLine);

}
