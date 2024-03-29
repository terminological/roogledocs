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
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;

import org.github.terminological.roogledocs.datatypes.Tuple;
import org.jbibtex.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.services.docs.v1.model.Dimension;
import com.google.api.services.docs.v1.model.Size;

import de.undercouch.citeproc.CSL;
import uk.co.terminological.rjava.RClass;
import uk.co.terminological.rjava.RConverter;
import uk.co.terminological.rjava.RDefault;
import uk.co.terminological.rjava.RMethod;
import uk.co.terminological.rjava.UnconvertableTypeException;
import uk.co.terminological.rjava.types.RCharacter;
import uk.co.terminological.rjava.types.RCharacterVector;
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
@RClass(imports = {"dplyr","tidyr"}, suggests= {"here"}) //N.b. these imports should be detailed in 
public class RoogleDocs {

	RService service;
	RDocument document;
	boolean disabled;
	String tokenDirectory;
	static Logger log = LoggerFactory.getLogger(RoogleDocs.class);
	
	private RDocument rdoc() throws IOException {
		if (document == null) throw new IOException("The google document has not been defined yet - use the `withDocument()`, `findOrCreateDocument()` or `findOrCloneTemplate()` method"); 
		return document;
	}
	
	/**
	 * Create a Roogledocs object for managing the interaction.
	 * 
	 * @param tokenDirectory the place to store authentication tokens. This should not be checked into version control.
	 * @param disabled a flag to switch roogledocs off (on a document by document basis, for testing or development. This can be set globally with `options('roogledocs.disabled'=TRUE)`
	 * @throws IOException if there is a problem storing the tokens or communicating with google servers
	 * @throws GeneralSecurityException if there is a problem authenticating
	 */
	@RMethod
	public RoogleDocs(
		@RDefault(rCode = ".tokenDirectory()") String tokenDirectory,
		@RDefault(rCode = "getOption('roogledocs.disabled',FALSE)") boolean disabled
	) throws IOException, GeneralSecurityException {
		this.disabled = disabled;
		this.tokenDirectory = tokenDirectory;
		if(!disabled) service = RService.with(tokenDirectory);
	}
	
	// Testing
	protected RoogleDocs(RService service, RDocument document) {
		this.service = service;
		this.document = document;
		this.disabled = false;
		this.tokenDirectory = service.getTokenDirectory().toString();
		
	}
	
	/**
	 * Re-authenticate roogledocs library
	 * 
	 * Re-authenticate the service deleting the existing OAuth tokens may be helpful if there is some problem. 
	 * 
	 * Generally this is only be needed if  
	 * application permission updates are needed in which case the directory can be manually deleted anyway,
	 * or if you want to switch google user without using a different tokenDirectory.
	 * 
	 * 
	 * @param tokenDirectory the place to store authentication tokens. This should not be checked into version control.
	 * @return a new RoogleDocs instance without an active document
	 * @throws IOException if there is a problem deleting the old tokens
	 * @throws GeneralSecurityException if there is a problem authenticating
	 */
	@RMethod
	public static RoogleDocs reauth(@RDefault(rCode = ".tokenDirectory()") String tokenDirectory) throws IOException, GeneralSecurityException {
		RService.deregister(tokenDirectory.toString());
		RService service = RService.with(tokenDirectory.toString());
		return new RoogleDocs(service, null);
	}
	
	/**
	 * Enables roogledocs method calls for this document. 
	 * 
	 * It is likely one of `withDocument()`, `findOrCreateDocument()` or `findOrCloneTemplate()` methods will be needed to specify the document.
	 *  
	 * @return itself - a fluent method
	 * @throws GeneralSecurityException if the client cannot authenticate
	 * @throws IOException if there is a problem communicating with google servers
	 */
	@RMethod 
	public RoogleDocs enable() throws IOException, GeneralSecurityException {
		this.disabled = false;
		if (this.service == null) service = RService.with(tokenDirectory);
		return this;
	}
	
	/**
	 * Disables roogledocs temporarily for this document. 
	 * 
	 * While disabled all calls to roogledocs will silently abort. 
	 * @return itself - a fluent method
	 */
	@RMethod 
	public RoogleDocs disable() {
		this.disabled = true;
		return this;
	}
	
	
	
