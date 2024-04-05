package org.github.terminological.roogledocs;

import static org.github.terminological.roogledocs.DocumentHelper.elements;
import static org.github.terminological.roogledocs.DocumentHelper.firstTableText;
import static org.github.terminological.roogledocs.DocumentHelper.imageSizes;
import static org.github.terminological.roogledocs.DocumentHelper.text;
import static org.github.terminological.roogledocs.StreamHelper.ofNullable;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.github.terminological.roogledocs.datatypes.LongFormatTable;
import org.github.terminological.roogledocs.datatypes.LongFormatText;
import org.github.terminological.roogledocs.datatypes.TupleList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.services.docs.v1.model.DeleteContentRangeRequest;
import com.google.api.services.docs.v1.model.Dimension;
import com.google.api.services.docs.v1.model.Document;
import com.google.api.services.docs.v1.model.ParagraphStyle;
import com.google.api.services.docs.v1.model.ReplaceAllTextRequest;
import com.google.api.services.docs.v1.model.Request;
import com.google.api.services.docs.v1.model.Size;
import com.google.api.services.docs.v1.model.SubstringMatchCriteria;
import com.google.api.services.docs.v1.model.Table;
import com.google.api.services.docs.v1.model.UpdateParagraphStyleRequest;

import uk.co.terminological.rjava.UnconvertableTypeException;
import uk.co.terminological.rjava.types.RBoundDataframe;
import uk.co.terminological.rjava.types.RDataframe;
import uk.co.terminological.rjava.types.RNumeric;
import uk.co.terminological.rjava.types.RNumericVector;
import uk.co.terminological.rjava.utils.RFunctions;

public class RDocument extends RCitable {

	RService service;
	String docId;
	String name;
	
	private Logger log = LoggerFactory.getLogger(RDocument.class);

	Document getDoc(String fields) throws IOException {
		return service.getDocs().documents().get(docId).setFields(fields).execute();
	}

	protected static String INLINE_FIELDS = "body(content(startIndex,endIndex,paragraph(elements(endIndex,startIndex,textRun(content))))),namedRanges";
	protected static String STRUCTURAL_ELEMENTS = "body(content(startIndex,endIndex,paragraph(elements(endIndex,startIndex,textRun(content))),table(columns,rows)))";
	protected static String TEXT_LINK_ELEMENTS = "body(content(startIndex,endIndex,paragraph(elements(endIndex,startIndex,textRun(content,textStyle(link(url)))))))";
	//protected static String TEXT_AND_IMAGE_LINK_ELEMENTS = "body(content(startIndex,endIndex,paragraph(elements(endIndex,startIndex,textRun(content,textStyle(link(url))),inlineObjectElement(textStyle(link(url))))),table(tableRows(tableCells(endIndex,startIndex,content(paragraph(elements(endIndex,startIndex,textRun(content,textStyle(link(url))))))))))";
	protected static String TEXT_AND_IMAGE_LINK_ELEMENTS = "body(content(startIndex,endIndex,paragraph(elements(endIndex,startIndex,textRun(content,textStyle(link(url))),inlineObjectElement(textStyle(link(url))))),table(tableRows(tableCells(endIndex,startIndex,content(paragraph(elements(endIndex,startIndex,textRun(content,textStyle(link(url)))))))))))";
	
	protected static String TABLES_ONLY = "body(content(startIndex,endIndex,table(columns,rows)))";
	protected static String TABLES_AND_CELLS = "body(content(startIndex,endIndex,table(tableRows(tableCells(endIndex,startIndex,tableCellStyle(columnSpan,rowSpan))))))"; //(tableCells(content(startIndex,endIndex,tableCellStyle(columnSpan,rowSpan))))))";
	protected static String IMAGE_POSITIONS = "inlineObjects(*),body(content(startIndex,endIndex,paragraph(elements(endIndex,startIndex,inlineObjectElement(*)))))";
	// protected static String IMAGE_SIZES = "body(content(startIndex,endIndex,paragraph(elements(endIndex,startIndex,inlineObjectElement(textStyle(link(url)))))))";
	protected static String MINIMAL = "body(content(startIndex,endIndex))";
	protected static String NAMED_RANGES = "namedRanges";
	
	//private static String LINK_FIELDS = "body(content(paragraph(elements(endIndex,startIndex,textRun(content,textStyle/link/url)))))";

