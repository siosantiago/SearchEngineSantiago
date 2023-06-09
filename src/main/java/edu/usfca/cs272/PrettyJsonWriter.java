package edu.usfca.cs272;

import static java.nio.charset.StandardCharsets.UTF_8;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import edu.usfca.cs272.InvertedIndex.PageScore;


/**
 * Outputs several simple data structures in "pretty" JSON format where newlines
 * are used to separate elements and nested elements are indented using spaces.
 *
 * Warning: This class is not thread-safe. If multiple threads access this class
 * concurrently, access must be synchronized externally.
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Fall 2022
 */
public class PrettyJsonWriter {
	/**
	 * Indents the writer by the specified number of times. Does nothing if the
	 * indentation level is 0 or less.
	 *
	 * @param writer the writer to user
	 * @param indent the number of times to indent
	 * @throws IOException if an IO error occurs
	 */
	public static void writeIndent(Writer writer, int indent) throws IOException {
		while (indent-- > 0) {
			writer.write("  ");
		}
	}

	/**
	 * Indents and then writes the String element.
	 *
	 * @param element the element to write
	 * @param writer the writer to use
	 * @param indent the number of times to indent
	 * @throws IOException if an IO error occurs
	 */
	public static void writeIndent(String element, Writer writer, int indent) throws IOException {
		writeIndent(writer, indent);
		writer.write(element);
	}

	/**
	 * Indents and then writes the text element surrounded by {@code " "}
	 * quotation marks.
	 *
	 * @param element the element to write
	 * @param writer the writer to use
	 * @param indent the number of times to indent
	 * @throws IOException if an IO error occurs
	 */
	public static void writeQuote(String element, Writer writer, int indent) throws IOException {
		writeIndent(writer, indent);
		writer.write('"');
		writer.write(element);
		writer.write('"');
	}
	
	

	/**
	 * Writes the elements as a pretty JSON array.
	 *
	 * @param elements the elements to write
	 * @param writer the writer to use
	 * @param indent the initial indent level; the first bracket is not indented,
	 *   inner elements are indented by one, and the last bracket is indented at
	 *   the initial indentation level
	 * @throws IOException if an IO error occurs
	 *
	 * @see Writer#write(String)
	 * @see #writeIndent(Writer, int)
	 * @see #writeIndent(String, Writer, int)
	 */
	public static void writeArray(Collection<? extends Number> elements,
			Writer writer, int indent) throws IOException {
		writer.write("[");
		if(!elements.isEmpty()) {
			writer.write("\n");
			Iterator<? extends Number> iterator = elements.iterator();
			PrettyJsonWriter.writeIndent(writer, indent + 1);
			writer.write(iterator.next().toString());
			while(iterator.hasNext()) {
				writer.write(",\n");
				PrettyJsonWriter.writeIndent(writer, indent + 1);
				writer.write(iterator.next().toString());
			}
		}
		writer.flush();
		writer.write("\n");
		PrettyJsonWriter.writeIndent(writer, indent);
		writer.write("]");
	}

