package org.github.terminological.roogledocs;

import static org.github.terminological.roogledocs.DocumentHelper.elements;
import static org.github.terminological.roogledocs.DocumentHelper.imageSizes;
import static org.github.terminological.roogledocs.DocumentHelper.text;
import static org.github.terminological.roogledocs.StreamHelper.ofNullable;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.github.terminological.roogledocs.datatypes.LongFormatTable;
import org.github.terminological.roogledocs.datatypes.Tuple;
import org.github.terminological.roogledocs.datatypes.TupleList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.services.docs.v1.model.DeleteContentRangeRequest;
import com.google.api.services.docs.v1.model.DeleteNamedRangeRequest;
import com.google.api.services.docs.v1.model.Dimension;
import com.google.api.services.docs.v1.model.Document;
import com.google.api.services.docs.v1.model.Range;
import com.google.api.services.docs.v1.model.Request;
import com.google.api.services.docs.v1.model.Size;
import com.google.api.services.docs.v1.model.Table;

import uk.co.terminological.rjava.UnconvertableTypeException;
import uk.co.terminological.rjava.types.RBoundDataframe;
import uk.co.terminological.rjava.types.RDataframe;
import uk.co.terminological.rjava.types.RNumeric;
import uk.co.terminological.rjava.types.RNumericVector;

public class RDocument {

	RService service;
	String docId;
	
	private Logger log = LoggerFactory.getLogger(RDocument.class);

	Document getDoc(String fields) throws IOException {
		return service.getDocs().documents().get(docId).setFields(fields).execute();
	}

	protected static String INLINE_FIELDS = "body(content(startIndex,endIndex,paragraph(elements(endIndex,startIndex,textRun(content))))),namedRanges";
	protected static String STRUCTURAL_ELEMENTS = "body(content(startIndex,endIndex,paragraph(elements(endIndex,startIndex,textRun(content))),table(columns,rows)))";
	protected static String TABLES_ONLY = "body(content(startIndex,endIndex,table(columns,rows)))";
	protected static String TABLES_AND_CELLS = "body(content(startIndex,endIndex,table(tableRows(tableCells(endIndex,startIndex,tableCellStyle(columnSpan,rowSpan))))))"; //(tableCells(content(startIndex,endIndex,tableCellStyle(columnSpan,rowSpan))))))";
	protected static String IMAGE_POSITIONS = "body(content(startIndex,endIndex,paragraph(elements(endIndex,startIndex,inlineObjectElement))))";
	
	//private static String LINK_FIELDS = "body(content(paragraph(elements(endIndex,startIndex,textRun(content,textStyle/link/url)))))";

	Document getDoc() throws IOException {
		return service.getDocs().documents().get(docId).execute();
	}

	public RDocument(String docId, RService service) {
		this.service = service;
		this.docId = docId;
	}

	public Map<String,TupleList<Integer,Integer>> updateInlineTags() throws IOException {
		Document doc = getDoc(INLINE_FIELDS);
		RequestBuilder requests = new RequestBuilder(this);
		
		Map<String,TupleList<Integer,Integer>> tl = new HashMap<>();
		ofNullable(doc.getNamedRanges())
			.flatMap(nr -> ofNullable(nr.values()))
			.flatMap(nrl -> ofNullable(nrl.getNamedRanges()))
			.forEach(nr -> {
				ofNullable(nr.getRanges())
				.forEach(r -> {
					String name = nr.getName();
					if (!tl.containsKey(name))	tl.put(name, TupleList.create());
					TupleList<Integer, Integer> tl2 = tl.get(name);
					Tuple<Integer,Integer> t2 = Tuple.create(r.getStartIndex(), r.getEndIndex());
					if (tl2.contains(t2)) {
						// delete this range if a duplicate present in the named ranges
						requests.add(new Request().setDeleteNamedRange(new DeleteNamedRangeRequest().setNamedRangeId(nr.getNamedRangeId())));
					} else {
						tl2.add(t2);
					}
				});
			});
			
		
		elements(doc)
			.forEach(e -> {
				text(e)
				.forEach(s -> {
					//Pattern r = Pattern.compile("\\{\\{"+tagName+"([^\\}]*)\\}\\}");
					Pattern r = Pattern.compile("\\{\\{([^\\}]+)\\}\\}");
					Matcher m = r.matcher(s);
					while(m.find()) {
						String name =  s.substring(m.start(1), m.end(1));
						if (!tl.containsKey(name))	tl.put(name, TupleList.create());
						TupleList<Integer, Integer> tl2 = tl.get(name);
						Tuple<Integer, Integer> match = Tuple.create(e.getStartIndex()+m.start(), e.getStartIndex()+m.end());
						if (!tl2.contains(match)) {
							// Only add if donesn't already exist
							requests.createNamedRange(name, e.getStartIndex()+m.start(), e.getStartIndex()+m.end());
							tl2.add(match);
						}
						
					}
				});
			});

		requests.sendRequest();
		
		return tl;

	}