	/**
	 * Return the name of the document
	 * 
	 * @param suffix an additional suffix to add to the name
	 * @return
	 */
	@RMethod
	public String getName(@RDefault(rCode = "''") String suffix) {
		return this.document.getName() + suffix;
	}
	
	/**
	 * Select a document by its share url or id.
	 * 
	 * @param shareUrlOrDocId the url from clicking a share button in google docs or an id from searchForDocuments() method
	 * @return itself - a fluent method
	 * @throws IOException if there is a problem communicating with google servers, or the document does not exist, or is not a google doc
	 */
	public RoogleDocs withDocument(String shareUrlOrDocId) throws IOException {
		if (disabled) return this;
		this.document = service.getDocument(shareUrlOrDocId);
		return this;
	}
	
	/**
	 * Get a document by id or sharing link.
	 * 
	 * @param shareUrlOrDocId the url from clicking a share button in google docs or an id from searchForDocuments() method
	 * @param tokenDirectory the place to store authentication tokens. This should not be checked into version control.
	 * @param disabled a flag to switch roogledocs off (on a document by document basis, for testing or development. This can be set globally with `options('roogledocs.disabled'=TRUE)`
	 * @return itself - a fluent method
	 * @throws IOException if there is a problem communicating with google servers, or the document does not exist, or is not a google doc
	 * @throws GeneralSecurityException 
	 */
	@RMethod
	public static RoogleDocs docById(
			String shareUrlOrDocId,
			@RDefault(rCode = ".tokenDirectory()") String tokenDirectory,
			@RDefault(rCode = "getOption('roogledocs.disabled',FALSE)") boolean disabled
	) throws IOException, GeneralSecurityException {
		RoogleDocs out = new RoogleDocs(tokenDirectory, disabled);
		out.withDocument(shareUrlOrDocId);
		return out;
	}
	
	/**
	 * Search for a document by name or create one if missing.
	 * @param title a document title. If there is an exact match in google drive then that document will be used 
	 * otherwise a new one will be created.
	 * @return itself - a fluent method
	 * @throws IOException if there is a problem communicating with google servers
	 */
	public RoogleDocs findOrCreateDocument(String title) throws IOException {
		if (disabled) return this;
		this.document = service.getOrCreateDocument(title);
		return this;
	}
	
	/**
	 * Get a document by name or create a blank document if missing.
	 * 
	 * @param title a document title. If there is an exact match in google drive then that document will be used
	 * @param tokenDirectory the place to store authentication tokens. This should not be checked into version control.
	 * @param disabled a flag to switch roogledocs off (on a document by document basis, for testing or development. This can be set globally with `options('roogledocs.disabled'=TRUE)`
	 * @return itself - a fluent method
	 * @throws IOException if there is a problem communicating with google servers
	 * @throws GeneralSecurityException 
	 */
	@RMethod
	public static RoogleDocs docByName(
			String title,
			@RDefault(rCode = ".tokenDirectory()") String tokenDirectory,
			@RDefault(rCode = "getOption('roogledocs.disabled',FALSE)") boolean disabled
	) throws IOException, GeneralSecurityException {
		RoogleDocs out = new RoogleDocs(tokenDirectory, disabled);
		out.findOrCreateDocument(title);
		return out;
	}
	
	
	/**
	 * Get a document by name or create one from a template if missing.
	 * 
	 * @param title a document title. If there is an exact match in google drive then that document will be used
	 * otherwise a new one will be created.
	 * @param templateUri the share link (or document id) of a template google document 
	 * @return itself - a fluent method
	 * @throws IOException if there is a problem communicating with google servers, or the template uri is not correct
	 */
	public RoogleDocs findOrCloneTemplate(String title, String templateUri) throws IOException {
		if (disabled) return this;
		this.document = service.getOrCloneDocument(title, templateUri);
		return this;
	}
	
