package org.github.terminological.roogledocs;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.github.terminological.roogledocs.datatypes.Tuple;
import org.github.terminological.roogledocs.datatypes.TupleList;

import static uk.co.terminological.rjava.RConverter.*;

import uk.co.terminological.rjava.RClass;
import uk.co.terminological.rjava.RDefault;
import uk.co.terminological.rjava.RMethod;
import uk.co.terminological.rjava.types.RDataframe;

/**
 * Programmatically substitute data into a google doc
 * @author terminological
 *
 */
@RClass
public class RoogleDocs {

	RService service;
	RDocument document;
	
	private RDocument rdoc() throws IOException {
		if (document == null) throw new IOException("Document must be defined - use the `withDocument()` method"); 
		return document;
	}
	
	/**
	 * 
	 * @param tokenDirectory the place to store authentication tokens. This should not be checked into version control.
	 * @throws IOException
	 * @throws GeneralSecurityException
	 */
	@RMethod
	public RoogleDocs(
		@RDefault(rCode = "normalizePath('~/.roogledocs', mustWork = FALSE)") String tokenDirectory 
	) throws IOException, GeneralSecurityException {
		service = new RService(tokenDirectory);
	}
	
	/**
	 * Select a document by its share url or id.
	 * @param shareUrlOrDocId the url from clicking a share button in google docs or an id from searchForDocuments() method
	 * @return itself - a fluent method
	 * @throws IOException
	 */
	@RMethod
	public RoogleDocs withDocument(String shareUrlOrDocId) throws IOException {
		this.document = service.getDocument(shareUrlOrDocId);
		return this;
	}
	
	/**
	 * Search for or create a document if it is missing.
	 * @param title a document title. If there is an exact match in google drive then that document will be used 
	 * otherwise a new one will be created.
	 * @return itself - a fluent method
	 * @throws IOException
	 */
	@RMethod
	public RoogleDocs findOrCreateDocument(String title) throws IOException {
		this.document = service.getOrCreate(title);
		return this;
	}
	
	/**
	 * @param titleMatch a string to be searched for as an approximate match. All results will be retrieved with document ids.
	 * @return a dataframe containing "id" and "name" columns
	 * @throws IOException
	 */
	@RMethod
	public RDataframe searchForDocuments(String titleMatch) throws IOException {
		List<Tuple<String, String>> tmp = service.search(titleMatch, RService.MIME_DOCS);
		return tmp.stream().collect(
				dataframeCollector(
						mapping(Tuple.class, "id", t->t.getFirst()),
						mapping(Tuple.class, "name", t->t.getSecond())
				));
	}
	
	/**
	 * Finds tags defined in the current document
	 * @return a dataframe containing "tag" and "count" columns
	 * @throws IOException
	 */
	@RMethod 
	public RDataframe tagsDefined() throws IOException {
		Map<String, TupleList<Integer, Integer>> tmp = rdoc().updateInlineTags();
		return tmp.entrySet().stream().collect(
				dataframeCollector(
						mapping("tag", t->t.getKey()),
						mapping("count", t->t.getValue().size())
				));
	}
	
	/**
	 * Subsititutes all occurrences of {{tag-name}} with the text parameter.
	 * @param tagName the tag name
	 * @param text the value to relace the tag with (e.g. a result from analysis)
	 * @return itself - a fluent method
	 * @throws IOException
	 */
	@RMethod
	public RoogleDocs updateTaggedText(String tagName, String text) throws IOException {
		rdoc().updateTaggedText(tagName, text);
		return this;
	}
	
	/**
	 * Subsitututes all occurrences of {{tag-name}} with an image from the local storage
	 * @param tagName the tag name
	 * @param absoluteFilePath a file path to an png imge file.
	 * @returnitself - a fluent method
	 * @throws IOException
	 */
	@RMethod
	public RoogleDocs updateTaggedImage(String tagName, String absoluteFilePath) throws IOException {
		String id = service.upload(Paths.get(absoluteFilePath));
		URI uri = service.getThumbnailUri(id);
		rdoc().updateTaggedImage(tagName, uri);
		service.delete(id);
		return this;
	}
	
	/**
	 * remove all text and images inserted by roogledocs and returns the bare document. 
	 * This is needed if content is being moved 
	 * @return itself - a fluent method
	 * @throws IOException
	 */
	@RMethod
	public RoogleDocs revertTags() throws IOException {
		rdoc().revertTags();
		return this;
	}
}
