package org.github.terminological.roogledocs;

import static org.github.terminological.roogledocs.DocumentHelper.elements;
import static org.github.terminological.roogledocs.DocumentHelper.imageSizes;
import static org.github.terminological.roogledocs.DocumentHelper.text;
import static org.github.terminological.roogledocs.StreamHelper.ofNullable;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.github.terminological.roogledocs.datatypes.LongFormatTable;
import org.github.terminological.roogledocs.datatypes.LongFormatText;
import org.github.terminological.roogledocs.datatypes.Tuple;
import org.github.terminological.roogledocs.datatypes.TupleList;
import org.jbibtex.BibTeXDatabase;
import org.jbibtex.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.services.docs.v1.model.DeleteContentRangeRequest;
import com.google.api.services.docs.v1.model.Dimension;
import com.google.api.services.docs.v1.model.Document;
import com.google.api.services.docs.v1.model.Range;
import com.google.api.services.docs.v1.model.Request;
import com.google.api.services.docs.v1.model.Size;
import com.google.api.services.docs.v1.model.Table;

import de.undercouch.citeproc.CSL;
import de.undercouch.citeproc.bibtex.BibTeXConverter;
import de.undercouch.citeproc.bibtex.BibTeXItemDataProvider;
import uk.co.terminological.rjava.UnconvertableTypeException;
import uk.co.terminological.rjava.types.RBoundDataframe;
import uk.co.terminological.rjava.types.RDataframe;
import uk.co.terminological.rjava.types.RNumeric;
import uk.co.terminological.rjava.types.RNumericVector;
import uk.co.terminological.rjava.utils.RFunctions;

public class RDocument {

	RService service;
	String docId;
	
	private Logger log = LoggerFactory.getLogger(RDocument.class);

	Document getDoc(String fields) throws IOException {
		return service.getDocs().documents().get(docId).setFields(fields).execute();
	}

	protected static String INLINE_FIELDS = "body(content(startIndex,endIndex,paragraph(elements(endIndex,startIndex,textRun(content))))),namedRanges";
	protected static String STRUCTURAL_ELEMENTS = "body(content(startIndex,endIndex,paragraph(elements(endIndex,startIndex,textRun(content))),table(columns,rows)))";
	protected static String TEXT_LINK_ELEMENTS = "body(content(startIndex,endIndex,paragraph(elements(endIndex,startIndex,textRun(content,textStyle(link(url)))))))";
	protected static String TEXT_AND_IMAGE_LINK_ELEMENTS = "body(content(startIndex,endIndex,paragraph(elements(endIndex,startIndex,textRun(content,textStyle(link(url))),inlineObjectElement(textStyle(link(url)))))))";
	protected static String TABLES_ONLY = "body(content(startIndex,endIndex,table(columns,rows)))";
	protected static String TABLES_AND_CELLS = "body(content(startIndex,endIndex,table(tableRows(tableCells(endIndex,startIndex,tableCellStyle(columnSpan,rowSpan))))))"; //(tableCells(content(startIndex,endIndex,tableCellStyle(columnSpan,rowSpan))))))";
	protected static String IMAGE_POSITIONS = "inlineObjects(*),body(content(startIndex,endIndex,paragraph(elements(endIndex,startIndex,inlineObjectElement(*)))))";
	// protected static String IMAGE_SIZES = "body(content(startIndex,endIndex,paragraph(elements(endIndex,startIndex,inlineObjectElement(textStyle(link(url)))))))";
	protected static String MINIMAL = "body(content(startIndex,endIndex))";
	protected static String NAMED_RANGES = "namedRanges";
	
	//private static String LINK_FIELDS = "body(content(paragraph(elements(endIndex,startIndex,textRun(content,textStyle/link/url)))))";

	Document getDoc() throws IOException {
		return service.getDocs().documents().get(docId).execute();
	}