	/**
	 * Get a document by name or create one from a template if missing.
	 * 
	 * @param title a document title. If there is an exact match in google drive then that document will be used
	 * otherwise a new one will be created.
	 * @param templateUri the share link (or document id) of a template google document 
	 * @param tokenDirectory the place to store authentication tokens. This should not be checked into version control.
	 * @param disabled a flag to switch roogledocs off (on a document by document basis, for testing or development. This can be set globally with `options('roogledocs.disabled'=TRUE)`
	 * @return itself - a fluent method
	 * @throws IOException if there is a problem communicating with google servers
	 * @throws GeneralSecurityException 
	 */
	@RMethod
	public static RoogleDocs docFromTemplate(
			String title,
			String templateUri,
			@RDefault(rCode = ".tokenDirectory()") String tokenDirectory,
			@RDefault(rCode = "getOption('roogledocs.disabled',FALSE)") boolean disabled
	) throws IOException, GeneralSecurityException {
		RoogleDocs out = new RoogleDocs(tokenDirectory, disabled);
		out.findOrCloneTemplate(title, templateUri);
		return out;
	}
	
	/**
	 * Search for documents with the given title
	 * 
	 * @param titleMatch a string to be searched for as an approximate match. All results will be retrieved with document ids.
	 * @param tokenDirectory the place to store authentication tokens. This should not be checked into version control.
	 * @return a dataframe containing "id" and "name" columns
	 * @throws IOException if there is a problem communicating with google servers
	 * @throws GeneralSecurityException 
	 */
	@RMethod
	public static RDataframe searchForDocuments(
			String titleMatch, 
			@RDefault(rCode = ".tokenDirectory()") String tokenDirectory
	) throws IOException, GeneralSecurityException {
		RService service = RService.with(tokenDirectory);
		List<Tuple<String, String>> tmp = service.search(titleMatch, RService.MIME_DOCS);
		return tmp.stream().collect(
				dataframeCollector(
						mapping(Tuple.class, "id", t->t.getFirst()),
						mapping(Tuple.class, "name", t->t.getSecond())
				));
	}
	
	/**
	 * List all tags
	 * 
	 * Finds tags defined in the current document
	 * 
	 * @return a dataframe containing "tag" and "count" columns
	 * @throws IOException if there is a problem communicating with google servers, or the text is blank
	 */
	@RMethod 
	public RDataframe tagsDefined() throws IOException {
		if (disabled) throw new IOException("roogledocs is disabled");
		Map<String, List<TextRunPosition>> tmp = rdoc().updateInlineTags();
		return tmp.entrySet().stream().collect(
				dataframeCollector(
						mapping("tag", t->t.getKey()),
						mapping("count", t->t.getValue().size())
				));
	}
	
	/**
	 * Replace tags for text
	 * 
	 * Substitutes all occurrences of {{tag-name}} with the text parameter. If the tag name is not 
	 * found in the document it is inserted at the end in a section labelled "Unmatched tags:". From there
	 * it can be cut and pasted into the right place.
	 * 
	 * @param tagName the tag name
	 * @param text the value to replace the tag with (e.g. a result from analysis) (cannot be empty)
	 * @return itself - a fluent method
	 * @throws IOException if there is a problem communicating with google servers
	 */
	@RMethod
	public RoogleDocs updateTaggedText(String text, @RDefault(rCode = "deparse(substitute(text))") String tagName) throws IOException {
		if (disabled) return this;
		if (text == "") throw new IOException("text cannot be blank - use a single space for empty content.");
		rdoc().updateTaggedText(tagName, text);
		System.out.println("Text "+tagName+" updated");
		return this;
	}
	
