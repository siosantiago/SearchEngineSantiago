package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;

/**
 * This class in charge of finding all the text from the path and adding them into a hashSet to be able
 * to added them into a invertedIndex. It contains two methods which one is recursive and calls the other
 * one.
 * 
 * @author Santiago Jaramillo
 *
 */
public class DirectoryTraverser {
	
	/**
	 * Method that gets all the text from the file and turns adds it to the collection
	 * 
	 * @param text to find the actual text inputFiles in a directory
	 * @param collection the place you will keep the text inputFiles
	 * @throws IOException the inputFile directory might not be there so you need to throw the exception
	 */
	public static void findAllTextFiles(Path text, Collection<Path> collection) throws IOException{
		if (Files.isDirectory(text)) {
			try(DirectoryStream<Path> listing = Files.newDirectoryStream(text)) {
				for (Path path: listing) {
					findAllTextFiles(path, collection);
				}
			}
		}
		else if (isTextFile(text)) {
			collection.add(text);
		}
	}
	
	
	/**
	 * Method that calls findAllTextFiles and creates the needed data
	 * structure. then return the paths needed.
	 *  
	 * @param path the path which we want to find the files.
	 * @return a hash set with all the files.
	 * @throws IOException reading files might throw a exception.
	 */
	public static HashSet<Path> getAllTextFiles(Path path) throws IOException {
		HashSet<Path> paths =  new HashSet<Path>();
		findAllTextFiles(path, paths);	
		return paths;
	}

	
	/**
	 * Boolean method that checks if it is a text file.
	 * 
	 * @param path to see if it is actually a .text file or if it's something else
	 * @return true if it is a text file false if its anything else.
	 */
	public static boolean isTextFile(Path path) {
		String lower = path.toString().toLowerCase();
		return lower.endsWith(".txt") || lower.endsWith(".text");
	}
	

}
