package edu.usfca.cs272;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import edu.usfca.cs272.InvertedIndex.PageScore;

/**
 * the same as query handler but multi
 * @author santiagojaramillo
 *
 */
public class MultithreadedQueryHandler implements QueryHandlerInterface{ 
	
	/**the work queue thread amount for the multi*/
	public final int threads;
	
	/** data structure that holds the page scores and the querie line for output*/
	private final Map<String, Collection<PageScore>> searchResults;

	/** the inverted Index needed to json */
	private final ThreadSafeInvertedIndex invertedIndex;

	/**
	 * Same as the other but with a work queue
	 * @param invertedIndex for the search
	 * @param threads for the task
	 */
	public MultithreadedQueryHandler(ThreadSafeInvertedIndex invertedIndex, int threads) {
		this.invertedIndex = invertedIndex;
		this.threads = threads;
		this.searchResults = new TreeMap<>();
	}

	@Override
	/**
	 * Method that creates a work queue give it methods to execute that it creates from reading the file given 
	 * very similar to the regular query handler parse query file.
	 * @param input the files to read to get the information from
	 * @param exact checks if it's going to need an exact search or a partial
	 * @throws IOException reading a file can throw exceptions.
	 */
	public void parseQueryFile(Path input, boolean exact) throws IOException {
		WorkQueue workQueue = new WorkQueue(threads);
		try(BufferedReader bufferReader = Files.newBufferedReader(input)) {
			String line;
			while((line = bufferReader.readLine()) != null) {
				workQueue.execute(new Task(line, exact));
			}
		}finally {
			workQueue.join(); 
		}
		
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
			
			synchronized(searchResults) {
				if(searchResults.containsKey(strQuery)) {
					return;
				}
			}
			var local = invertedIndex.search(stems, exact);
			
			synchronized(searchResults) {
				searchResults.put(strQuery, local);
			}
		}
	}
	
	
	/**
	 * Task made to run the query line in a multithreaded way
	 * @author santiagojaramillo
	 *
	 */
	public class Task implements Runnable {
		
		/** input for the task */
		private final String input;
		
		/**exact or not for task*/
		private final boolean exact;
		
		/**
		 * Constructor that you give the string need to create the result and if it is an exact search or not
		 * @param input to use to search
		 * @param exact true if it is an exact search otherwise partial search.
		 */
		public Task(String input,  boolean exact) {
			this.input = input;
			this.exact = exact;
		}
		
		@Override
		public void run() {
			try {
				parseQueryLine(input, exact);
			} catch (IOException e) {
				System.out.println("Error handling the file." + e);
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
		synchronized (searchResults) {
			PrettyJsonWriter.writePrettyQueryOutput(searchResults, output);
		}
	}
		
	@Override 
	/**
	 * Method to String the class printing the search result, and the inverted index
	 */
	public String toString() {
		StringBuffer strB;
		synchronized (searchResults) {
			strB = new StringBuffer(searchResults.toString());
		}
		
		synchronized (invertedIndex) {
			strB = strB.append(invertedIndex.toString());
		}
		
		return strB.toString();
	}
	
	@Override
	/**
	 * Method that return an unmodifiable set of all the query lines.
	 * @return an unmodifiable set of all the search result queries.
	 */
	public Set<String> getQueryLines() {
		synchronized (searchResults) {
			return Collections.unmodifiableSet(searchResults.keySet());
		}	
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
			synchronized (searchResults) {
				if(searchResults.containsKey(strQuery)) {
					return searchResults.get(strQuery);
				}
			}
		}
		return null;
	}
}