	/**
	 * Writes the elements as a pretty JSON array to file.
	 *
	 * @param elements the elements to write
	 * @param path the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see Files#newBufferedReader(Path, java.nio.charset.Charset)
	 * @see StandardCharsets#UTF_8
	 * @see #writeArray(Collection, Writer, int)
	 */
	public static void writeArray(Collection<? extends Number> elements,
			Path path) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeArray(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON array.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see StringWriter
	 * @see #writeArray(Collection, Writer, int)
	 */
	public static String writeArray(Collection<? extends Number> elements) {
		try {
			StringWriter writer = new StringWriter();
			writeArray(elements, writer, 0);
			return writer.toString();
		}
		catch (IOException e) {
			return null;
		}
	}

	/**
	 * Writes the elements as a pretty JSON object.
	 *
	 * @param elements the elements to write
	 * @param writer the writer to use
	 * @param indent the initial indent level; the first bracket is not indented,
	 *   inner elements are indented by one, and the last bracket is indented at
	 *   the initial indentation level
	 * @throws IOException if an IO error occurs
	 *
	 * @see Writer#write(String)
	 * @see #writeIndent(Writer, int)
	 * @see #writeIndent(String, Writer, int)
	 */
	public static void writeObject(Map<String, ? extends Number> elements,
			Writer writer, int indent) throws IOException {
		writer.write("{");
		if(!elements.isEmpty()) {
			writer.write("\n");
			var entryIterator = elements.entrySet().iterator();
			String str = entryIterator.next().getKey();
			PrettyJsonWriter.writeQuote(str, writer, indent + 1);
			writer.write(": ");
			writer.write(elements.get(str).toString());
			while(entryIterator.hasNext()) {
				writer.write(",");
				writer.write("\n");
				str = entryIterator.next().getKey();
				PrettyJsonWriter.writeQuote(str, writer, indent + 1);
				writer.write(": ");
				writer.write(elements.get(str).toString());
			}
		}
		writer.write("\n");
		PrettyJsonWriter.writeIndent(writer, indent);
		writer.write("}");
	}

	/**
	 * Writes the elements as a pretty JSON object to file.
	 *
	 * @param elements the elements to write
	 * @param path the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see Files#newBufferedReader(Path, java.nio.charset.Charset)
	 * @see StandardCharsets#UTF_8
	 * @see #writeObject(Map, Writer, int)
	 */
	public static void writeObject(Map<String, ? extends Number> elements,
			Path path) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeObject(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON object.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see StringWriter
	 * @see #writeObject(Map, Writer, int)
	 */
	public static String writeObject(Map<String, ? extends Number> elements) {
		try {
			StringWriter writer = new StringWriter();
			writeObject(elements, writer, 0);
			return writer.toString();
		}
		catch (IOException e) {
			return null;
		}
	}

	/**
	 * Writes the elements as a pretty JSON object with nested arrays. The generic
	 * notation used allows this method to be used for any type of map with any
	 * type of nested collection of number objects.
	 *
	 * @param elements the elements to write
	 * @param writer the writer to use
	 * @param indent the initial indent level; the first bracket is not indented,
	 *   inner elements are indented by one, and the last bracket is indented at
	 *   the initial indentation level
	 * @throws IOException if an IO error occurs
	 *
	 * @see Writer#write(String)
	 * @see #writeIndent(Writer, int)
	 * @see #writeIndent(String, Writer, int)
	 * @see #writeArray(Collection)
	 */
	public static void writeNestedArrays(
			Map<String, ? extends Collection<? extends Number>> elements,
			Writer writer, int indent) throws IOException {
		writer.write("{");
		if(!elements.isEmpty()) {
			writer.write("\n");
			var entryIterator = elements.entrySet().iterator();
			String str = entryIterator.next().getKey();
			PrettyJsonWriter.writeQuote(str, writer, indent + 1);
			writer.write(": ");
			PrettyJsonWriter.writeArray(elements.get(str), writer, indent + 1);
			while(entryIterator.hasNext()) {
				writer.write(",");
				writer.write("\n");
				str = entryIterator.next().getKey();
				PrettyJsonWriter.writeQuote(str, writer, indent + 1);
				writer.write(": ");
				PrettyJsonWriter.writeArray(elements.get(str), writer, indent + 1);
			}
		}
		writer.write("\n");
		PrettyJsonWriter.writeIndent(writer, indent);
		writer.write("}");
	}

	/**
	 * Writes the elements as a pretty JSON object with nested arrays to file.
	 *
	 * @param elements the elements to write
	 * @param path the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see Files#newBufferedReader(Path, java.nio.charset.Charset)
	 * @see StandardCharsets#UTF_8
	 * @see #writeNestedArrays(Map, Writer, int)
	 */
	public static void writeNestedArrays(
			Map<String, ? extends Collection<? extends Number>> elements, Path path)
			throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeNestedArrays(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON object with nested arrays.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see StringWriter
	 * @see #writeNestedArrays(Map, Writer, int)
	 */
	public static String writeNestedArrays(
			Map<String, ? extends Collection<? extends Number>> elements) {
		try {
			StringWriter writer = new StringWriter();
			writeNestedArrays(elements, writer, 0);
			return writer.toString();
		}
		catch (IOException e) {
			return null;
		}
	}
	
	/**
	 * Creates the writer to pass to create the JSON writer
	 * 
	 * @param invertedIndex to add to the JSON format
	 * @param path of the element
	 * @throws IOException it works with a buffer and paths so it might not be able to open file
	 */
	public static void writeSuperNestedArrays(Map<String, ? extends Map<String, ? extends Collection<? extends Number>>> invertedIndex, Path path) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeSuperNestedArrays(invertedIndex, writer, 0);
		}
	}
	
	/**
	 * Creates the JSON output from the invertedIndex given
	 * 
	 * @param invertedIndex the data structure used to create the JSOn writer
	 * @param writer the writer used to add to the file or print statement
	 * @param indent how much indentation is need
	 * @throws IOException write can give you a IOExeption
	 */
	public static void writeSuperNestedArrays(Map<String, ? extends Map<String, ? extends Collection<? extends Number>>> invertedIndex, Writer writer, int indent) throws IOException{
		writer.write("{");
		if(!invertedIndex.isEmpty()) {
			writer.write("\n");
			var entryIterator = invertedIndex.entrySet().iterator();
			String str = entryIterator.next().getKey();
			PrettyJsonWriter.writeQuote(str, writer, indent + 1);
			writer.write(": ");
			PrettyJsonWriter.writeNestedArrays(invertedIndex.get(str), writer, indent + 1); 
			while(entryIterator.hasNext()) {
				writer.write(",");
				writer.write("\n");
				str = entryIterator.next().getKey();
				PrettyJsonWriter.writeQuote(str, writer, indent + 1);
				writer.write(": ");
				PrettyJsonWriter.writeNestedArrays(invertedIndex.get(str), writer, indent + 1);
			}
		}
		writer.write("\n");
		PrettyJsonWriter.writeIndent(writer, indent);
		writer.write("}");
	}
	
	/**
	 * Creates the writer if there is no file to input it so it will be send to the terminal
	 * as a print statement
	 * 
	 * @param invertedIndex is the elements passed to be put in JSON format
	 * @return a {@link String} containing the elements in pretty JSON format 
	 */
	public static String writeSuperNestedArrays(Map<String, ? extends Map<String, ? extends Collection<? extends Number>>> invertedIndex ) {
		try {
			StringWriter writer = new StringWriter();
			writeSuperNestedArrays(invertedIndex, writer, 0);
			return writer.toString();
		}
		catch (IOException e) {
			return null;
		}
	}

	/**
	 * Method that calls its own query out putter to be able to return and write what is needed for the query. Creates the writer.
	 * @param searchResults all of the page scores needed
	 * @param path the path of the output file
	 * @throws IOException writing to a file can create exceptions
	 */
	public static void writePrettyQueryOutput(Map<String, Collection<PageScore>> searchResults, Path path) throws IOException {
		try(BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writePrettyQueryOutPut(searchResults, writer, 0);
			
		}
	}

	/**
	 * Method that creates the first part of the json file for query searches.
	 * @param searchResults all of the page scores needed.
	 * @param writer the writer that is writing to the output file.
	 * @param indentation the amount of indentation need for the json file
	 * @throws IOException writing to a file can create exceptions
	 */
	public static void writePrettyQueryOutPut(Map<String, Collection<PageScore>> searchResults, Writer writer, int indentation) throws IOException {
		writer.write("{\n");
		if(!searchResults.isEmpty()) {
			Iterator<Entry<String, Collection<PageScore>>> pageIterator = searchResults.entrySet().iterator();
			var entry = pageIterator.next();
			writePageRanks(entry.getValue(), writer, 1, entry.getKey());
			while(pageIterator.hasNext()) {
				writer.write(",\n");
				entry = pageIterator.next();
				writePageRanks(entry.getValue(), writer, 1, entry.getKey());
			}
			writer.write("\n");
		}
		writer.write("}");
	} 
	
	/**
	 * Creates the second [art of the query output to the file, it adds all the writing of each of the page ranks
	 * @param collection all of the page scores needed
	 * @param writer the writer that is writing to the output file.
	 * @param indent the amount of indentation need for the JSON file
	 * @param query that will be written into output
	 * @throws IOException  writing to a file can create exceptions
	 */
	public static void writePageRanks(Collection<PageScore> collection, Writer writer, int indent, String query) throws IOException {
		PrettyJsonWriter.writeQuote(query, writer, indent);
		writer.write(": " + "[\n");
		if(!collection.isEmpty()) {
			Iterator<InvertedIndex.PageScore> scoreIterator = collection.iterator();
			InvertedIndex.PageScore pageScore = scoreIterator.next(); 
			writeOnePage(pageScore, writer, indent + 1);
			while(scoreIterator.hasNext()) {
				writer.write(",\n");
				pageScore = scoreIterator.next(); 
				writeOnePage(pageScore, writer, indent + 1);
			}
			writer.write("\n");
		}
		PrettyJsonWriter.writeIndent("]", writer, indent);
	}
	
	/**
	 * Method that writes on the file a whole pageScore in JSON standards.
	 * @param pageScore pageScore needed values.
	 * @param writer writer needed values.
	 * @param indent the amount of indent needed.
	 * @throws IOException writing on a file.
	 */
	public static void writeOnePage(InvertedIndex.PageScore pageScore, Writer writer, int indent) throws IOException {
		DecimalFormat FORMATTER = new DecimalFormat("0.00000000");
		PrettyJsonWriter.writeIndent("{\n", writer, indent);
		PrettyJsonWriter.writeQuote("count", writer, indent + 1);
		writer.write(": " + pageScore.getCount() + ",\n");
		PrettyJsonWriter.writeQuote("score", writer, indent + 1);
		writer.write(": " + FORMATTER.format(pageScore.getScore()) + ",\n");
		PrettyJsonWriter.writeQuote("where", writer, indent + 1);
		writer.write(": ");
		PrettyJsonWriter.writeQuote(pageScore.getLocation(), writer, 0);
		writer.write("\n");
		PrettyJsonWriter.writeIndent("}",writer, indent);
	}
	

	/**
	 * Writes the elements as a pretty JSON array with nested objects. The generic
	 * notation used allows this method to be used for any type of collection with
	 * any type of nested map of String keys to number objects.
	 *
	 * @param elements the elements to write
	 * @param writer the writer to use
	 * @param indent the initial indent level; the first bracket is not indented,
	 *   inner elements are indented by one, and the last bracket is indented at
	 *   the initial indentation level
	 * @throws IOException if an IO error occurs
	 *
	 * @see Writer#write(String)
	 * @see #writeIndent(Writer, int)
	 * @see #writeIndent(String, Writer, int)
	 * @see #writeObject(Map)
	 */
	public static void writeNestedObjects(
			Collection<? extends Map<String, ? extends Number>> elements,
			Writer writer, int indent) throws IOException {
		writer.write("[");
		if(!elements.isEmpty()) {
			writer.write("\n");
			var entryIterator = elements.iterator();
			PrettyJsonWriter.writeIndent(writer, indent); 
			PrettyJsonWriter.writeObject(entryIterator.next(), writer, indent);//changed this ones from the ones in the other JSON file
			while(entryIterator.hasNext()) {
				writer.write(",\n");
				PrettyJsonWriter.writeIndent(writer, indent);
				PrettyJsonWriter.writeObject(entryIterator.next(), writer, indent);
			}
		}
		writer.write("\n");
		writer.write("]");
	}

	/**
	 * Writes the elements as a pretty JSON array with nested objects to file.
	 *
	 * @param elements the elements to write
	 * @param path the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see Files#newBufferedReader(Path, java.nio.charset.Charset)
	 * @see StandardCharsets#UTF_8
	 * @see #writeNestedObjects(Collection)
	 */
	public static void writeNestedObjects(
			Collection<? extends Map<String, ? extends Number>> elements, Path path)
			throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeNestedObjects(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON array with nested objects.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see StringWriter
	 * @see #writeNestedObjects(Collection)
	 */
	public static String writeNestedObjects(
			Collection<? extends Map<String, ? extends Number>> elements) {
		try {
			StringWriter writer = new StringWriter();
			writeNestedObjects(elements, writer, 0);
			return writer.toString();
		}
		catch (IOException e) {
			return null;
		}
	}
}