	public void updateTaggedText(String tagName, String newText) throws IOException {

		// Fetch the document to determine the current indexes of the named ranges.
		// Find the matching named ranges.
		log.info("Autotext replacing: {{"+tagName+"}} with "+newText);
		updateInlineTags();
		Document document = getDoc();
		
		List<Range> allRanges = new ArrayList<>();
		Set<Integer> insertIndexes = new HashSet<>();
		
		ofNullable(document.getNamedRanges())
			.flatMap(nr -> ofNullable(nr.get(tagName)))
			.flatMap(nrl -> ofNullable(nrl.getNamedRanges()))
			.forEach(namedRange -> {
				allRanges.addAll(namedRange.getRanges());
				insertIndexes.add(namedRange.getRanges().get(0).getStartIndex());
			});

		// Sort the list of ranges by startIndex, in descending order.
		allRanges.sort(Comparator.comparing(Range::getStartIndex).reversed());

		// Create a sequence of requests for each range.
		RequestBuilder requests = new RequestBuilder(this);
		for (Range range : allRanges) {
			// Delete all the content in the existing range.
			requests.deleteContent(range);

			if (insertIndexes.contains(range.getStartIndex())) {
				// Insert the replacement text.
				Range newRange = requests.insertText(newText, range.getStartIndex());

//				Optional<TextStyle> style = textStyle(document,range);				
//				if (style.isPresent()) {
//					requests.add(new Request().setUpdateTextStyle(new UpdateTextStyleRequest().setRange(newRange).setTextStyle(style.get()).setFields("*")));
//				}

				// Re-create the named range on the new text.
				requests.createNamedRange(tagName, newRange);
			}
		}

		requests.sendRequest();
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
		updateInlineTags();
		Document document = getDoc();
		// System.out.print(document.toPrettyString());
		
		List<Range> allRanges = new ArrayList<>();
		Set<Integer> insertIndexes = new HashSet<>();
		
		ofNullable(document.getNamedRanges())
			.flatMap(nr -> ofNullable(nr.get(tagName)))
			.flatMap(nrl -> ofNullable(nrl.getNamedRanges()))
			.forEach(namedRange -> {
				allRanges.addAll(namedRange.getRanges());
				insertIndexes.add(namedRange.getRanges().get(0).getStartIndex());
			});
		
		// Sort the list of ranges by startIndex, in descending order.
		allRanges.sort(Comparator.comparing(Range::getStartIndex).reversed());

		Map<Integer,Size> imageSizes = imageSizes(document);
		
		// Create a sequence of requests for each range.
		RequestBuilder requests = new RequestBuilder(this);
		for (Range range : allRanges) {
			// Delete all the content in the existing range.
			requests.add(
					new Request().setDeleteContentRange(new DeleteContentRangeRequest().setRange(range)));

			if (insertIndexes.contains(range.getStartIndex())) {
				// Insert the replacement image.
				
				if (useGoogleDocSize && imageSizes.containsKey(range.getStartIndex())) {
					size = imageSizes.get(range.getStartIndex());
				}
				
				Range newRange = requests.insertImage(imageLink, range.getStartIndex(), size);

				// Re-create the named range on the new text.
				requests.createNamedRange(tagName, newRange);
			}
		}

		requests.sendRequest();
		
	}
	
