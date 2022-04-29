package org.github.terminological.roogledocs;

import static uk.co.terminological.rjava.RConverter.dataframeCollector;
import static uk.co.terminological.rjava.RConverter.mapping;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
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
import uk.co.terminological.rjava.types.RCharacter;
import uk.co.terminological.rjava.types.RDataframe;
import uk.co.terminological.rjava.types.RNumeric;
import uk.co.terminological.rjava.types.RNumericVector;

/**
 * Programmatically substitute images, data and tables into a google doc. 
 * R library to perform limited interactions with google docs (and maybe one day slides)
 * in R via the Java API library. The purpose being to support google docs as a
 * platform for interactive development and documentation of data analysis in R for scientific
 * publication, although it is not limited to this purpose. The workflow supported is a parallel documentation and analysis
 * where a team of people are working collaboratively on documentation, whilst at the same time analysis
 * is being performed and results updated repeatedly as a result of new data. In this environment updating
 * numeric results, tabular data and figures in word documents manually becomes annoying. With roogledocs
 * you can automate this a bit like a RMarkdown document, but with the added benefit that the content
 * can be updated independently of the analysis, by the wider team.
 * 
 */
@RClass(imports = {"tidyverse","huxtable"}, suggests= {"here"})
public class RoogleDocs {

	RService service;
	RDocument document;
	boolean disabled;
	String tokenDirectory;
	
	private RDocument rdoc() throws IOException {
		if (document == null) throw new IOException("The google document has not been defined yet - use the `withDocument()`, `findOrCreateDocument()` or `findOrCloneTemplate()` method"); 
		return document;
	}
	
	/**
	 * 
	 * @param tokenDirectory the place to store authentication tokens. This should not be checked into version control.
	 * @param disabled a flag to switch roogledocs off (on a document by document basis, for testing or development. This can be set globally with `options('roogledocs.disabled'=TRUE)`
	 * @throws IOException if there is a problem storing the tokens or communicating with google servers
	 * @throws GeneralSecurityException if there is a problem authenticating
	 */
	@RMethod
	public RoogleDocs(
		@RDefault(rCode = "normalizePath('~/.roogledocs', mustWork = FALSE)") String tokenDirectory,
		@RDefault(rCode = "getOption('roogledocs.disabled',FALSE)") boolean disabled
	) throws IOException, GeneralSecurityException {
		this.disabled = disabled;
		this.tokenDirectory = tokenDirectory;
		if(!disabled) service = new RService(tokenDirectory);
	}
	