	Document getDoc() throws IOException {
		return service.getDocs().documents().get(docId).setSuggestionsViewMode("SUGGESTIONS_INLINE").execute();
	}

	public RDocument(String docId, String docName, RService service) {
		super();
		this.service = service;
		this.docId = docId;
		this.name = docName;
	}


	// Get rid of same place but wrong name links.
	private static void findAndRemoveOld(Map<String, List<TextRunPosition>> tl, String newName, TextRunPosition matchPosition) {
		tl.forEach((k,v) -> {
			if (!k.equals(newName)) {
				v.remove(matchPosition);
			}
		});
		tl.entrySet().removeIf(e -> e.getValue().size()==0);
	}
	
	/**
	 * Fetches document content and scans it for text runs containing links and {{tags}}
	 * Creates links for test that is in the correct format. 
	 * @return 
	 * @throws IOException
	 */
	public Map<String,List<TextRunPosition>> updateInlineTags() throws IOException {
		Document doc = getDoc(TEXT_AND_IMAGE_LINK_ELEMENTS);
		DocumentRequestBuilder requests = new DocumentRequestBuilder(this);
		
		Map<String, List<TextRunPosition>> tl = DocumentHelper.findLinks(doc);
		
		Pattern r = Pattern.compile("\\{\\{([^\\}]+)\\}\\}");
		
		elements(doc)
			.forEach(e -> {
				text(e)
				.forEach(s -> {
					//Pattern r = Pattern.compile("\\{\\{"+tagName+"([^\\}]*)\\}\\}");
					
					Matcher m = r.matcher(s);
					while(m.find()) {
						String name =  s.substring(m.start(1), m.end(1));
						
						if (!tl.containsKey(name))	tl.put(name, new ArrayList<>());
						List<TextRunPosition> tl2 = tl.get(name);
						TextRunPosition matchPosition = TextRunPosition.of(e.getStartIndex()+m.start(), e.getStartIndex()+m.end());

						if (!tl2.contains(matchPosition)) {
							// The tag text is not matching existing link names at this position.
							requests.createLinkTag(name, matchPosition);
							tl2.add(matchPosition);
						} else {
							// The tag is present and a matching link exists at this position.
						}
						
						// Q: What happens if a linked tag is updated changing the tag name 
						// but the link is still in place with the old name.
						// A: The link is updated as the tag name is not matched by tl.containsKey
						// However the OLD name is still present in tl pointing to the same place.
						// remove it:
						findAndRemoveOld(tl, name, matchPosition);
					}
				});
			});
		
		firstTableText(doc)
			.forEach(el -> {
				text(el.getSecond()).forEach(s->{
					
					Matcher m = r.matcher(s);
					while(m.find()) {
						String name =  s.substring(m.start(1), m.end(1));
						
						if (!tl.containsKey(name))	tl.put(name, new ArrayList<>());
						List<TextRunPosition> tl2 = tl.get(name);
						TextRunPosition matchPosition = TextRunPosition.of(el.getSecond().getStartIndex()+m.start(), el.getSecond().getStartIndex()+m.end());
						TextRunPosition tableMatch = TextRunPosition.of(el.getFirst().getStartIndex(), el.getFirst().getEndIndex());	

						if (!tl2.contains(tableMatch)) {
							// The tag text is not matching existing link names at this position.
							requests.createLinkTag(name, matchPosition);
							tl2.add(tableMatch);
						} else {
							// The tag is present and a matching link exists at this position.
						}
						
						// Q: What happens if a linked tag is updated changing the tag name 
						// but the link is still in place with the old name.
						// A: The link is updated as the tag name is not matched by tl.containsKey
						// However the OLD name is still present in tl pointing to the same place.
						// remove it:
						findAndRemoveOld(tl, name, tableMatch);
					}
					
					
				});
			});

		requests.sendRequest();
		
		return tl;

	}
	
