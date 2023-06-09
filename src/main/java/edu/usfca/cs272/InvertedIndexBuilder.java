package edu.usfca.cs272;

import static opennlp.tools.stemmer.snowball.SnowballStemmer.ALGORITHM.ENGLISH;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

/**
 * Builder class that gets all the things from a inputFile into 
 * our invertedIndex, it only contains one method.
 * 
 * @author Santiago Jaramillo
 *
 */
public class InvertedIndexBuilder {
	
	/**
	 * Method builds the invertedIndex with the path given by the directory traverser.
	 * @param input path that needs to be build
	 * @param invertedIndex the inverted index being used
	 * @throws IOException adding a file can made me send a no file exception
	 */
	public static void build(Path input, InvertedIndex invertedIndex) throws IOException {
		if (Files.isDirectory(input)) {
			HashSet<Path> hashSet = DirectoryTraverser.getAllTextFiles(input);
			
			for(Path path: hashSet) {
				InvertedIndexBuilder.addFile(invertedIndex, path);
			}
		}
		else {
			InvertedIndexBuilder.addFile(invertedIndex, input);
		}
	}

	/**
	 * Method that adds all the list of words into the invertedIndex
	 * it has a counter to put how many words there the word was at 
	 * in the text.
	 * 
	 * @param invertedIndex add all the list words to the data structure
	 * @param input the inputFiles that need to be the key of the words
	 * @throws IOException file can throw exceptions
	 */
	public static void addFile(InvertedIndex invertedIndex, Path input) throws IOException {
		int counter = 1;
		String location = input.toString();
		
		try (BufferedReader buffer = Files.newBufferedReader(input)) {
			String line;
			Stemmer stemmer = new SnowballStemmer(ENGLISH);
			while((line = buffer.readLine()) != null) {
				String[] cleanedAndSplitText = WordCleaner.parse(line);
				for(String word: cleanedAndSplitText) {
					String stemed = stemmer.stem(word).toString();
					invertedIndex.addPosition(stemed, location, counter++);
				}
			}
		}
	}
}
