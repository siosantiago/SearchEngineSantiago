package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * The class of the data structure needed to complete the project1test it has
 * a treeSet nested into a TreeMap nested into a tree map to keep it in order
 * 
 * @author Santiago Jaramillo
 * @version fall 2022
 */
public class InvertedIndex {
	
	/**
	 * Inner class to be able to keep all information needed for ranking the searches.
	 * Implements comparable
	 * @author Santiago Jaramillo
	 *
	 */
	public class PageScore implements Comparable<PageScore> {
		
		/** the score compare to all the words in the location */
		private double score;
		
		/** the amount of times the query was found */
		private int count;
		
		/** location in string of the page score */
		private final String location; 
		/**
		 * Constructor method that assign all the values
		 * @param location location in string of the page score
		 */
		public PageScore(String location) { 
			this.score = 0;
			this.count = 0;
			this.location = location;
		}
		
		/**
		 * Getter method that returns the score
		 * @return score of the page score
		 */
		public double getScore() {
			return score;
		}
		
		/**
		 * Getter method that returns the count
		 * @return count of the page score
		 */
		public int getCount() {
			return count;
		}
		
		/**
		 * Getter method that returns the score
		 * @return score of the page score
		 */
		public String getLocation() {
			return location;
		}
		
		/**
		 * It updates the values from the count score
		 * @param word need to know which values need to be updated
		 */
		private void updateValues(String word) {
			count += invertedIndex.get(word).get(location).size();
			score = (double) count / filesCounter.get(location); 
		}

		@Override
		/**
		 * compareTo method that check first the score, than the count and finally the location to compare which one should go first.
		 * @param other another page score to compareTo
		 * @return which of the two has priority.
		 */
		public int compareTo(PageScore other) {
			int scoreResult = Double.compare(other.score, this.score);
			
			if(scoreResult != 0) {
				return scoreResult;
			}
			
			int countResult = Integer.compare(other.count, this.count);
			
			if(countResult != 0) {
				return countResult;
			}
			
			return this.location.compareToIgnoreCase(other.location);
		}

		@Override
		/**
		 * The way the output should be handled in a toString.
		 */
		public String toString() {
			String result = "count: " + count + "\nscore: " + score + "\nlocation: " + location + "\n";
			return result;
		}
	}
	
	
	
	/** the treeMap for the invertedIndex */
	private final TreeMap<String, TreeMap<String, TreeSet<Integer>>> invertedIndex;
	
	/** the treeMap that contains a key query, and a value integer */
	private final TreeMap<String, Integer> filesCounter; 
	
	/**
	 * Constructor of InvertedIndex
	 */
	public InvertedIndex() {
		invertedIndex = new TreeMap<>();
		filesCounter = new TreeMap<>();
	}
	
	/**
	 * First method called to do the search which will separate if it needs to do a exact or partial search
	 * @param queries needed to do the search
	 * @param exact check if its exact or partial
	 * @return the list gotten from the search
	 */
	public List<PageScore> search(Set<String> queries, boolean exact) {
		return exact ? exactSearch(queries) : partialSearch(queries);
	}
	
	/**
	 * Method that creates the exact search in the inverted index and returns a list.
	 * 
	 * @param queries the different queries needed.
	 * @return pageScores of every search.
	 */
	public List<PageScore> exactSearch(Set<String> queries) {
		HashMap<String, PageScore> totalsFiles = new HashMap<>();
		List<PageScore> pageScores = new ArrayList<>();

		for(String word: queries) {
			if(invertedIndex.containsKey(word)) {
				searchHelper(totalsFiles, pageScores, word);
			}
		}
		
		Collections.sort(pageScores);
		return pageScores;
	}