	public void updateTaggedText(Map<String,String> tagMap) throws IOException {

		// Find the matching named ranges.
		Map<String, List<TextRunPosition>> lm = updateInlineTags();
		
		// Create a reverse order map of ranges to update and the tags to update with 
		TreeMap<TextRunPosition,String> allRanges = new TreeMap<>(new TextRunPosition.Compare().reversed());
		List<String> unmatched = new ArrayList<>();
		
		// add all the ranges in the tag map
		tagMap.forEach((tag,value) -> {
			if (lm.containsKey(tag)) {
				lm.get(tag).forEach(t -> {
					allRanges.put(
						t, 
						tag
					);
				});
			} else {
				unmatched.add(tag);
			}
		});
		
		DocumentRequestBuilder requests = new DocumentRequestBuilder(this);
		
		//Deal with unmatched and place at end of document
		if (!unmatched.isEmpty()) {
			Collections.reverse(unmatched);
			Document document = getDoc(MINIMAL);
			TextRunPosition end = endPos(document);
			for (String tagName: unmatched) {
				log.info("tag '"+tagName+"' not found in document. adding to end.");
				String tagDesc = "\n"+tagName+": ";
				String newText = tagMap.get(tagName);
				TextRunPosition newRange = requests.insertTextContent(end, newText, Optional.of("NORMAL_TEXT"));
				requests.createLinkTag(tagName, newRange);
				requests.insertTextContent(end, tagDesc, Optional.empty());
			}
			
		}
		
		// Create a sequence of requests for each range in reverse document order, so we can apply in one go.
		
		for (TextRunPosition range : allRanges.navigableKeySet()) {
			// Delete all the content in the existing range.
			requests.deleteContent(range);
			String tagName = allRanges.get(range);
			String newText = tagMap.get(tagName);
			// Insert the replacement text.
			TextRunPosition newRange = requests.insertTextContent(range, newText, Optional.empty());
			// Re-create the roogledocs hyperlink on the new text.
			requests.createLinkTag(tagName, newRange);
		}

		requests.sendRequest();
	}
	
	public void updateTaggedText(String tagName, String newText) throws IOException {

		// Fetch the document to determine the current indexes of the named ranges.
		// Find the matching named ranges.
		log.info("Autotext replacing: {{"+tagName+"}} with "+newText);
		
		Map<String,String> toUpdate = new HashMap<>();
		toUpdate.put(tagName, newText);
		updateTaggedText(toUpdate);

	}

	public void updateTaggedImage(String tagName, URI imageLink) throws IOException {
		updateTaggedImage(tagName, imageLink, true, null);
	}
	
	public void updateTaggedImage(String tagName, URI imageLink, Double maxWidthInInches, Double maxHeightInInches) throws IOException {
		Size size = new Size()
				.setWidth(new Dimension().setMagnitude(maxWidthInInches*72).setUnit("PT"))
				.setHeight(new Dimension().setMagnitude(maxHeightInInches*72).setUnit("PT"));
		updateTaggedImage(tagName, imageLink, false, size);
	}
	