	/**
	 * Replace a tag with an image.
	 * 
	 * Substitutes all occurrences of {{tag-name}} with an image from the local storage. 
	 * 
	 * The image is uploaded to your google drive as a temporary file, and briefly made publicly readable. From there it is inserted into the 
	 * google doc, and one completed the temporary file deleted from your google drive. Insertion is
	 * done using the dimensions of the existing image (if present) or the PNG dimensions if not.
	 * Creating the image with the correct dimensions (and providing the dpi if not 300) is important.
	 * 
	 * If the tag is missing from the document the image
	 * is inserted at the end (and tagged). From there it can be cut and pasted to the correct place.
	 * 
	 * @param tagName the tag name
	 * @param absoluteFilePath a file path to an png image file.
	 * @param dpi the dots per inch of the image in the document (defaults to 300)
	 * @param keepUpload keep the uploaded image as a supplementary file in the same directory as the google doc
	 * @return itself - a fluent method
	 * @throws IOException if there is a problem communicating with google servers, or there is a problem loading the image file
	 */
	@RMethod
	public RoogleDocs updateTaggedImage(String absoluteFilePath, @RDefault(rCode = "deparse(substitute(absoluteFilePath))") String tagName, @RDefault(rCode="300") double dpi, @RDefault(rCode="FALSE") boolean keepUpload) throws IOException {
		if (disabled) return this;
		Path path = Paths.get(absoluteFilePath);
		String name = rdoc().getName()+" - "+tagName;
		List<String> parents = this.service.getFileParents(rdoc().getDocId());
		String id = null; 
		if (keepUpload) {
			id = service.upload(name,path,parents,true,false);
		} else {
			id = service.uploadTmp(path);
		}
		URI uri = service.getThumbnailUri(id);
		rdoc().updateTaggedImage(tagName, uri, true, getImageDim(absoluteFilePath, dpi));
		System.out.println("Figure "+tagName+" updated");
		if (!keepUpload) service.delete(id);
		return this;
	}
	