	/**
	 * Method that creates the exact search in the inverted index and returns a list.
	 * 
	 * @param queries the different queries needed.
	 * @return pageScores of every search.
	 */
	public List<PageScore> partialSearch(Set<String> queries) {
		HashMap<String, PageScore> totalsFiles = new HashMap<>();
		List<PageScore> pageScores = new ArrayList<>();
		
		for(String querie: queries) {
			for(String word: invertedIndex.tailMap(querie).keySet()) { 
				if(!word.startsWith(querie)) {
					break;
				}
				searchHelper(totalsFiles, pageScores, word);
			}
		}
		
		Collections.sort(pageScores);
		return pageScores;
	}
	
	/**
	 * Method that creates the new page score, add in to the totals files and to the page scores structure the needed methods
	 * @param totalsFiles data structure to be added the page score
	 * @param pageScores data structure to be added the page score
	 * @param word of the search an to find its page score or make it.
	 */
	private void searchHelper(HashMap<String, PageScore> totalsFiles, List<PageScore> pageScores, String word) {
		Set<String> locations = invertedIndex.get(word).keySet();
		for(String location : locations) {
			if (!totalsFiles.containsKey(location)) {
				PageScore pageScore = new PageScore(location);
				totalsFiles.put(location, pageScore);
				pageScores.add(pageScore);
			}
			totalsFiles.get(location).updateValues(word);
		}
	}
	
	/**
	 * Getter Method that returns the count of the file inputed as a parameter
	 * @param location of the file to see the amount of words in this location
	 * @return an integer that positive of the amount of words in this location or a negative number if this location does not exist.
	 */
	public int getCount(String location) {
		return filesCounter.getOrDefault(location, 0);
	}
	
	/**
	 * Method that checks if the location exist inside of the files counter
	 * @param location to be checked
	 * @return boolean result of contains key
	 */
	public boolean containsLocation(String location) {
		return filesCounter.containsKey(location);
	}
	
	/**
	 * Getter method of a unmodifiable set of all the keys in file counter
	 * @return the keys in a unmodifiable way
	 */
	public Set<String> getLocationsFilesCounter() {
		return Collections.unmodifiableSet(filesCounter.keySet());
	}
	
	
	/**
	 * method to return the amount of words in the Inverted Index and therefore the size.
	 * 
	 * @return the amount of words that exist inside the Inverted Index.
	 */
	public int numWords() {
		return invertedIndex.size();
	}
	
	/**
	 * Method that return the amount of files that have this word.
	 * 
	 * @param word check how many locations of files exist that have that word
	 * 	in their file.
	 * 
	 * @return the amount of files that contain this word.
	 */
	public int numLocations(String word) {
		if(this.containsWord(word)) {
			return invertedIndex.get(word).size();
		}
		return -1;
	}
	
	/**
	 * Method that returns the size of all the indexes of this word in this file.
	 * 
	 * @param word looked by the person to find all its positions in the file.
	 * @param location of the file in string of the indexes.
	 * @return amount of indexes in the file of this word
	 */
	public int numPositions(String word, String location) {
		if(containsLocation(word, location)) {
			return invertedIndex.get(word).get(location).size();
		}
		return -1;
	}
	/**
	 * Boolean method that returns if a word is inside the invertedIndex.
	 * 
	 * @param word checked if it is inside the invertedIndex.
	 * @return true or false depending if the word is inside the invertedIndex
	 */
	public boolean containsWord(String word) {
		return invertedIndex.containsKey(word);
	}
	
	/**
	 * Boolean method that returns if in a word of the invertedIndex there is this locations given.
	 * 
	 * @param word of the location to find if the location is inside of this word.
	 * @param location checked to see if it actually contains this words.
	 * @return true if the locations is contained inside of the word of the invertedIndex else false.
	 */
	public boolean containsLocation(String word, String location) {
		if(containsWord(word)) {
			return invertedIndex.get(word).containsKey(location);
		}
		return false;
	}
	