	public void updateTaggedImage(String tagName, URI imageLink, boolean useGoogleDocSize, Size size) throws IOException {

		// Fetch the document to determine the current indexes of the named ranges.
		// Find the matching named ranges.
		log.info("Autotext replacing: {{"+tagName+"}} with image: "+imageLink);
		Map<String, List<TextRunPosition>> lm = updateInlineTags();
		
		Document document = getDoc(IMAGE_POSITIONS);
		
		List<TextRunPosition> allRanges = new ArrayList<>(lm.getOrDefault(tagName, new ArrayList<>()));
		
		if (allRanges.isEmpty()) {
			log.info("tag '"+tagName+"' not found in document. adding image at end.");
			allRanges.add(endPos(document));
		}
		
		// Sort the list of ranges by startIndex, in descending order.
		allRanges.sort(new TextRunPosition.Compare().reversed());

		
		// System.out.print(document.toPrettyString());
		Map<Integer,Size> imageSizes = imageSizes(document);
		
		// Create a sequence of requests for each range.
		DocumentRequestBuilder requests = new DocumentRequestBuilder(this);
		for (TextRunPosition range : allRanges) {
			// Delete all the content in the existing range (including link).
			if (!range.isEmpty()) {
				requests.add(
					new Request().setDeleteContentRange(new DeleteContentRangeRequest().setRange(range.getDocsRange())));
			}

			// Insert the replacement image.
				
			if (useGoogleDocSize && imageSizes.containsKey(range.getStart())) {
				size = imageSizes.get(range.getStart());
			}
			
			TextRunPosition newRange = requests.insertImage(imageLink, range, size);

			// Re-create the link on the new text.
			requests.createLinkTag(tagName, newRange);
			
		}

		requests.sendRequest();
		
	}
	
	
	public void updateTaggedTable(String tagName, RDataframe longFormatTable, RNumericVector colWidths, RNumeric tableWidthInches) throws IOException, UnconvertableTypeException {
		
		log.info("Autotext replacing: {{"+tagName+"}} with table");
		Map<String, List<TextRunPosition>> lm = updateInlineTags();
		
		RBoundDataframe<LongFormatTable> df = longFormatTable.attachPermissive(LongFormatTable.class);
		// get rows and columns of table
		if (RFunctions.any(s -> s.isNa(), df.get("row"))) throw new UnconvertableTypeException("the row column cannot have missing values");
		if (RFunctions.any(s -> s.isNa(), df.get("col"))) throw new UnconvertableTypeException("the col column cannot have missing values");
		
		int rows = df.streamCoerce().mapToInt(lft -> lft.row().get()+lft.rowSpan().opt().orElse(1)-1).max().orElseThrow(() -> new IOException("Zero rows in table"));
		int cols = df.streamCoerce().mapToInt(lft -> lft.col().get()+lft.colSpan().opt().orElse(1)-1).max().orElseThrow(() -> new IOException("Zero columns in table"));
		
		DocumentRequestBuilder request1 = new DocumentRequestBuilder(this);
		Document document = getDoc(TABLES_ONLY);
		TextRunPosition tablePos;
		
		if (!lm.containsKey(tagName)) {
			tablePos= endPos(document);
		} else {
			List<TextRunPosition> tablePosns = lm.get(tagName);
			if (tablePosns.size() >1 ) throw new RuntimeException("Tagged tables must be unique. Delete duplicate tags and try again.");
			tablePos = tablePosns.get(0);
			request1.deleteContent(tablePos);
			// Shift new table insert position back by one due to paragraph mark before table.
			tablePos = tablePos.offset(-1);
		}
		request1.createTable(tablePos,rows,cols,colWidths,tableWidthInches);
		
		
		
		request1.sendRequest();
		// Shift table position due to additional lf.
		tablePos = tablePos.offset(1);
		
		int startIndex = tablePos.getStart();
		// get layout of now empty table 
		document = getDoc(TABLES_AND_CELLS);
		Table insertInto = ofNullable(document.getBody().getContent())
				.filter(b -> Optional.ofNullable(b.getStartIndex()).orElse(0) == startIndex)
				.flatMap(b -> ofNullable(b.getTable()))
				.findFirst().orElseThrow(() -> new RuntimeException("Could not find the tagged table."));
		
		
		DocumentRequestBuilder request2 = new DocumentRequestBuilder(this);
		List<LongFormatTable> tmp = df.streamCoerce().collect(Collectors.toList()); 
	    
		request2.writeTableContent(tmp, insertInto, tablePos, Optional.of(tagName));
		
		request2.sendRequest();
		
		
	}
	
	public void removeTags() throws IOException {
		Document document = getDoc(TEXT_AND_IMAGE_LINK_ELEMENTS);
		
		Map<String,List<TextRunPosition>> tl = DocumentHelper.findLinks(document);
		
		List<TextRunPosition> allRanges = new ArrayList<>();
		//HashMap<Integer,String> insertIndexes = new HashMap<>();

		// Determine all the ranges of text to be removed, and at which indexes the replacement text
		// should be inserted.
		tl.entrySet().stream().forEach(e -> {
			// String tagName = e.getKey();
			allRanges.addAll(e.getValue());
		});
		
		allRanges.sort(new TextRunPosition.Compare().reversed());
		
		DocumentRequestBuilder requests = new DocumentRequestBuilder(this);
		for (TextRunPosition range : allRanges) {
			requests.removeLink(range);
		}
		//Remove hidden unicode charcters.
		requests.add(new Request().setReplaceAllText(
				new ReplaceAllTextRequest()
					.setContainsText(new SubstringMatchCriteria().setText("\u2060"))
					.setReplaceText("")
				));
		requests.sendRequest();
	}
	
	public void revertTags() throws IOException {

		// Fetch the document to determine the current indexes of the named ranges.
		// Find the matching named ranges.
		Document document = getDoc(TEXT_AND_IMAGE_LINK_ELEMENTS);
		
		Map<String,List<TextRunPosition>> tl = DocumentHelper.findLinks(document);
		TreeMap<TextRunPosition,String> allRanges = new TreeMap<>(new TextRunPosition.Compare().reversed());
		
		tl.forEach((k,v) -> {
			v.forEach(t -> {
				allRanges.put(t, k);
			});
		});
		
		// Create a sequence of requests for each range.
		DocumentRequestBuilder requests = new DocumentRequestBuilder(this);
		allRanges.forEach((range, tagName) -> {
			// Delete all the content in the existing range.
			requests.deleteContent(range);
			requests.insertTextContent(range, tagName, Optional.empty());
		});

		requests.sendRequest();
	}