	/**
	 * Replace a tag with a table.
	 * 
	 * Substitutes a unique occurrences of {{tag-name}} with a table. The tag should either be in the text of the document or as the first entry in 
	 * the first cell of a table. Once inserted the table is tagged using a zero width character
	 * as the very first item in the first cell. This will be removed if `removeTags()` is called.
	 * 
	 * If the tag is missing the table is inserted at the end of the document where it can be 
	 * cut and pasted to the correct place. 
	 * 
	 * @param tagName the tag name
	 * @param longFormatTable A dataframe consisting of the table content and formatting indexed by row and column. at a minimum this should have columns label,row,col, but may also include
	 * rowSpan,colSpan,fillColour, leftBorderWeight, rightBorderWeight, topBorderWeight, bottomBorderWeight, alignment (START,CENTER,END), valignment (TOP,MIDDLE,BOTTOM), fontName, fontFace, fontSize.
	 * @param colWidths A vector including the relative length of each column. This can be left out if longFormatTable comes from `as.long_format_table`
	 * @param tableWidthInches The final width of the table in inches (defaults to a size that fits in A4 page with margins) but you may want to make this wider for
	 *   landscape tables 
	 * @return itself - a fluent method
	 * @throws IOException if there is a problem communicating with google servers, or there is a problem loading the image file
	 * @throws UnconvertableTypeException if the longFormatTable is incorrectly structured.
	 */
	@RMethod
	public RoogleDocs updateTaggedTable(
			RDataframe longFormatTable, @RDefault(rCode = "deparse(substitute(longFormatTable))") String tagName, 
			@RDefault(rCode="attr(longFormatTable,'colWidths')") RNumericVector colWidths, @RDefault(rCode="6.2") RNumeric tableWidthInches
		) throws IOException, UnconvertableTypeException {
		if (disabled) return this;
		this.rdoc().updateTaggedTable(tagName, longFormatTable, colWidths, tableWidthInches);
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
	 * Revert tagged text and images.
	 * 
	 * Remove all tagged text and images inserted by roogledocs and returns the bare document the tags in place. This does not affect figures and tables inserted by index (i.e. without tags) 
	 * This is needed if content is being moved around as cut and paste of tagged content unfortunately removes the internal named range of the tag.
	 *  
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
	 * Remove all tags
	 * 
	 * Finds tags defined in the current document and removes them. This 
	 * cannot be undone, except by rolling back to a previous version.
	 * 
	 * @param confirm - This action must be confirmed by passing `true` as cannot be undone.
	 * @return itself - a fluent method
	 * @throws IOException if there is a problem communicating with google servers
	 */
	@RMethod 
	public RoogleDocs removeTags(@RDefault(rCode = "(menu(c('Yes','No'), title = 'Are you sure?')==1)") boolean confirm) throws IOException {
		if (disabled) throw new IOException("roogledocs is disabled");
		if (confirm) {
			rdoc().removeTags();
			System.out.println("All roogledoc tags removed from document.");
		} else {
			System.out.println("Removing tags aborted as confirmation not given.");
		}
		return this;
	}
	
	
	/**
	 * Update or insert a formatted table into the document. 
	 * 
	 * This function counts the number of tables from the start of the document.
	 * Inserting tables by index works only if your document does not change much or you are creating one from
	 * scratch. You can overwrite tables using this function but if the order of tables has been changed by
	 * your collaborators this will be generally cause probelms. Use of this function is generally discouraged and
	 * we now prefer `updateTaggedTable()`. 
	 * 
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
	 * Update or insert a figure in the document from a locally stored PNG by index.
	 * 
	 * This function counts the number of inline images (i.e. not absolutely positioned ones) from the start of the document.
	 * Inserting images by index works only if your document does not change much or you are creating one from
	 * scratch. You can overwrite images using this function but if the order of images has been changed by
	 * your collaborators this will generally cause problems. This function uploads the image into a temporary file onto your Google Drive, and makes it briefly publicly readable. From there inserts it into the 
	 * google document. Once this is complete the temporary google drive copy of the image is deleted. 
	 * 
	 * @param figureIndex what is the figure index in the document? (This only counts inline images - and ignores absolutely positioned ones). leave out for a new image at the end of the document. 
	 * @param absoluteFilePath a file path to an png image file (only png is supported at this point).
	 * @param dpi the dots per inch of the image in the document (defaults to 300). the final size of the image in the doc will be determined by the image file dimensions and the dpi.
	 * @param keepUpload keep the uploaded image as a supplementary file in the same directory as the google doc
	 * @return itself - a fluent method
	 * @throws IOException if there is a problem communicating with google servers, or the png file cannot be read.
	 */
	@RMethod
	public RoogleDocs updateFigure(String absoluteFilePath, @RDefault(rCode="-1") int figureIndex, @RDefault(rCode="300") double dpi, @RDefault(rCode="FALSE") boolean keepUpload) throws IOException {
		if (disabled) return this;
		Path path = Paths.get(absoluteFilePath);
		String name = rdoc().getName()+" - figure "+figureIndex;
		List<String> parents = this.service.getFileParents(rdoc().getDocId());
		String id = null; 
		if (keepUpload) {
			id = service.upload(name,path,parents,true,false);
		} else {
			id = service.upload(path);
		}
		URI uri = service.getThumbnailUri(id);
		int index = rdoc().updateOrInsertInlineImage(figureIndex, uri, getImageDim(absoluteFilePath, dpi));
		System.out.println("Figure "+index+" updated");
		if (!keepUpload) service.delete(id);
		return this;
	}
	
	/**
	 * Save the document as a PDF
	 * 
	 * Saves a snapshot of the current google doc with `roogledocs` links removed as a pdf to a local drive. 
	 * This is mainly intended for snapshotting the current state of the document. For final export once all
	 * analysis is complete it may be preferable to call `doc$removeTags()` and manually export the output
	 * but after this no further updating is possible.
	 * 
	 * @param absoluteFilePath - a file path to save the pdf.
	 * @param uploadCopy place a copy of the downloaded pdf back onto google drive in the same folder as the document
	 *   for example for keeping submitted versions of a updated document. This will overwrite files of the same name in the 
	 *   google drive directory.
	 * @return itself - a fluent method
	 * @throws IOException if there is a problem communicating with google servers, or the file cannot be saved.
	 */
	@RMethod
	public RoogleDocs saveAsPdf(String absoluteFilePath, @RDefault(rCode="FALSE") boolean uploadCopy) throws IOException {
		if (disabled) return this;
		RDocument newdoc = this.service.getOrCloneDocument("tmp_copy_for_pdf_"+UUID.randomUUID().toString(), document.getDocId());
		newdoc.removeTags();
		newdoc.saveAsPdf(absoluteFilePath);
		this.service.delete(newdoc.getDocId());
		if (uploadCopy) this.uploadSupplementaryFiles(absoluteFilePath, true, false);
		return this;
	}
	
	/**
	 * Upload a file into the same directory as the document.
	 * 
	 * This allow you to load e.g. a supplementary file, or the pdf of an image file or a docx/html version of a table
	 * into google drive into the same directory as the document you are editing. This is handy for organising all the files
	 * for a journal submission in one place. Any kind of file can be loaded, and the mimetype will be detected. Normal Google Drive rules 
	 * for uploads will be triggered at this point. As google drive can have multiple files with the same name
	 * the behaviour if the file already exists is slightly complex, with `overwrite` and `duplicate` options. 
	 * 
	 * @param absoluteFilePath - a file path to upload.
	 * @param overwrite - if matching file(s) are found in the target, delete them before uploading the new one.
	 * @param duplicate - if matching file(s) are found in the target, upload this new file anyway, creating duplicate names in the folder.
	 * @return itself - a fluent method
	 * @throws IOException if there was a problem with finding the file or uploading it
	 */
	@RMethod
	public RoogleDocs uploadSupplementaryFiles(String absoluteFilePath, @RDefault(rCode="FALSE") boolean overwrite, @RDefault(rCode="FALSE") boolean duplicate) throws IOException {
		if (disabled) return this;
		//TODO: detect naming collisions and allow overwriting?
		Path path = Paths.get(absoluteFilePath);
		String name = path.getFileName().toString();
		List<String> parents = this.service.getFileParents(rdoc().getDocId());
		this.service.upload(name, path, parents, overwrite, duplicate);
		return this;
	}
	
	
	
	/**
	 * Deletes a google document by name. 
	 * @param docName - the name of a document to delete. must be an exact and unique match.
	 * @param areYouSure - a boolean check.
	 * @return nothing, called for side efffects
	 * @throws IOException if there is a problem communicating with google servers, or the file cannot be saved.
	 * @throws GeneralSecurityException 
	 */
	@RMethod
	public static void deleteDocument(
			String docName, 
			@RDefault(rCode = "utils::askYesNo(paste0('Are you sure you want to delete ',docName),FALSE)") boolean areYouSure,
			@RDefault(rCode = ".tokenDirectory()") String tokenDirectory,
			@RDefault(rCode = "getOption('roogledocs.disabled',FALSE)") boolean disabled
	) throws IOException, GeneralSecurityException {
		if (disabled) return;
		if (areYouSure) RService.with(tokenDirectory).deleteByName(docName, RService.MIME_DOCS);
		else System.out.println("aborted delete.");
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
	
	/**
	 * List the supported citation styles 
	 * 
	 * @return a vector of csl style names
	 * @throws IOException if the citation styles cannot be found
	 */
	@RMethod
	public static RCharacterVector citationStyles() throws IOException {
		return RConverter.convert(CSL.getSupportedStyles().toArray(new String[] {}));
	}
	
	/**
	 * Update citation tags in the document. 
	 * 
	 * A citation tag is like this `{{cite:challen2020;danon2021}}`. The ids are matched against the provided bibtex, and
	 * the tags are replaced with an appropriate citation string. The bibliography itself is added to a specific slide for 
	 * references which can be decided with the `{{references}}` tag. 
	 * 
	 * If references do not already exist and there if no `{{references}}` tag the
	 * references will be added to the end of the document. Where it can be cut and
	 * pasted to the right place. N.B. Do not split up the references when you move them.
	 *  
	 * @param bibTexPath - the full file path to the file containing the bibtex 
	 * @param citationStyle - the CSL specification
	 * @return itself - a fluent method
	 * @throws IOException if there is a problem communicating with google servers.
	 * @throws ParseException if the bibTex is poorly formed
	 */
	@RMethod
	public RoogleDocs updateCitations(RCharacter bibTexPath, @RDefault(rCode = "'ieee-with-url'") RCharacter citationStyle) throws IOException, ParseException {
		if (disabled) return this;
		
		// setup 
		String bibTex = Files.readString(Paths.get(bibTexPath.toString()));
		if (!CSL.supportsStyle(citationStyle.get())) throw new IOException("Unsupported citation style:"+citationStyle.get());
		document.updateCitations(bibTex, citationStyle.get());
				
		return this;
	}
}
