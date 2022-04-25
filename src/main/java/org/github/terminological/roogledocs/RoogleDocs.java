package org.github.terminological.roogledocs;

import static uk.co.terminological.rjava.RConverter.dataframeCollector;
import static uk.co.terminological.rjava.RConverter.mapping;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;

import org.github.terminological.roogledocs.datatypes.Tuple;
import org.github.terminological.roogledocs.datatypes.TupleList;

import com.google.api.services.docs.v1.model.Dimension;
import com.google.api.services.docs.v1.model.Size;

import uk.co.terminological.rjava.RClass;
import uk.co.terminological.rjava.RDefault;
import uk.co.terminological.rjava.RMethod;
import uk.co.terminological.rjava.UnconvertableTypeException;
import uk.co.terminological.rjava.types.RDataframe;
import uk.co.terminological.rjava.types.RNumeric;
import uk.co.terminological.rjava.types.RNumericVector;

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
	 * Substitutes all occurrences of {{tag-name}} with the text parameter.
	 * @param tagName the tag name
	 * @param text the value to replace the tag with (e.g. a result from analysis)
	 * @return itself - a fluent method
	 * @throws IOException
	 */
	@RMethod
	public RoogleDocs updateTaggedText(String tagName, String text) throws IOException {
		rdoc().updateTaggedText(tagName, text);
		System.out.println("Text "+tagName+" updated");
		return this;
	}
	
	/**
	 * Substitutes all occurrences of {{tag-name}} with an image from the local storage
	 * @param tagName the tag name
	 * @param absoluteFilePath a file path to an png image file.
	 * @param dpi the dots per inch of the image in the document (defaults to 300)
	 * @return itself - a fluent method
	 * @throws IOException
	 */
	@RMethod
	public RoogleDocs updateTaggedImage(String tagName, String absoluteFilePath, @RDefault(rCode="300") double dpi) throws IOException {
		String id = service.upload(Paths.get(absoluteFilePath));
		URI uri = service.getThumbnailUri(id);
		rdoc().updateTaggedImage(tagName, uri, true, getImageDim(absoluteFilePath, dpi));
		System.out.println("Figure "+tagName+" updated");
		service.delete(id);
		return this;
	}
	
	private static String getFileSuffix(final String path) {
	    String result = null;
	    if (path != null) {
	        result = "";
	        if (path.lastIndexOf('.') != -1) {
	            result = path.substring(path.lastIndexOf('.'));
	            if (result.startsWith(".")) {
	                result = result.substring(1);
	            }
	        }
	    }
	    return result;
	}
	
	private static Size getImageDim(final String path, double dpi) throws IOException {
		Size result = null;
	    String suffix = getFileSuffix(path);
	    Iterator<ImageReader> iter = ImageIO.getImageReadersBySuffix(suffix);
	    if (iter.hasNext()) {
	        ImageReader reader = iter.next();
	        try {
	            ImageInputStream stream = new FileImageInputStream(new File(path));
	            reader.setInput(stream);
	            int width = reader.getWidth(reader.getMinIndex());
	            int height = reader.getHeight(reader.getMinIndex());
	            result = new Size()
	            		.setWidth(
	            				new Dimension().setMagnitude(width*dpi/72).setUnit("PT")
	            		)
	            		.setHeight(
	            				new Dimension().setMagnitude(height*dpi/72).setUnit("PT")
	            		);
	        } finally {
	            reader.dispose();
	        }
	    } else {
	        throw new IOException("No reader found for given format: " + suffix);
	    }
	    return result;
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
	
	/**
	 * @param tableIndex what is the table index in the document? leave out for a new table at the end of the document.
	 * @param longFormatTable A dataframe consisting of the table content and formatting indexed by row and column
	 * @return
	 * @throws IOException if the google docs API throws an error 
	 * @throws UnconvertableTypeException if the data frame is the wrong format.
	 */
	@RMethod
	public RoogleDocs updateTable(@RDefault(rCode="-1") int tableIndex, RDataframe longFormatTable, RNumericVector colWidths, @RDefault(rCode="5.9") RNumeric tableWidthInches) throws IOException, UnconvertableTypeException {
		int index = rdoc().updateOrInsertTable(tableIndex, longFormatTable, colWidths, tableWidthInches);
		System.out.println("Table "+index+" updated");
		return this;
	}
	
	/**
	 * @param figureIndex what is the figure index in the document (only counts inline images - not absolutely positioned ones)? leave out for a new image at the end of the document. 
	 * @param absoluteFilePath a file path to an png image file.
	 * @param dpi the dots per inch of the image in the document (defaults to 300)
	 * @return itself (a fluent method)
	 * @throws IOException if the google docs API throws an error, or the file cannot be read.
	 */
	@RMethod
	public RoogleDocs updateFigure(@RDefault(rCode="-1") int figureIndex, String absoluteFilePath, @RDefault(rCode="300") double dpi) throws IOException {
		String id = service.upload(Paths.get(absoluteFilePath));
		URI uri = service.getThumbnailUri(id);
		int index = rdoc().updateOrInsertInlineImage(figureIndex, uri, getImageDim(absoluteFilePath, dpi));
		System.out.println("Figure "+index+" updated");
		service.delete(id);
		return this;
	}
}