	public  RService getService() {
		return service;
	}

	public String getDocId() {
		return docId;
	}
	
	public String getName() {
		return name;
	}

	public int updateOrInsertInlineImage(int figureIndex, URI imageLink, Double maxWidthInInches, Double maxHeightInInches) throws IOException {
		Size size = new Size()
				.setWidth(new Dimension().setMagnitude(maxWidthInInches*72).setUnit("PT"))
				.setHeight(new Dimension().setMagnitude(maxHeightInInches*72).setUnit("PT"));
		return updateOrInsertInlineImage(figureIndex, imageLink, size);
	}
	
	public int updateOrInsertInlineImage(int figureIndex, URI imageLink, Size size) throws IOException {
		Document document = getDoc(IMAGE_POSITIONS);
		TupleList<String,TextRunPosition> imageIds = DocumentHelper.inlineImageIds(document);
		DocumentRequestBuilder request1 = new DocumentRequestBuilder(this);
		try {
			
			//String imageId = imageIds.get(figureIndex-1).getFirst();
			TextRunPosition range = imageIds.get(figureIndex-1).getSecond();
			
			request1.add(
					new Request().setDeleteContentRange(new DeleteContentRangeRequest().setRange(range.getDocsRange())));

//			if (useGoogleDocSize && imageSizes.containsKey(range.getStartIndex())) {
//				size = imageSizes.get(range.getStartIndex());
//			}
				
			//Range newRange = 
			request1.insertImage(imageLink, range, size);

			// request1.updateImageWithUri(imageId, imageLink);
			request1.sendRequest();
			return figureIndex;
			
		} catch (IndexOutOfBoundsException e) {
			
			//create a new image at end of document
			figureIndex = imageIds.size()+1;
			request1.insertImageAtEnd(imageLink, size);
			request1.sendRequest();
			return figureIndex;
		}
		
	}
	
	public int updateOrInsertTable(int tableIndex, RDataframe longFormatTable, RNumericVector colWidths, RNumeric tableWidthInches) throws IOException, UnconvertableTypeException {
		Document document = getDoc(TABLES_ONLY);
		
		RBoundDataframe<LongFormatTable> df = longFormatTable.attachPermissive(LongFormatTable.class);
		// get rows and columns of table
		if (RFunctions.any(s -> s.isNa(), df.get("row"))) throw new UnconvertableTypeException("the row column cannot have missing values");
		if (RFunctions.any(s -> s.isNa(), df.get("col"))) throw new UnconvertableTypeException("the col column cannot have missing values");
		
		int rows = df.streamCoerce().mapToInt(lft -> lft.row().get()+lft.rowSpan().opt().orElse(1)-1).max().orElseThrow(() -> new IOException("Zero rows in table"));
		int cols = df.streamCoerce().mapToInt(lft -> lft.col().get()+lft.colSpan().opt().orElse(1)-1).max().orElseThrow(() -> new IOException("Zero columns in table"));
		
		DocumentRequestBuilder request1 = new DocumentRequestBuilder(this);
		
		List<TextRunPosition> tables;
		TextRunPosition tablePos;
		tables = ofNullable(document.getBody().getContent())
				.filter(se -> se.getTable() != null)
				.map(se -> TextRunPosition.of(se.getStartIndex()-1,se.getEndIndex())) //Always \n added before table which is sort of part of table.
				.collect(Collectors.toList());
		try {
			
			tablePos = tables.get(tableIndex-1);
			// delete the existing table
			request1.deleteContent(tablePos);
			request1.createTable(tablePos,rows,cols,colWidths,tableWidthInches);
			
		} catch (IndexOutOfBoundsException e) {
			
			// if nothing found set position to end of last element in content.
			// and reset the index to the current table count. 
			tableIndex = tables.size()+1;
			request1.createTable(endPos(document),rows,cols,colWidths,tableWidthInches);
			
		}
		
		request1.sendRequest();
		
		// get layout of now empty table 
		document = getDoc(TABLES_AND_CELLS);
		List<Table> tables2 = ofNullable(document.getBody().getContent())
				.flatMap(b -> ofNullable(b.getTable()))
				.collect(Collectors.toList());
		
		tables = ofNullable(document.getBody().getContent())
				.filter(se -> se.getTable() != null)
				.map(se -> TextRunPosition.of(se.getStartIndex(),se.getEndIndex())) // the actual start of the table this time
				.collect(Collectors.toList());
		
		Table insertInto = tables2.get(tableIndex-1);
		tablePos = tables.get(tableIndex-1);
		
		DocumentRequestBuilder request2 = new DocumentRequestBuilder(this);
		List<LongFormatTable> tmp = df.streamCoerce().collect(Collectors.toList()); 
	    
		request2.writeTableContent(tmp, insertInto, tablePos, Optional.empty());
		
		request2.sendRequest();
		
		
		return tableIndex;
	}