	public void revertTags() throws IOException {

		// Fetch the document to determine the current indexes of the named ranges.
		// Find the matching named ranges.
		Document document = getDoc();
		
		List<Range> allRanges = new ArrayList<>();
		HashMap<Integer,String> insertIndexes = new HashMap<>();

		// Determine all the ranges of text to be removed, and at which indexes the replacement text
		// should be inserted.
		ofNullable(document.getNamedRanges())
			.flatMap(nr -> nr.entrySet().stream())
			.forEach(e -> {
				String tagName = e.getKey();
				ofNullable(e.getValue().getNamedRanges())
					.forEach( namedRange -> {
						allRanges.addAll(namedRange.getRanges());
						insertIndexes.put(namedRange.getRanges().get(0).getStartIndex(), tagName);
					});
			});
		
		// Sort the list of ranges by startIndex, in descending order.
		allRanges.sort(Comparator.comparing(Range::getStartIndex).reversed());

		// Create a sequence of requests for each range.
		RequestBuilder requests = new RequestBuilder(this);
		for (Range range : allRanges) {
			// Delete all the content in the existing range.
			requests.deleteContent(range);

			if (insertIndexes.containsKey(range.getStartIndex())) {
				String tagName = "{{"+insertIndexes.get(range.getStartIndex())+"}}"; 
				// Insert the replacement text.
				requests.insertText(tagName, range.getStartIndex());

			}
		}

		requests.sendRequest();
	}

	public  RService getService() {
		return service;
	}

	public String getDocId() {
		return docId;
	}

	public int updateOrInsertInlineImage(int figureIndex, URI imageLink, Double maxWidthInInches, Double maxHeightInInches) throws IOException {
		Size size = new Size()
				.setWidth(new Dimension().setMagnitude(maxWidthInInches*72).setUnit("PT"))
				.setHeight(new Dimension().setMagnitude(maxHeightInInches*72).setUnit("PT"));
		return updateOrInsertInlineImage(figureIndex, imageLink, size);
	}
	
	public int updateOrInsertInlineImage(int figureIndex, URI imageLink, Size size) throws IOException {
		Document document = getDoc(IMAGE_POSITIONS);
		List<String> imageIds = DocumentHelper.inlineImageIds(document);
		RequestBuilder request1 = new RequestBuilder(this);
		try {
			
			String imageId = imageIds.get(figureIndex-1);
			request1.updateImageWithUri(imageId, imageLink);
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
		
		RBoundDataframe<LongFormatTable> df = longFormatTable.attach(LongFormatTable.class);
		// get rows and columns of table
		int rows = df.streamCoerce().mapToInt(lft -> lft.row().get()+lft.rowSpan().get()-1).max().orElseThrow(() -> new IOException("Zero rows in table"));
		int cols = df.streamCoerce().mapToInt(lft -> lft.col().get()+lft.colSpan().get()-1).max().orElseThrow(() -> new IOException("Zero columns in table"));
		
		RequestBuilder request1 = new RequestBuilder(this);
		
		TupleList<Integer,Integer> tables;
		Tuple<Integer,Integer> tablePos;
		tables = ofNullable(document.getBody().getContent())
				.filter(se -> se.getTable() != null)
				.map(se -> Tuple.create(se.getStartIndex()-1,se.getEndIndex())) //Always \n added before table which is sort of part of table.
				.collect(TupleList.collector());
		try {
			
			tablePos = tables.get(tableIndex-1);
			// delete the existing table
			request1.deleteContent(tablePos);
			request1.createTable(tablePos.getFirst(),rows,cols,colWidths,tableWidthInches);
			
		} catch (IndexOutOfBoundsException e) {
			
			// if nothing found set position to end of last element in content.
			Integer endPos = ofNullable(document.getBody().getContent()).mapToInt(se -> se.getEndIndex()).max().orElseThrow(() -> new RuntimeException("Unable to find end fo document"));
			tablePos = Tuple.create(endPos, endPos);
			// and reset the index to the current table count. 
			tableIndex = tables.size()+1;
			request1.createTableAtEnd(tablePos.getFirst(),rows,cols,colWidths,tableWidthInches);
			
		}
		
		// create a blank table size rows/cols as position or end of document
		
		request1.sendRequest();
		
		// get layout of now empty table 
		document = getDoc(TABLES_AND_CELLS);
		List<Table> tables2 = ofNullable(document.getBody().getContent())
				.flatMap(b -> ofNullable(b.getTable()))
				.collect(Collectors.toList());
		
		tables = ofNullable(document.getBody().getContent())
				.filter(se -> se.getTable() != null)
				.map(se -> Tuple.create(se.getStartIndex(),se.getEndIndex())) // the actual start of the table this time
				.collect(TupleList.collector());
		
		Table insertInto = tables2.get(tableIndex-1);
		tablePos = tables.get(tableIndex-1);
		
		RequestBuilder request2 = new RequestBuilder(this);
		List<LongFormatTable> tmp = df.streamCoerce().collect(Collectors.toList()); 
	    
		request2.writeTableContent(tmp, insertInto, tablePos.getFirst());
		
		request2.sendRequest();
		
		
		return tableIndex;
	}
	
}