	/**
	 * Boolean method that returns if the position exist on the file given for this word.
	 * 
	 * @param word given for the location
	 * @param location given to see if the position is in this file
	 * @param position checked if position exist or not
	 * @return true if the position exist false otherwise.
	 */
	public boolean containsPosition(String word, String location, int position) {
		if(containsLocation(word, location)) {
			return invertedIndex.get(word).get(location).contains(position);
		}
		return false;
	}
	
	/**
	 * Getter method that returns a set of all the words in the 
	 * invertedIndex.
	 * 
	 * @return the set of all the words in the inverted index.
	 */
	public Set<String> getWords() {
		return Collections.unmodifiableSet(invertedIndex.keySet());
	}
	
	/**
	 * Getter method of all the keys of the locations in the inverted index.
	 * 
	 * @param word of the keySet of locations needed.
	 * @return the keySet of all the locations of this word or null if there is no such word.
	 */
	public Set<String> getLocations(String word) {
		if(containsWord(word)) {
			return invertedIndex.get(word).keySet();
		}
		return null;
	}
	
	/**
	 * Getter method for a certain position
	 * 
	 * @param word the keySet of locations needed.
	 * @param location the keySet need for the positions
	 * @return the set of all the positions.
	 */
	public Set<Integer> getPositions(String word, String location) {
		if(containsLocation(word, location)) {
			return invertedIndex.get(word).get(location);
		}
		return null;
	}
	
	/**
	 * Add method of position.
	 * 
	 * @param word of the location
	 * @param location of the count
	 * @param count needed to be added.
	 * @return true if added
	 */
	public boolean addPosition(String word, String location, int count) {
		int current = filesCounter.getOrDefault(location, 0);
		filesCounter.put(location, Math.max(current, count));
		
		invertedIndex.putIfAbsent(word, new TreeMap<String, TreeSet<Integer>>());
		invertedIndex.get(word).putIfAbsent(location, new TreeSet<Integer>());	
		return invertedIndex.get(word).get(location).add(count);
	}
	
	@Override
	/**
	 * Method toString that give you the string of the inverted index.
	 */
	public String toString() {
		return invertedIndex.toString();
	}
	
	/**
	 * Method that creates the JSON file and calls prettyJSON writer.
	 * 
	 * @param path path thats going to be written by the JSON file
	 * @throws IOException writing in a file can give you a exception
	 */
	public void toJSON(Path path) throws IOException {
		PrettyJsonWriter.writeSuperNestedArrays(invertedIndex, path);
	}
	
	/**
	 * Method that creates the JSON file and calls prettyJSON writer for the counts method.
	 * 
	 * @param output path thats going to be written by the JSON file
	 * @throws IOException writing in a file can give you a exception
	 */
	public void countToJSON(Path output) throws IOException {
		PrettyJsonWriter.writeObject(filesCounter, output);
	}
	
	/**
	 * The copying of one inverted index to the other
	 * @param other inverted index to be written too
	 */
	public void addAll(InvertedIndex other) {
		for (var wordsEntry: other.invertedIndex.entrySet()) {
			String word = wordsEntry.getKey();
			TreeMap<String, TreeSet<Integer>> locations = wordsEntry.getValue();
			
			if (this.invertedIndex.containsKey(word)) {
				for(var positionEntry: locations.entrySet()) {
					String position = positionEntry.getKey();
					TreeSet<Integer> positions = positionEntry.getValue();
					if(this.invertedIndex.get(word).containsKey(position)) {
						this.invertedIndex.get(word).get(position).addAll(positions);
					}else {
						invertedIndex.get(word).put(position, positions);
					}
				}
			}
			else {
				this.invertedIndex.put(word, locations);
			}
		}
		
		for (var countsEntry : other.filesCounter.entrySet()) {
			int current = this.filesCounter.getOrDefault(countsEntry.getKey(), 0);
			this.filesCounter.put(countsEntry.getKey(), Math.max(current, countsEntry.getValue()));
		}
	}
}