	/**
	 * Reauthenticate the service deleting the existing OAuth tokens. Generally this would only be needed if 
	 * application permission updates are needed in which case the directory can be manually deleted anyway,
	 * or if you want to switch google user without using a different tokenDirectory.  
	 * @return a fluent method
	 * @throws IOException if there is a problem deleting the old tokens
	 * @throws GeneralSecurityException if there is a problem authenticating
	 */
	@RMethod
	public RoogleDocs reauth() throws IOException, GeneralSecurityException {
		if (disabled) return this;
		Path tokenDirectory = service.getTokenDirectory();
		Files.walk(tokenDirectory)
			.filter(f -> !Files.isDirectory(f))
			.forEach(f -> {
				try {
					Files.delete(f);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			});
		service = new RService(tokenDirectory);
		return this;
	}
	
	/**
	 * Enables roogledocs. It is likely one of `withDocument()`, `findOrCreateDocument()` or `findOrCloneTemplate()` methods will be needed to specify the document. 
	 * @return itself - a fluent method
	 * @throws GeneralSecurityException if the client cannot authenticate
	 * @throws IOException if there is a problem communicating with google servers
	 */
	@RMethod 
	public RoogleDocs enable() throws IOException, GeneralSecurityException {
		this.disabled = false;
		if (this.service == null) service = new RService(tokenDirectory);
		return this;
	}
	
	/**
	 * disables roogledocs temporarily only for this document. While disabled all calls to roogledocs will silently fail. 
	 * @return itself - a fluent method
	 */
	@RMethod 
	public RoogleDocs disable() {
		this.disabled = true;
		return this;
	}
	
	/**
	 * Select a document by its share url or id.
	 * @param shareUrlOrDocId the url from clicking a share button in google docs or an id from searchForDocuments() method
	 * @return itself - a fluent method
	 * @throws IOException if there is a problem communicating with google servers
	 */
	@RMethod
	public RoogleDocs withDocument(String shareUrlOrDocId) throws IOException {
		if (disabled) return this;
		this.document = service.getDocument(shareUrlOrDocId);
		return this;
	}
	
	/**
	 * Search for or create a document if it is missing.
	 * @param title a document title. If there is an exact match in google drive then that document will be used 
	 * otherwise a new one will be created.
	 * @return itself - a fluent method
	 * @throws IOException if there is a problem communicating with google servers
	 */
	@RMethod
	public RoogleDocs findOrCreateDocument(String title) throws IOException {
		if (disabled) return this;
		this.document = service.getOrCreate(title);
		return this;
	}
	
	/**
	 * Search for or create a document as a copy of a template document if it is missing.
	 * @param title a document title. If there is an exact match in google drive then that document will be used
	 * otherwise a new one will be created.
	 * @param templateUri the share link (or document id) of a template google document 
	 * @return itself - a fluent method
	 * @throws IOException if there is a problem communicating with google servers, or the template uri is not correct
	 */
	@RMethod
	public RoogleDocs findOrCloneTemplate(String title, String templateUri) throws IOException {
		if (disabled) return this;
		this.document = service.getOrClone(title, templateUri);
		return this;
	}
	
	/**
	 * Search for documents with the given title
	 * @param titleMatch a string to be searched for as an approximate match. All results will be retrieved with document ids.
	 * @return a dataframe containing "id" and "name" columns
	 * @throws IOException if there is a problem communicating with google servers
	 */
	@RMethod
	public RDataframe searchForDocuments(String titleMatch) throws IOException {
		if (disabled) throw new IOException("roogledocs is disabled");
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
	 * @throws IOException if there is a problem communicating with google servers, or the text is blank
	 */
	@RMethod 
	public RDataframe tagsDefined() throws IOException {
		if (disabled) throw new IOException("roogledocs is disabled");
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
	 * @param text the value to replace the tag with (e.g. a result from analysis) (cannot be empty)
	 * @return itself - a fluent method
	 * @throws IOException if there is a problem communicating with google servers
	 */
	@RMethod
	public RoogleDocs updateTaggedText(String text, String tagName) throws IOException {
		if (disabled) return this;
		if (text == "") throw new IOException("text cannot be blank - use a single space for empty content.");
		rdoc().updateTaggedText(tagName, text);
		System.out.println("Text "+tagName+" updated");
		return this;
	}
	
	/**
	 * Substitutes all occurrences of {{tag-name}} with an image from the local storage. There are limited circumstances
	 * in which using this is a good idea. It will almost always be better to use `updateFigure()` to insert an image
	 * by index. If you choose to ignore this warning, beware combining this with `updateFigure()` as potentially the figure indexes will
	 * change dynamically. You have been warned.
	 * @param tagName the tag name
	 * @param absoluteFilePath a file path to an png image file.
	 * @param dpi the dots per inch of the image in the document (defaults to 300)
	 * @return itself - a fluent method
	 * @throws IOException if there is a problem communicating with google servers, or there is a problem loading the image file
	 */
	@RMethod
	public RoogleDocs updateTaggedImage(String absoluteFilePath, String tagName, @RDefault(rCode="300") double dpi) throws IOException {
		if (disabled) return this;
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
	
	protected static Size getImageDim(final String path, double dpi) throws IOException {
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
	            				new Dimension().setMagnitude(width/dpi*72).setUnit("PT")
	            		)
	            		.setHeight(
	            				new Dimension().setMagnitude(height/dpi*72).setUnit("PT")
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
	 * remove all tagged text and images inserted by roogledocs and returns the bare document. This does not affect figures and tables inserted by index (i.e. without tags) 
	 * This is needed if content is being moved around as cut and paste of tagged content unfortunately removes the internal named range of the tag. 
	 * @return itself - a fluent method
	 * @throws IOException if there is a problem communicating with google servers
	 */
	@RMethod
	public RoogleDocs revertTags() throws IOException {
		if (disabled) return this;
		rdoc().revertTags();
		return this;
	}
	
	/**
	 * Update or insert a formatted table into the document. The table and formatting are described in a dataframe the format of which is documented in the as.long_format_table() method.
	 * @param tableIndex what is the table index in the document? This can be left out for a new table at the end of the document.
	 * @param longFormatTable A dataframe consisting of the table content and formatting indexed by row and column. at a minimum this should have columns label,row,col, but may also include
	 * rowSpan,colSpan,fillColour, leftBorderWeight, rightBorderWeight, topBorderWeight, bottomBorderWeight, alignment (START,CENTER,END), valignment (TOP,MIDDLE,BOTTOM), fontName, fontFace, fontSize.
	 * @param colWidths A vector including the relative length of each column. This can be left out if longFormatTable comes from as.long_format_table
	 * @param tableWidthInches The final width of the table in inches (defaults to a size that fits in A4 page with margins)  
	 * @return itself - a fluent method
	 * @throws IOException if there is a problem communicating with google servers
	 * @throws UnconvertableTypeException if the longFormatTable data frame is the wrong format.
	 */
	@RMethod
	public RoogleDocs updateTable(RDataframe longFormatTable, @RDefault(rCode="-1") int tableIndex, @RDefault(rCode="attr(longFormatTable,'colWidths')") RNumericVector colWidths, @RDefault(rCode="6.2") RNumeric tableWidthInches) throws IOException, UnconvertableTypeException {
		if (disabled) return this;
		int index = rdoc().updateOrInsertTable(tableIndex, longFormatTable, colWidths, tableWidthInches);
		System.out.println("Table "+index+" updated");
		return this;
	}
	
	/**
	 * Update or insert a figure in the document. 
	 * @param figureIndex what is the figure index in the document? (This only counts inline images - and ignores absolutely positioned ones). leave out for a new image at the end of the document. 
	 * @param absoluteFilePath a file path to an png image file (only png is supported at this point).
	 * @param dpi the dots per inch of the image in the document (defaults to 300). the final size of the image in the doc will be determined by the image file dimensions and the dpi.
	 * @return itself - a fluent method
	 * @throws IOException if there is a problem communicating with google servers, or the png file cannot be read.
	 */
	@RMethod
	public RoogleDocs updateFigure(String absoluteFilePath, @RDefault(rCode="-1") int figureIndex, @RDefault(rCode="300") double dpi) throws IOException {
		if (disabled) return this;
		String id = service.upload(Paths.get(absoluteFilePath));
		URI uri = service.getThumbnailUri(id);
		int index = rdoc().updateOrInsertInlineImage(figureIndex, uri, getImageDim(absoluteFilePath, dpi));
		System.out.println("Figure "+index+" updated");
		service.delete(id);
		return this;
	}
	
	/**
	 * Saves a snapshot of the current google doc as a pdf to a local drive. This is mainly intended for testing. 
	 * @param absoluteFilePath - a file path to save the pdf.
	 * @return itself - a fluent method
	 * @throws IOException if there is a problem communicating with google servers, or the file cannot be saved.
	 */
	@RMethod
	public RoogleDocs saveAsPdf(String absoluteFilePath) throws IOException {
		if (disabled) return this;
		document.saveAsPdf(absoluteFilePath);
		return this;
	}
	
	/**
	 * Deletes a google document by name. 
	 * @param docName - the name of a document to delete. must be an exact and unique match.
	 * @param areYouSure - a boolean check.
	 * @return itself - a fluent method
	 * @throws IOException if there is a problem communicating with google servers, or the file cannot be saved.
	 */
	@RMethod
	public RoogleDocs deleteDocument(String docName, @RDefault(rCode="utils::askYesNo(paste0('Are you sure you want to delete ',docName),FALSE)") boolean areYouSure) throws IOException {
		if (disabled) return this;
		if (areYouSure) service.deleteByName(docName);
		else System.out.println("aborted delete.");
		return this;
	}
	
	/**
	 * Append text to the document with optional paragraph styling. If you run text blocks into each other without newlines the whole resulting paragraph will be styled. You 
	 * would normally not want this so it is up to you to end paragraphs with a new line character, before changing styles.
	 * @param text - a single string with the text to append which may include newlines
	 * @param style - one of NORMAL_TEXT, TITLE, SUBTITLE, HEADING_1, ... HEADING_6
	 * @return itself - a fluent method
	 * @throws IOException if there is a problem communicating with google servers.
	 */
	@RMethod
	public RoogleDocs appendText(RCharacter text, @RDefault(rCode="'NORMAL_TEXT'") RCharacter style) throws IOException {
		if (disabled) return this;
		if(!text.isNa()) document.appendText(text.get(), style.opt());
		return this;
	}
	
	
	/**
	 * Append a new paragraph, with text from the 'label' column with optional formating in the other columns.
	 * @param formattedTextDf - a data frame containing the columns label, and optionally: link (as a URL), fontName, fontFace, fontSize.
	 * @return itself - a fluent method
	 * @throws IOException if there is a problem communicating with google servers.
	 * @throws UnconvertableTypeException if the dataframe format is not correct
	 */
	@RMethod
	public RoogleDocs appendFormattedParagraph(RDataframe formattedTextDf) throws IOException, UnconvertableTypeException {
		if (disabled) return this;
		document.appendText(formattedTextDf);
		return this;
	}
}