	public RDocument(String docId, RService service) {
		this.service = service;
		this.docId = docId;
	}

//	public Map<String,TupleList<Integer,Integer>> updateInlineTags() throws IOException {
//		Document doc = getDoc(INLINE_FIELDS);
//		RequestBuilder requests = new RequestBuilder(this);
//		
//		Map<String,TupleList<Integer,Integer>> tl = new HashMap<>();
//		ofNullable(doc.getNamedRanges())
//			.flatMap(nr -> ofNullable(nr.values()))
//			.flatMap(nrl -> ofNullable(nrl.getNamedRanges()))
//			.forEach(nr -> {
//				ofNullable(nr.getRanges())
//				.forEach(r -> {
//					String name = nr.getName();
//					if (!tl.containsKey(name))	tl.put(name, TupleList.create());
//					TupleList<Integer, Integer> tl2 = tl.get(name);
//					Tuple<Integer,Integer> t2 = Tuple.create(r.getStartIndex(), r.getEndIndex());
//					if (tl2.contains(t2)) {
//						// delete this range if a duplicate present in the named ranges
//						requests.add(new Request().setDeleteNamedRange(new DeleteNamedRangeRequest().setNamedRangeId(nr.getNamedRangeId())));
//					} else {
//						tl2.add(t2);
//					}
//				});
//			});
//			
//		
//		elements(doc)
//			.forEach(e -> {
//				text(e)
//				.forEach(s -> {
//					//Pattern r = Pattern.compile("\\{\\{"+tagName+"([^\\}]*)\\}\\}");
//					Pattern r = Pattern.compile("\\{\\{([^\\}]+)\\}\\}");
//					Matcher m = r.matcher(s);
//					while(m.find()) {
//						String name =  s.substring(m.start(1), m.end(1));
//						if (!tl.containsKey(name))	tl.put(name, TupleList.create());
//						TupleList<Integer, Integer> tl2 = tl.get(name);
//						Tuple<Integer, Integer> match = Tuple.create(e.getStartIndex()+m.start(), e.getStartIndex()+m.end());
//						if (!tl2.contains(match)) {
//							// Only add if donesn't already exist
//							requests.createNamedRange(name, e.getStartIndex()+m.start(), e.getStartIndex()+m.end());
//							tl2.add(match);
//						}
//						
//					}
//				});
//			});
//
//		requests.sendRequest();
//		
//		return tl;
//
//	}

	// Get rid of same place but wrong name links.
	private static void findAndRemoveOld(Map<String,TupleList<Integer,Integer>> map, String name, Tuple<Integer,Integer> pos) {
		map.forEach((k,v) -> {
			if (!k.equals(name)) {
				v.remove(pos);
			}
		});
		map.entrySet().removeIf(e -> e.getValue().size()==0);
	}
	