	public void saveAsPdf(String absoluteFilePath) throws IOException {
		OutputStream outputStream = new FileOutputStream(absoluteFilePath);
		service.getDrive().files().export(docId, "application/pdf")
	    	.executeMediaAndDownloadTo(outputStream);
	}
	
	static TextRunPosition endPos(Document document) {
		Integer endPos = ofNullable(document.getBody().getContent()).mapToInt(se -> se.getEndIndex()-1).max().orElseThrow(() -> new RuntimeException("Unable to find end of document"));
		return TextRunPosition.of(endPos, endPos);
	}
	
	public void appendText(String text, Optional<String> style) throws IOException {
		Document document = getDoc(MINIMAL);
		DocumentRequestBuilder request1 = new DocumentRequestBuilder(this);
		request1.insertTextContent(endPos(document), text, style);
		request1.sendRequest();
	}
	
	public void appendText(RDataframe longFormatText) throws IOException, UnconvertableTypeException {
		Document document = getDoc(MINIMAL);
		RBoundDataframe<LongFormatText> df = longFormatText.attachPermissive(LongFormatText.class);
		DocumentRequestBuilder request1 = new DocumentRequestBuilder(this);
		TextRunPosition position = endPos(document);
		request1.insertTextContent(position, "\n", Optional.of("NORMAL_TEXT"));
		request1.writeTextContent(position.offset(1), df.streamCoerce().collect(Collectors.toList()));
		request1.sendRequest();
	}
	
	public void insertReferences(List<String> bibs) throws IOException {
		
		Map<String, List<TextRunPosition>> allDocumentTags = this.updateInlineTags();
		DocumentRequestBuilder request = new DocumentRequestBuilder(this);
		
		List<TextRunPosition> bibentries = allDocumentTags.entrySet().stream()
			.filter(e -> 
				e.getKey().matches("reference_[0-9]+")
			)
			.flatMap(e -> e.getValue().stream())
			.map(t -> t.offset(0,1))
			.collect(Collectors.toList());
		
		// If we cant find references as individual items look for a placeholder for insertion:
		if (bibentries.isEmpty()) {
			allDocumentTags.getOrDefault("references",new ArrayList<>())
				.stream().findFirst().ifPresent(ref -> bibentries.add(ref));
		}
		
		bibentries.sort(new TextRunPosition.Compare().reversed());
		request.deleteContent(bibentries);
		request.sendRequest();
		
		Document doc = getDoc(MINIMAL);
		TextRunPosition insertAt = TextRunPosition.spanning(bibentries).orElse(endPos(doc));
		
		DocumentRequestBuilder request2 = new DocumentRequestBuilder(this);
		Collections.reverse(bibs);
		for (int i=0; i < bibs.size(); i++) {
			String s = bibs.get(i);
			TextRunPosition tmp = request2.insertTextContent(insertAt, s, Optional.of("NORMAL_TEXT"));
			request2.createLinkTag("reference_"+(bibs.size()-i), tmp);
			request2.add(new Request().setUpdateParagraphStyle(
					tmp.setPosition(new UpdateParagraphStyleRequest()
							.setParagraphStyle(new ParagraphStyle()
									.setLineSpacing(100.0F)
									.setIndentFirstLine(new Dimension().setMagnitude(0D).setUnit("PT"))
									.setIndentStart(new Dimension().setMagnitude(0.5*72).setUnit("PT"))
									.setSpaceAbove(new Dimension().setMagnitude(2D).setUnit("PT"))
									.setSpaceBelow(new Dimension().setMagnitude(0D).setUnit("PT"))
									)
							.setFields("lineSpacing,indentFirstLine,indentStart,spaceAbove,spaceBelow")
				)));
		}
		request2.sendRequest();
		
	}
	
}