	/**
	 * Fetches document content and scans it for text runs containing links and {{tags}}
	 * Creates links for test that is in the correct format. 
	 * @return 
	 * @throws IOException
	 */
	public Map<String,TupleList<Integer,Integer>> updateInlineTags() throws IOException {
		Document doc = getDoc(TEXT_AND_IMAGE_LINK_ELEMENTS);
		RequestBuilder requests = new RequestBuilder(this);
		
		Map<String,TupleList<Integer,Integer>> tl = DocumentHelper.findLinks(doc);
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
						Tuple<Integer, Integer> matchPosition = Tuple.create(e.getStartIndex()+m.start(), e.getStartIndex()+m.end());

						if (!tl2.contains(matchPosition)) {
							// The tag text is not matching existing link names at this position.
							requests.createLinkTag(name, e.getStartIndex()+m.start(), e.getStartIndex()+m.end());
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

		requests.sendRequest();
		
		return tl;

	}
	
	public void updateTaggedText(Map<String,String> tagMap) throws IOException {

		// Find the matching named ranges.
		Map<String, TupleList<Integer, Integer>> lm = updateInlineTags();
		
		// Create a reverse order map of ranges to update and the tags to update with 
		TreeMap<Range,String> allRanges = new TreeMap<>(Comparator.comparing(Range::getStartIndex).reversed());
		
		// add all the ranges in the tag map
		lm.forEach((k,v) -> {
			if (tagMap.keySet().contains(k)) {
				v.forEach(t -> {
					allRanges.put(
						new Range().setStartIndex(t.getFirst()).setEndIndex(t.getSecond()), 
						k
					);
				});
			}
		});
		
		// Create a sequence of requests for each range in reverse document order, so we can apply in one go.
		RequestBuilder requests = new RequestBuilder(this);
		for (Range range : allRanges.navigableKeySet()) {
			// Delete all the content in the existing range.
			requests.deleteContent(range);
			String tagName = allRanges.get(range);
			String newText = tagMap.get(tagName);

			// Insert the replacement text.
			Range newRange = requests.insertTextContent(range.getStartIndex(),newText, Optional.empty());

			// Re-create the named range on the new text.
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
		
//		Map<String, TupleList<Integer, Integer>> lm = updateInlineTags();
//				
//		List<Range> allRanges = new ArrayList<>();
//		//Set<Integer> insertIndexes = new HashSet<>();
//		
//		lm.getOrDefault(tagName, TupleList.create()).stream()
//			.forEach(t -> {
//				allRanges.add(new Range().setStartIndex(t.getFirst()).setEndIndex(t.getSecond()));
//				//insertIndexes.add(t.getFirst());
//			});
//		
//		// Sort the list of ranges by startIndex, in descending order.
//		allRanges.sort(Comparator.comparing(Range::getStartIndex).reversed());
//
//		// Create a sequence of requests for each range.
//		RequestBuilder requests = new RequestBuilder(this);
//		for (Range range : allRanges) {
//			// Delete all the content in the existing range.
//			requests.deleteContent(range);
//
//			//if (insertIndexes.contains(range.getStartIndex())) {
//				// Insert the replacement text.
//				Range newRange = requests.insertTextContent(range.getStartIndex(),newText, Optional.empty());
//
//				// Re-create the named range on the new text.
//				requests.createLinkTag(tagName, newRange);
//			//}
//		}
//
//		requests.sendRequest();
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
	
//	public void updateTaggedImage(String tagName, URI imageLink, boolean useGoogleDocSize, Size size) throws IOException {
//
//		// Fetch the document to determine the current indexes of the named ranges.
//		// Find the matching named ranges.
//		log.info("Autotext replacing: {{"+tagName+"}} with image: "+imageLink);
//		updateInlineTags();
//		Document document = getDoc();
//		// System.out.print(document.toPrettyString());
//		
//		List<Range> allRanges = new ArrayList<>();
//		Set<Integer> insertIndexes = new HashSet<>();
//		
//		ofNullable(document.getNamedRanges())
//			.flatMap(nr -> ofNullable(nr.get(tagName)))
//			.flatMap(nrl -> ofNullable(nrl.getNamedRanges()))
//			.forEach(namedRange -> {
//				allRanges.addAll(namedRange.getRanges());
//				insertIndexes.add(namedRange.getRanges().get(0).getStartIndex());
//			});
//		
//		// Sort the list of ranges by startIndex, in descending order.
//		allRanges.sort(Comparator.comparing(Range::getStartIndex).reversed());
//
//		Map<Integer,Size> imageSizes = imageSizes(document);
//		
//		// Create a sequence of requests for each range.
//		RequestBuilder requests = new RequestBuilder(this);
//		for (Range range : allRanges) {
//			// Delete all the content in the existing range.
//			requests.add(
//					new Request().setDeleteContentRange(new DeleteContentRangeRequest().setRange(range)));
//
//			if (insertIndexes.contains(range.getStartIndex())) {
//				// Insert the replacement image.
//				
//				if (useGoogleDocSize && imageSizes.containsKey(range.getStartIndex())) {
//					size = imageSizes.get(range.getStartIndex());
//				}
//				
//				Range newRange = requests.insertImage(imageLink, range.getStartIndex(), size);
//
//				// Re-create the named range on the new text.
//				requests.createNamedRange(tagName, newRange);
//			}
//		}
//
//		requests.sendRequest();
//		
//	}
//	

	public void updateTaggedImage(String tagName, URI imageLink, boolean useGoogleDocSize, Size size) throws IOException {

		// Fetch the document to determine the current indexes of the named ranges.
		// Find the matching named ranges.
		log.info("Autotext replacing: {{"+tagName+"}} with image: "+imageLink);
		Map<String, TupleList<Integer, Integer>> lm = updateInlineTags();
		
		List<Range> allRanges = new ArrayList<>();
		Set<Integer> insertIndexes = new HashSet<>();
		
		lm.getOrDefault(tagName, TupleList.create()).stream()
			.forEach(t -> {
				allRanges.add(new Range().setStartIndex(t.getFirst()).setEndIndex(t.getSecond()));
				insertIndexes.add(t.getFirst());
			});
		
		// Sort the list of ranges by startIndex, in descending order.
		allRanges.sort(Comparator.comparing(Range::getStartIndex).reversed());

		Document document = getDoc(IMAGE_POSITIONS);
		// System.out.print(document.toPrettyString());
		Map<Integer,Size> imageSizes = imageSizes(document);
		
		// Create a sequence of requests for each range.
		RequestBuilder requests = new RequestBuilder(this);
		for (Range range : allRanges) {
			// Delete all the content in the existing range (including link).
			requests.add(
					new Request().setDeleteContentRange(new DeleteContentRangeRequest().setRange(range)));

			if (insertIndexes.contains(range.getStartIndex())) {
				// Insert the replacement image.
				
				if (useGoogleDocSize && imageSizes.containsKey(range.getStartIndex())) {
					size = imageSizes.get(range.getStartIndex());
				}
				
				Range newRange = requests.insertImage(imageLink, range.getStartIndex(), size);

				// Re-create the link on the new text.
				requests.createLinkTag(tagName, newRange);
			}
		}

		requests.sendRequest();
		
	}
	
	public void removeTags() throws IOException {
		Document document = getDoc(TEXT_AND_IMAGE_LINK_ELEMENTS);
		
		Map<String,TupleList<Integer,Integer>> tl = DocumentHelper.findLinks(document);
		
		List<Range> allRanges = new ArrayList<>();
		//HashMap<Integer,String> insertIndexes = new HashMap<>();

		// Determine all the ranges of text to be removed, and at which indexes the replacement text
		// should be inserted.
		tl.entrySet().stream().forEach(e -> {
			// String tagName = e.getKey();
			e.getValue().stream().forEach( t -> {
				Range r = new Range()
						.setStartIndex(t.getFirst())
						.setEndIndex(t.getSecond());
				allRanges.add(r);
				// insertIndexes.put(t.getFirst(), tagName);
			});
		});
		
		
		allRanges.sort(Comparator.comparing(Range::getStartIndex).reversed());
		
		RequestBuilder requests = new RequestBuilder(this);
		for (Range range : allRanges) {
			requests.removeLink(range);
		}
		requests.sendRequest();
	}
	
	public void revertTags() throws IOException {

		// Fetch the document to determine the current indexes of the named ranges.
		// Find the matching named ranges.
		Document document = getDoc(TEXT_AND_IMAGE_LINK_ELEMENTS);
		
		Map<String,TupleList<Integer,Integer>> tl = DocumentHelper.findLinks(document);
		
		List<Range> allRanges = new ArrayList<>();
		HashMap<Integer,String> insertIndexes = new HashMap<>();

		// Determine all the ranges of text to be removed, and at which indexes the replacement text
		// should be inserted.
		tl.entrySet().stream().forEach(e -> {
			String tagName = e.getKey();
			e.getValue().stream().forEach( t -> {
				Range r = new Range()
						.setStartIndex(t.getFirst())
						.setEndIndex(t.getSecond());
				allRanges.add(r);
				insertIndexes.put(t.getFirst(), tagName);
			});
		});
		
		
//		ofNullable(document.getNamedRanges())
//			.flatMap(nr -> nr.entrySet().stream())
//			.forEach(e -> {
//				String tagName = e.getKey();
//				ofNullable(e.getValue().getNamedRanges())
//					.forEach( namedRange -> {
//						allRanges.addAll(namedRange.getRanges());
//						insertIndexes.put(namedRange.getRanges().get(0).getStartIndex(), tagName);
//					});
//			});
		
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
				requests.insertTextContent(range.getStartIndex(),tagName, Optional.empty());

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
		TupleList<String,Range> imageIds = DocumentHelper.inlineImageIds(document);
		RequestBuilder request1 = new RequestBuilder(this);
		try {
			
			//String imageId = imageIds.get(figureIndex-1).getFirst();
			Range range = imageIds.get(figureIndex-1).getSecond();
			
			request1.add(
					new Request().setDeleteContentRange(new DeleteContentRangeRequest().setRange(range)));

//			if (useGoogleDocSize && imageSizes.containsKey(range.getStartIndex())) {
//				size = imageSizes.get(range.getStartIndex());
//			}
				
			//Range newRange = 
			request1.insertImage(imageLink, range.getStartIndex(), size);

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
			// and reset the index to the current table count. 
			tableIndex = tables.size()+1;
			request1.createTable(endPos(document),rows,cols,colWidths,tableWidthInches);
			
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

	public void saveAsPdf(String absoluteFilePath) throws IOException {
		OutputStream outputStream = new FileOutputStream(absoluteFilePath);
		service.getDrive().files().export(docId, "application/pdf")
	    	.executeMediaAndDownloadTo(outputStream);
	}
	
	private static int endPos(Document document) {
		Integer endPos = ofNullable(document.getBody().getContent()).mapToInt(se -> se.getEndIndex()-1).max().orElseThrow(() -> new RuntimeException("Unable to find end of document"));
		return endPos;
	}
	
	public void appendText(String text, Optional<String> style) throws IOException {
		Document document = getDoc(MINIMAL);
		RequestBuilder request1 = new RequestBuilder(this);
		request1.insertTextContent(endPos(document), text, style);
		request1.sendRequest();
	}
	
	public void appendText(RDataframe longFormatText) throws IOException, UnconvertableTypeException {
		Document document = getDoc(MINIMAL);
		RBoundDataframe<LongFormatText> df = longFormatText.attachPermissive(LongFormatText.class);
		RequestBuilder request1 = new RequestBuilder(this);
		int position = endPos(document);
		request1.insertTextContent(position, "\n", Optional.of("NORMAL_TEXT"));
		request1.writeTextContent(position+1, df.streamCoerce().collect(Collectors.toList()));
		request1.sendRequest();
	}
	
	private Stream<String> citeIds(String tag) {
		return Stream.of(tag.replace("cite:", "").split(";")).map(s -> StringUtils.strip(s,"?"));
	}
	
	private String[] citeIdArray(String tag) {
		return citeIds(tag).collect(Collectors.toList()).toArray(new String[0]);
	}
	
	public void updateCitations(String bibTex, String citationStyle) throws IOException, ParseException {
		
		// Load the bibtex and setup the parser.
		BibTeXDatabase db = new BibTeXConverter().loadDatabase(new ByteArrayInputStream(bibTex.getBytes(StandardCharsets.UTF_8)));
		BibTeXItemDataProvider provider = new BibTeXItemDataProvider();
		provider.addDatabase(db);
		List<String> bibtexIds = Arrays.asList(provider.getIds());
				
		CSL citeproc = new CSL(provider, citationStyle);
		citeproc.setOutputFormat("text");
		
		// Get the tags in the document 
		Map<String, TupleList<Integer, Integer>> allDocumentTags = this.updateInlineTags();
		
		// populate citeKeyOrder with the first occurrence of each citation in the document and the start range.
		Map<String, Integer> citeKeyOrder = new HashMap<>();  
		allDocumentTags.entrySet().stream()
			.filter(e -> e.getKey().startsWith("cite:"))
			.forEach(e -> {
				citeIds(e.getKey())
					.forEach(s -> {
						int firstOccurs = e.getValue().stream().mapToInt(t->t.getFirst()).min().orElse(0); 
						// the first index each citation appears int the document
						if (!citeKeyOrder.containsKey(s) || citeKeyOrder.get(s) > firstOccurs) {
							// ensures the value for each citation is the smallest:
							citeKeyOrder.put(s, firstOccurs);
						}
					});
			});
		// sort tmp2 by the value and extract the citation ids. This is the list in which they appear in the 
		// document.
		List<Entry<String, Integer>> sortedKeys = new ArrayList<>(citeKeyOrder.entrySet());
        sortedKeys.sort(Entry.comparingByValue());
        
        // register the keys in appearance order.
        // once this is done the order we process the keys doesn;t matter.
        List<String> notMatched = new ArrayList<>();
        sortedKeys.stream()
        		.map(k -> k.getKey())
        		.forEach(s -> {
        			if (bibtexIds.contains(s)) {
        				citeproc.registerCitationItems(s);
        			} else {
        				notMatched.add(s);
        			};
        		});
		
        // maps tags e.g. {{cite:challen2013;danon2014} to "[1],[2]" display string replacement.
		Map<String,String> tagToCite = new HashMap<>();
		allDocumentTags.keySet().stream()
			.filter(s -> s.startsWith("cite:"))
			.forEach(s -> {
				String[] citeIds = citeIdArray(s);
				//all the citeIds for this specific cite tag should be in the bibtex if we are to proceed with the matching.
				if (Stream.of(citeIds).allMatch(c -> bibtexIds.contains(c))) {
					String citeString = citeproc.makeCitation(citeIds).stream().map(c -> c.getText()).collect(Collectors.joining(","));
					tagToCite.put(s, citeString);
				} else {
					// Not all of the ids were matched. Some might have been though and if 
					// so then they will have been registered and will appear in the bibliography.
					// We can work out which ones and highlight them replacing the text with a 
					String debugString = "{{cite:"+
							Stream.of(citeIds).map(c -> {
								if (!bibtexIds.contains(c)) {return "?"+c+"?";} else {return(c);}
							}).collect(Collectors.joining(";"))
							+"}}";
					tagToCite.put(s, debugString);
				}
			});
		
		
		// find and delete any tag of the format {{bib:[0-9*]}}
		// use position of the first as place to enter bibliography
		
				
		this.updateTaggedText(tagToCite);
		
		
		RequestBuilder request = new RequestBuilder(this);
		
		List<Range> bibentries = allDocumentTags.entrySet().stream()
			.filter(e -> e.getKey().matches("reference_[0-9]+"))
			.flatMap(e -> e.getValue().stream())
			.map(t -> new Range().setStartIndex(t.getFirst()).setEndIndex(t.getSecond()+1))
			.collect(Collectors.toList());
		
		request.deleteContent(bibentries);
		request.sendRequest();
		
		Document doc = getDoc(MINIMAL);
		Integer insertAt = bibentries.stream().mapToInt(s -> s.getStartIndex()).min().orElse(endPos(doc));
		
		List<String> bibs = Arrays.asList(citeproc.makeBibliography().getEntries());
		RequestBuilder request2 = new RequestBuilder(this);
		Collections.reverse(bibs);
		for (int i=0; i < bibs.size(); i++) {
			String s = bibs.get(i);
			Range tmp = request2.insertTextContent(insertAt, s, Optional.of("NORMAL_TEXT"));
			request2.createLinkTag("reference_"+(bibs.size()-i), tmp);
		}
		request2.sendRequest();
				
		if (notMatched.size() > 0) {
			log.info("Unmatched citation keys: ");
			log.info(notMatched.stream().collect(Collectors.joining("; ")));
			log.info("Available keys: ");
			log.info(bibtexIds.stream().collect(Collectors.joining("; ")));
		}
		
		
		
		citeproc.close();
	}
	
	
	
}
