package org.github.terminological.roogledocs;

import static org.github.terminological.roogledocs.SlidesHelper.firstTableCells;
import static org.github.terminological.roogledocs.SlidesHelper.pageElements;
import static org.github.terminological.roogledocs.SlidesHelper.text;
import static org.github.terminological.roogledocs.SlidesHelper.textElements;
import static org.github.terminological.roogledocs.StreamHelper.ls;
//import static org.github.terminological.roogledocs.SlidesHelper.elements;
//import static org.github.terminological.roogledocs.SlidesHelper.imageSizes;
//import static org.github.terminological.roogledocs.SlidesHelper.text;
import static org.github.terminological.roogledocs.StreamHelper.ofNullable;
import static org.github.terminological.roogledocs.StreamHelper.recurseOne;
import static org.github.terminological.roogledocs.StreamHelper.str;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.github.terminological.roogledocs.SlidesHelper.Decomposition;
import org.github.terminological.roogledocs.datatypes.LongFormatTable;
import org.github.terminological.roogledocs.datatypes.LongFormatText;
import org.github.terminological.roogledocs.datatypes.Tuple;
import org.github.terminological.roogledocs.datatypes.TupleList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.services.slides.v1.model.AffineTransform;
import com.google.api.services.slides.v1.model.DeleteObjectRequest;
import com.google.api.services.slides.v1.model.Dimension;
import com.google.api.services.slides.v1.model.LayoutPlaceholderIdMapping;
import com.google.api.services.slides.v1.model.LayoutProperties;
import com.google.api.services.slides.v1.model.Page;
import com.google.api.services.slides.v1.model.PageElement;
import com.google.api.services.slides.v1.model.ParagraphStyle;
import com.google.api.services.slides.v1.model.Presentation;
import com.google.api.services.slides.v1.model.ReplaceAllTextRequest;
import com.google.api.services.slides.v1.model.Request;
import com.google.api.services.slides.v1.model.Size;
import com.google.api.services.slides.v1.model.SubstringMatchCriteria;
import com.google.api.services.slides.v1.model.Table;
import com.google.api.services.slides.v1.model.UpdateParagraphStyleRequest;

import uk.co.terminological.rjava.UnconvertableTypeException;
import uk.co.terminological.rjava.types.RBoundDataframe;
import uk.co.terminological.rjava.types.RDataframe;
import uk.co.terminological.rjava.types.RNumericVector;
import uk.co.terminological.rjava.utils.RFunctions;

public class RPresentation extends RCitable {

	RService service;
	String docId;
	String name;
	
	// query and store the presentation layout
	// at construction time. Use this to determine defaultLayout.
	// Also for default sizes and transforms etc.
	Presentation layout;
	LayoutElement defaultBodyElement;
	List<LayoutElement> layList;

	private Logger log = LoggerFactory.getLogger(RPresentation.class);

	public RPresentation(String docId, String name, RService service) throws IOException {
		this.service = service;
		this.docId = docId;
		this.name = name;
		this.layout = service.getSlides().presentations().get(docId).setFields(FULL_LAYOUTS).execute();
		this.findLayoutDefaults();
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
	
	// LAYOUTS
	
	private class LayoutElement implements Cloneable {

		
		public String pageId;
		public String pageName;
		public String pageDisplayName;
		public Size pageSize;
		public Size size;
		public AffineTransform transform;
		public boolean isImage;
		public boolean isShape;
		public boolean isTextBox;
		public Double area;
		public Double ypos;
		public String pageElementId;
		public String placeholderType;
		public int pageElementIndex;
		public int pageIndex;
		public String masterId;
		
		public Object clone() throws CloneNotSupportedException {
			return super.clone();
		}
		
		
	}
	
	private Optional<LayoutElement> largest(Stream<LayoutElement> layList, Comparator<LayoutElement> comp) {
		ArrayList<LayoutElement> tmp = layList.collect(Collectors.toCollection(() -> new ArrayList<>()));
		Collections.sort(tmp, comp);
		Collections.reverse(tmp);
		return tmp.stream().findFirst();
	}
	
	private Optional<LayoutElement> smallest(Stream<LayoutElement> layList, Comparator<LayoutElement> comp) {
		ArrayList<LayoutElement> tmp = layList.collect(Collectors.toCollection(() -> new ArrayList<>()));
		Collections.sort(tmp, comp);
		return tmp.stream().findFirst();
	}
	
	private void findLayoutDefaults() throws IOException {
		
		List<String> masters = ofNullable(layout.getSlides()).map(s -> s.getSlideProperties().getMasterObjectId()).distinct().collect(Collectors.toList());
		if (masters.size() > 1) throw new IOException("This presentation uses multiple masters. It is not supported in roogledocs. Sorry.");
		String master;
		if (masters.size() == 0) {
			master = ofNullable(layout.getMasters()).map(m -> m.getObjectId()).findFirst().orElseThrow(() -> new IOException("There is no suitable master slide in this presentation."));
		} else {
			master = masters.get(0);
		}
		
		layList = new ArrayList<>();
		LayoutElement pageLay = new LayoutElement();
		pageLay.pageSize = layout.getPageSize();
		int j = 0;
		for (Page p: ls(layout.getLayouts())) {
			LayoutElement lay;
			try {
				lay = (LayoutElement) pageLay.clone();
			} catch (CloneNotSupportedException e) {
				throw new RuntimeException(e);
			}
			lay.pageId = p.getObjectId();
			LayoutProperties lp = p.getLayoutProperties();
			lay.masterId = lp.getMasterObjectId();
			lay.pageName = lp.getName();
			lay.pageDisplayName = lp.getDisplayName();
			lay.pageIndex = j;
			int i = 0;
			for (PageElement pe: ls(p.getPageElements())) {
				LayoutElement lay2;
				try {
					lay2 = (LayoutElement) lay.clone();
				} catch (CloneNotSupportedException e) {
					throw new RuntimeException(e);
				}
				lay2.pageElementId = pe.getObjectId();
				lay2.size = pe.getSize();
				lay2.transform = pe.getTransform();
				lay2.isImage = pe.getImage() != null;
				lay2.isShape = pe.getShape() != null;
				lay2.isTextBox = lay2.isShape && pe.getShape().getShapeType() != null && pe.getShape().getShapeType().equals("TEXT_BOX");
				
				lay2.placeholderType = recurseOne(pe, String.class, "*", "placeholder", "type").orElse(null);
				
				lay2.area = SlidesHelper.calculateArea(lay2.size,lay2.transform);
				lay2.ypos = lay2.transform.getTranslateY();
				
				
				lay2.pageElementIndex = i;
				if (lay2.masterId.equals(master)) layList.add(lay2);
				i = i + 1;
			}
			j=j+1;
		}
		
		largest(
				layList.stream()
					.filter(ll -> ll.isTextBox)
					.filter(ll -> ll.placeholderType == null ? false : ll.placeholderType.equals("BODY")),
				Comparator.comparing(ll -> ll.area)
		).ifPresentOrElse(
				ll -> defaultBodyElement = ll,
				() -> log.warn("Could not find a suitable default for new slides. You must specify one with `setDefaultLayout()`.")
		);
		
	}
	
	public Size defaultSize() {
		return defaultBodyElement.size;
	}
	
	public AffineTransform defaultTransform() {
		return defaultBodyElement.transform;
	}
	
	public String getLayoutIdForName(String layout) throws IOException {
		return findLayoutByName(layout).stream().map(l -> l.pageId).findFirst().get();
	}
	
	private List<LayoutElement> findLayoutByName(String layout) throws IOException {
		List<LayoutElement> tmp = layList.stream().filter(ll -> 
			ll.pageName.equalsIgnoreCase(layout) ||
			ll.pageDisplayName.equalsIgnoreCase(layout)
		).collect(Collectors.toList());
		if (tmp.isEmpty()) throw new IOException("Could not find layout for: "+layout);
		return tmp;
	}
	
	private List<LayoutPlaceholderIdMapping> mappingsForLayout(String layoutId, String newBodyId, String newTitleId) {
		
		List<LayoutPlaceholderIdMapping> out = new ArrayList<>();
		
		// Body:
		Optional<LayoutElement> body = largest(
				layList.stream()
					.filter(ll -> ll.pageId.equals(layoutId))
					.filter(ll -> ll.isTextBox),
				Comparator.comparing(ll -> ll.area)
			);
		
		body.ifPresent(bodyEl -> {
			// Add bodyId
			out.add(new LayoutPlaceholderIdMapping()
					.setLayoutPlaceholderObjectId(bodyEl.pageElementId)
					.setObjectId(newBodyId));
			// Title:
			smallest(
					layList.stream()
						.filter(ll -> ll.pageId.equals(layoutId))
						.filter(ll -> !ll.pageElementId.equals(bodyEl.pageElementId))
						.filter(ll -> ll.isTextBox),
					Comparator.comparing(ll -> ll.ypos)
			).ifPresent(ll -> out.add(
					new LayoutPlaceholderIdMapping()
					.setLayoutPlaceholderObjectId(ll.pageElementId)
					.setObjectId(newTitleId)));
						
		});
		
		return(out);
	}
	
	public String getDefaultLayoutId() {
		return defaultBodyElement.pageId;
	}
	
	public String getDefaultLayoutName() {
		return defaultBodyElement.pageName;
	}
	
	public int getDefaultLayoutBodyIndex() {
		return defaultBodyElement.pageElementIndex;
	}
	
	public List<String> getLayouts() {
		return layList.stream().map(l -> l.pageName).distinct().collect(Collectors.toList());
	}
	
	public TupleList<Double,Double> getDimensions(String layout) throws IOException {
		List<LayoutElement> tmp = findLayoutByName(layout);
		TupleList<Double,Double> out = new TupleList<>();
		tmp.forEach(ll -> 
				out.add(getWidthHeight(ll))
			);
		return out;				
	}
	
	private Tuple<Double,Double> getWidthHeight(LayoutElement el) {
		Double width = SlidesHelper.calculateWidth( el.size, el.transform );
		Double height = SlidesHelper.calculateHeight( el.size, el.transform );
		return Tuple.create(width, height);
	}
	
	public Tuple<Double,Double> getBodyDimensions() {
		return getWidthHeight(defaultBodyElement);
	}
	
	public void setDefaultLayout(String layout) throws IOException {
		List<LayoutElement> tmp = findLayoutByName(layout);
		Optional<LayoutElement> body = largest(
				tmp.stream()
				.filter(ll -> ll.isTextBox),
			Comparator.comparing(ll -> ll.area)
		);
		this.defaultBodyElement = body.orElseThrow(
				() -> new IOException("Could not find a useable default text box in layout: "+layout)
		);
	}

	// protected static String INLINE_FIELDS = "body(content(startIndex,endIndex,paragraph(elements(endIndex,startIndex,textRun(content))))),namedRanges";
	protected static String PRESENTATION_STRUCTURE = "pageSize,layouts(layoutProperties(name),pageElements(size,transform,description)),slides(objectId,pageElements(objectId,size,transform,title,description,shape(shapeType,text.textElements(startIndex,endIndex,textRun(content,style.link))),image(imageProperties(link)),table(*)))";
//	protected static String TEXT_LINK_ELEMENTS = "body(content(startIndex,endIndex,paragraph(elements(endIndex,startIndex,textRun(content,textStyle(link(url)))))))";
//	protected static String TEXT_AND_IMAGE_LINK_ELEMENTS = "body(content(startIndex,endIndex,paragraph(elements(endIndex,startIndex,textRun(content,textStyle(link(url))),inlineObjectElement(textStyle(link(url)))))))";
//	protected static String TABLES_ONLY = "body(content(startIndex,endIndex,table(columns,rows)))";
//	protected static String TABLES_AND_CELLS = "body(content(startIndex,endIndex,table(tableRows(tableCells(endIndex,startIndex,tableCellStyle(columnSpan,rowSpan))))))"; //(tableCells(content(startIndex,endIndex,tableCellStyle(columnSpan,rowSpan))))))";
	protected static String IMAGE_POSITIONS = "layouts(layoutProperties(name),pageElements(size,transform,description)),slides(objectId,pageElements(objectId,size,transform,image(imageProperties(link))))";
	protected static String LAYOUTS = "layouts(layoutProperties(name),pageElements(size,transform,description,shape(shapeType))),slides(objectId,pageElements(objectId,size,transform))";
	protected static String FULL_LAYOUTS = "presentationId,pageSize,title,masters(objectId),slides(slideProperties(masterObjectId)),layouts(*),revisionId";
	protected static String TABLE_LAYOUT = "pageSize,slides(objectId,pageElements(objectId,size,transform,table(*)))";
//	
//	protected static String MINIMAL = "body(content(startIndex,endIndex))";
//	protected static String NAMED_RANGES = "namedRanges";
	
	//private static String LINK_FIELDS = "body(content(paragraph(elements(endIndex,startIndex,textRun(content,textStyle/link/url)))))";

	Presentation getPresentation(String fields) throws IOException {
		return service.getSlides().presentations().get(docId).setFields(fields).execute();
	}
	
	Presentation getPresentation() throws IOException {
		return service.getSlides().presentations().get(docId).execute();
	}

	// Get rid of same place but wrong name links.
	private static void findAndRemoveOld(Map<String, List<TextRunPosition>>  map, String name, TextRunPosition pos) {
		map.forEach((k,v) -> {
			if (!k.equals(name)) {
				v.remove(pos);
			}
		});
		map.entrySet().removeIf(e -> e.getValue().size()==0);
	}
	
	

	public Map<String, List<TextRunPosition>> updateInlineTags() throws IOException {
		return updateInlineTags(true);
	}
	
	/**
	 * Fetches document content and scans it for text runs containing links and {{tags}}
	 * Creates links for test that is in the correct format. 
	 * @return 
	 * @throws IOException
	 */
	public Map<String, List<TextRunPosition>> updateInlineTags(boolean images) throws IOException {
		Presentation doc = getPresentation(PRESENTATION_STRUCTURE);
		SlidesRequestBuilder requests = new SlidesRequestBuilder(this);
		
		Map<String, List<TextRunPosition>> tl = SlidesHelper.findLinks(doc, images, true, true);
		
		Pattern r = Pattern.compile("\\{\\{([^\\}]+)\\}\\}");
		ofNullable(doc.getSlides()).forEach(slide -> {
			ofNullable(slide.getPageElements())
			.forEach(pe -> {
				textElements(pe).forEach(e -> {
					text(e).forEach(s -> {
						//Pattern r = Pattern.compile("\\{\\{"+tagName+"([^\\}]*)\\}\\}");
						
						Matcher m = r.matcher(s);
						while(m.find()) {
							String name =  s.substring(m.start(1), m.end(1));
							if (!tl.containsKey(name))	tl.put(name, new ArrayList<>());
							List<TextRunPosition> tl2 = tl.get(name);
							TextRunPosition matchPosition = TextRunPosition.of(
											slide.getObjectId(),
											pe.getObjectId(), 
											Optional.ofNullable(e.getStartIndex()).orElse(0)+m.start(),
											Optional.ofNullable(e.getStartIndex()).orElse(0)+m.end()
											);
							
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
			});
		});
		
		firstTableCells(doc)
			.forEach(el -> {
				String s = el.getSecond().getContent();
				Matcher m = r.matcher(s);
				while(m.find()) {
					String name =  s.substring(m.start(1), m.end(1));
					
					if (!tl.containsKey(name))	tl.put(name, new ArrayList<>());
					List<TextRunPosition> tl2 = tl.get(name);
					TextRunPosition matchPosition = el.getFirst().offsetStart(m.start(), m.end());
					TextRunPosition tableMatch = el.getFirst();
	
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


		requests.sendRequest();
		
		return tl;

	}
//	
	public void updateTaggedText(Map<String,String> tagMap) throws IOException {

		// Find the matching named ranges.
		Map<String, List<TextRunPosition>> lm = updateInlineTags(false);
		
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
		
		// Create a sequence of requests for each range in reverse document order, so we can apply in one go.
		SlidesRequestBuilder requests = new SlidesRequestBuilder(this);
		
		for (TextRunPosition range : allRanges.navigableKeySet()) {
			// Delete all the content in the existing range.
			requests.deleteContent(range);
			String tagName = allRanges.get(range);
			String newText = tagMap.get(tagName);

			// Insert the replacement text.
			TextRunPosition newRange = requests.insertTextContent(range,newText, Optional.empty());

			// Re-create the named range on the new text.
			requests.createLinkTag(tagName, newRange);
		}
		
		requests.sendRequest();
		
		// Add unmatched to slide at end
		
		if (!unmatched.isEmpty()) {
			List<LongFormatText> slideBody = new ArrayList<>();
			unmatched.stream().forEach(tag -> {
				slideBody.add(LongFormatText.of(tag+": ", null, null, null, null));
				slideBody.add(LongFormatText.of(tagMap.get(tag), null, null, null, SlidesRequestBuilder.linkUrl(tag)));
				slideBody.add(LongFormatText.of("\n", null, null, null, null));
			});
	
			TextRunPosition bodyPos = this.appendSlide(getDefaultLayoutId(), Optional.of("Unmatched tags"));
			this.setSlideBody(bodyPos, slideBody, Optional.empty());
		}
	}
//	
	public void updateTaggedText(String tagName, String newText) throws IOException {

		// Fetch the document to determine the current indexes of the named ranges.
		// Find the matching named ranges.
		log.info("Autotext replacing: {{"+tagName+"}} with "+newText);
		
		Map<String,String> toUpdate = new HashMap<>();
		toUpdate.put(tagName, newText);
		updateTaggedText(toUpdate);
		
	}

	
	


	public void updateTaggedImage(String tagName, URI imageLink) throws IOException {

		// Fetch the document to determine the current indexes of the named ranges.
		// Find the matching named ranges.
		log.info("Autotext replacing: {{"+tagName+"}} with image: "+imageLink);
		Map<String, List<TextRunPosition>> lm = updateInlineTags();
		
		List<TextRunPosition> allRanges = lm.getOrDefault(tagName, new ArrayList<>());
		
		if (allRanges.isEmpty()) {
			
			log.info("tag '"+tagName+"' not found in document. appending image as final slide.");
			TextRunPosition bodyPos = this.appendSlide(getDefaultLayoutId(), Optional.of("Figure "+tagName));
			this.setSlideBody(bodyPos, imageLink, tagName, Optional.of(1));
			
		} else {
			
			// Sort the list of ranges by startIndex, in descending order.
			allRanges.sort(new TextRunPosition.Compare().reversed());
			Presentation document = getPresentation(IMAGE_POSITIONS);
			
			this.replaceElementsWithImage(
					document,
					allRanges.stream().map(t -> t.shapeId).collect(Collectors.toList()),
					imageLink,
					Optional.of(tagName)
			);
		}
		
	}
	
	
	
	
	public TupleList<String,Tuple<Double,Double>> taggedElementDimensions() throws IOException {
		
		Map<String,List<TextRunPosition>> els = updateInlineTags(true);
		TupleList<String,Tuple<Double,Double>> dimensions = TupleList.create();
		Presentation document = getPresentation(PRESENTATION_STRUCTURE);
		
		els.forEach( (tag,positions) -> {
			positions.forEach(p -> {
				
				Optional<PageElement> pe = SlidesHelper.findElementById(document, p.shapeId);
				pe.ifPresent(pe2 -> {
					Double width = SlidesHelper.calculateWidth(pe2.getSize(), pe2.getTransform());
					Double height = SlidesHelper.calculateHeight(pe2.getSize(), pe2.getTransform());
					dimensions.add(Tuple.create(tag, Tuple.create(width, height)));
				});
				
			});
		});
		
		return dimensions;
	}
	
	
	//TODO: consider slide numbers for a tag using same method as above?
	
		
	public void removeTags() throws IOException {
		Presentation doc = getPresentation(PRESENTATION_STRUCTURE);
		SlidesRequestBuilder requests = new SlidesRequestBuilder(this);
		
		Map<String, List<TextRunPosition>> tl = SlidesHelper.findLinks(doc,false,true,false);
		tl.forEach((k,v) -> {
			v.forEach(p -> requests.removeTextLink(p));
		});
		
		Map<String, List<TextRunPosition>> tl2 = SlidesHelper.findLinks(doc,true,false,false);
		tl2.forEach((k,v) -> {
			v.forEach(p -> requests.removeImageLink(p));
		});
		
//		Map<String, List<TextRunPosition>> tl3 = SlidesHelper.findLinks(doc,false,false,true);
//		List<TextRunPosition> textRuns = new ArrayList<>();
//		tl3.forEach((k,v) -> {
//			textRuns.addAll(v);
//		});
//		textRuns.sort(new TextRunPosition.Compare().reversed());
//		textRuns.forEach(p -> requests.removeTableLink(p));
//		requests.sendRequest();
		
		requests.add(new Request().setReplaceAllText(
				new ReplaceAllTextRequest()
					.setContainsText(new SubstringMatchCriteria().setText("\u2060"))
					.setReplaceText("")
				));
		requests.sendRequest();
	}
//	
//	public void revertTags() throws IOException {
//
//		// Fetch the document to determine the current indexes of the named ranges.
//		// Find the matching named ranges.
//		Presentation doc = getPresentation(PRESENTATION_STRUCTURE);
//		SlidesRequestBuilder requests = new SlidesRequestBuilder(this);
//		
//		Map<String, List<TextRunPosition>> tl = SlidesHelper.findLinks(doc, true, true);
//		
//		List<Range> allRanges = new ArrayList<>();
//		HashMap<Integer,String> insertIndexes = new HashMap<>();
//
//		// Determine all the ranges of text to be removed, and at which indexes the replacement text
//		// should be inserted.
//		tl.entrySet().stream().forEach(e -> {
//			String tagName = e.getKey();
//			e.getValue().stream().forEach( t -> {
//				Range r = new Range()
//						.setStartIndex(t.getFirst())
//						.setEndIndex(t.getSecond());
//				allRanges.add(r);
//				insertIndexes.put(t.getFirst(), tagName);
//			});
//		});
//		
//		
////		ofNullable(document.getNamedRanges())
////			.flatMap(nr -> nr.entrySet().stream())
////			.forEach(e -> {
////				String tagName = e.getKey();
////				ofNullable(e.getValue().getNamedRanges())
////					.forEach( namedRange -> {
////						allRanges.addAll(namedRange.getRanges());
////						insertIndexes.put(namedRange.getRanges().get(0).getStartIndex(), tagName);
////					});
////			});
//		
//		// Sort the list of ranges by startIndex, in descending order.
//		allRanges.sort(Comparator.comparing(Range::getStartIndex).reversed());
//
//		// Create a sequence of requests for each range.
//		for (Range range : allRanges) {
//			// Delete all the content in the existing range.
//			requests.deleteContent(range);
//
//			if (insertIndexes.containsKey(range.getStartIndex())) {
//				String tagName = "{{"+insertIndexes.get(range.getStartIndex())+"}}"; 
//				// Insert the replacement text.
//				requests.insertTextContent(range.getStartIndex(), tagName, Optional.empty());
//
//			}
//		}
//
//		requests.sendRequest();
//	}



//	public int updateOrInsertTable(int tableIndex, RDataframe longFormatTable, RNumericVector colWidths, RNumeric tableWidthInches) throws IOException, UnconvertableTypeException {
//		
//		Presentation doc = getPresentation(TABLES_ONLY);
//		SlidesRequestBuilder request1 = new SlidesRequestBuilder(this);
//		
//		RBoundDataframe<LongFormatTable> df = longFormatTable.attachPermissive(LongFormatTable.class);
//		// get rows and columns of table
//		if (RFunctions.any(s -> s.isNa(), df.get("row"))) throw new UnconvertableTypeException("the row column cannot have missing values");
//		if (RFunctions.any(s -> s.isNa(), df.get("col"))) throw new UnconvertableTypeException("the col column cannot have missing values");
//		
//		int rows = df.streamCoerce().mapToInt(lft -> lft.row().get()+lft.rowSpan().opt().orElse(1)-1).max().orElseThrow(() -> new IOException("Zero rows in table"));
//		int cols = df.streamCoerce().mapToInt(lft -> lft.col().get()+lft.colSpan().opt().orElse(1)-1).max().orElseThrow(() -> new IOException("Zero columns in table"));
//		
//		
//		TupleList<Integer,Integer> tables;
//		Tuple<Integer,Integer> tablePos;
//		tables = ofNullable(doc.getBody().getContent())
//				.filter(se -> se.getTable() != null)
//				.map(se -> Tuple.create(se.getStartIndex()-1,se.getEndIndex())) //Always \n added before table which is sort of part of table.
//				.collect(TupleList.collector());
//		try {
//			
//			tablePos = tables.get(tableIndex-1);
//			// delete the existing table
//			request1.deleteContent(tablePos);
//			request1.createTable(tablePos.getFirst(),rows,cols,colWidths,tableWidthInches);
//			
//		} catch (IndexOutOfBoundsException e) {
//			
//			// if nothing found set position to end of last element in content.
//			// and reset the index to the current table count. 
//			tableIndex = tables.size()+1;
//			request1.createTable(endPos(document),rows,cols,colWidths,tableWidthInches);
//			
//		}
//		
//		// create a blank table size rows/cols as position or end of document
//		
//		request1.sendRequest();
//		
//		// get layout of now empty table 
//		doc = getPresentation(TABLES_AND_CELLS);
//		List<Table> tables2 = ofNullable(doc.getBody().getContent())
//				.flatMap(b -> ofNullable(b.getTable()))
//				.collect(Collectors.toList());
//		
//		tables = ofNullable(doc.getBody().getContent())
//				.filter(se -> se.getTable() != null)
//				.map(se -> Tuple.create(se.getStartIndex(),se.getEndIndex())) // the actual start of the table this time
//				.collect(TupleList.collector());
//		
//		Table insertInto = tables2.get(tableIndex-1);
//		tablePos = tables.get(tableIndex-1);
//		
//		SlidesRequestBuilder request2 = new SlidesRequestBuilder(this);
//		List<LongFormatTable> tmp = df.streamCoerce().collect(Collectors.toList()); 
//	    
//		request2.writeTableContent(tmp, insertInto, tablePos.getFirst());
//		
//		request2.sendRequest();
//		
//		
//		return tableIndex;
//	}
//
	public void saveAsPdf(String absoluteFilePath) throws IOException {
		OutputStream outputStream = new FileOutputStream(absoluteFilePath);
		service.getDrive().files().export(docId, "application/pdf")
	    	.executeMediaAndDownloadTo(outputStream);
	}

	
	
	
	public TextRunPosition appendSlide(String layoutId, Optional<String> title) throws IOException {
		
		String titleId = UUID.randomUUID().toString();
		String bodyId = UUID.randomUUID().toString();
		List<LayoutPlaceholderIdMapping> mappings = mappingsForLayout(layoutId, bodyId, titleId);
		SlidesRequestBuilder requests = new SlidesRequestBuilder(this);
		String slideId = requests.createSlideAtEnd(layoutId, mappings);
		
		// The title id will have been created if mappings has 2 items
		if (title.isPresent() && mappings.size() > 1) {
			requests.insertTextContent(TextRunPosition.of(slideId, titleId), title.get(), Optional.empty());
		};
		
		requests.sendRequest();
		
		return TextRunPosition.of(slideId, bodyId, 0,0);
	}
	


	public void setSlideTitle(TextRunPosition bodyPos, String title) throws IOException {
		Presentation doc = getPresentation(PRESENTATION_STRUCTURE);
		SlidesRequestBuilder request2 = new SlidesRequestBuilder(this);
			
		Optional<TextRunPosition> bodyBox = SlidesHelper.checkCurrent(doc, bodyPos);
		bodyBox.ifPresent(tb -> {
			request2.deleteContent(tb);
			request2.insertTextContent(tb, title, Optional.empty());
		});
		request2.sendRequest();
	}
	
	public void setSlideBody(TextRunPosition bodyPos, List<LongFormatText> body, Optional<Integer> index) throws IOException {
		
		Presentation doc = getPresentation(PRESENTATION_STRUCTURE);
		SlidesRequestBuilder request2 = new SlidesRequestBuilder(this);
		
		Optional<TextRunPosition> bodyBox = SlidesHelper.checkCurrent(doc, bodyPos);
		bodyBox.ifPresent(tb -> {
			request2.deleteContent(tb);
			request2.writeTextContent(tb, body);
		});
		
		request2.sendRequest();
		
	}
	
	public void setSlideBody(TextRunPosition bodyPos, URI image, String tag, Optional<Integer> index) throws IOException {
		
		Presentation doc = getPresentation(PRESENTATION_STRUCTURE);
		
		replaceElementsWithImage(
			doc,
			Collections.singletonList(bodyPos.getShapeId().orElseThrow()),
			image,
			Optional.ofNullable(tag)
		);
		
	}
	
	public void setSlideBody(TextRunPosition bodyPos, List<String> body, Optional<String> style, Optional<Integer> index) throws IOException {
		
		Presentation doc = getPresentation(PRESENTATION_STRUCTURE);
		SlidesRequestBuilder request2 = new SlidesRequestBuilder(this);
		List<String> body2 = new ArrayList<String>(body);
		Collections.reverse(body2);
		
		Optional<TextRunPosition> bodyBox = SlidesHelper.checkCurrent(doc, bodyPos);
		bodyBox.ifPresent(tb -> {
			request2.deleteContent(tb);
			body2.forEach(s -> 
				request2.insertTextContent(tb, s+"\n", style)
			);
		});
		
		request2.sendRequest();
		
	}

	@Override
	protected void insertReferences(List<String> bibs) throws IOException {
		
		Map<String, List<TextRunPosition>> allDocumentTags = this.updateInlineTags();
		
		//Presentation doc = getPresentation(PRESENTATION_STRUCTURE);
		SlidesRequestBuilder request = new SlidesRequestBuilder(this);
		
		List<TextRunPosition> bibentries = allDocumentTags.entrySet().stream()
			.filter(e -> e.getKey().matches("reference_[0-9]+"))
			.flatMap(e -> e.getValue().stream())
			.map(t -> t.offset(0,1))
			.collect(Collectors.toList());
		
		// If we cant find references as individual items look for a placeholder for insertion:
		if (bibentries.isEmpty()) {
			allDocumentTags.getOrDefault("references",new ArrayList<>())
				.stream().findFirst().ifPresent(ref -> bibentries.add(ref));
		}
		
		if (!bibentries.isEmpty()) { 
		
			// Existing references section.
			request.deleteContent(bibentries);
			request.sendRequest();
			
			SlidesRequestBuilder request2 = new SlidesRequestBuilder(this);
			TextRunPosition insertAt = TextRunPosition.spanning(bibentries).get();
			
			Collections.reverse(bibs);
			for (int i=0; i < bibs.size(); i++) {
				String s = bibs.get(i);
				TextRunPosition tmp = request2.insertTextContent(insertAt, s, Optional.empty());
				request2.setFontSize(tmp, 12.0);
				request2.removeLineSpacing(tmp);
				request2.add(new Request().setUpdateParagraphStyle(
					tmp.setPosition(new UpdateParagraphStyleRequest()
							.setStyle(new ParagraphStyle()
									.setIndentFirstLine(new Dimension().setMagnitude(0D).setUnit("PT"))
									.setIndentStart(new Dimension().setMagnitude(0.5*72).setUnit("PT"))
									.setSpaceAbove(new Dimension().setMagnitude(0D).setUnit("PT"))
									.setSpaceBelow(new Dimension().setMagnitude(0D).setUnit("PT"))
									)
							.setFields("indentFirstLine,indentStart,spaceAbove,spaceBelow")
				)));
				request2.createLinkTag("reference_"+(bibs.size()-i), tmp);
			}
			request2.sendRequest();
		
		} else {
		
			TextRunPosition insertAt = this.appendSlide(getDefaultLayoutId(), Optional.of("References"));
			// Presentation doc2 = getPresentation(PRESENTATION_STRUCTURE);
						
			Collections.reverse(bibs);
			for (int i=0; i < bibs.size(); i++) {
				String s = bibs.get(i);
				TextRunPosition tmp = request.insertTextContent(insertAt, s, Optional.empty());
				request.setFontSize(tmp, 8.0);
				request.removeLineSpacing(tmp);
				request.add(new Request().setUpdateParagraphStyle(
						tmp.setPosition(new UpdateParagraphStyleRequest()
								.setStyle(new ParagraphStyle()
										.setIndentFirstLine(new Dimension().setMagnitude(0D).setUnit("PT"))
										.setIndentStart(new Dimension().setMagnitude(0.25*72).setUnit("PT"))
										)
								.setFields("indentFirstLine,indentStart")
					)));
				request.createLinkTag("reference_"+(bibs.size()-i), tmp);
			}
			request.sendRequest();
		
		}
		
	}
	
	public void updateTaggedTable(String tagName, RDataframe longFormatTable, RNumericVector colWidths) throws IOException, UnconvertableTypeException {
		
		log.info("Autotext replacing: {{"+tagName+"}} with table");
		Map<String, List<TextRunPosition>> lm = updateInlineTags();
		
		RBoundDataframe<LongFormatTable> df = longFormatTable.attachPermissive(LongFormatTable.class);
		// get rows and columns of table
		if (RFunctions.any(s -> s.isNa(), df.get("row"))) throw new UnconvertableTypeException("the row column cannot have missing values");
		if (RFunctions.any(s -> s.isNa(), df.get("col"))) throw new UnconvertableTypeException("the col column cannot have missing values");
		
		int rows = df.streamCoerce().mapToInt(lft -> lft.row().get()+lft.rowSpan().opt().orElse(1)-1).max().orElseThrow(() -> new IOException("Zero rows in table"));
		int cols = df.streamCoerce().mapToInt(lft -> lft.col().get()+lft.colSpan().opt().orElse(1)-1).max().orElseThrow(() -> new IOException("Zero columns in table"));
		
		SlidesRequestBuilder request1 = new SlidesRequestBuilder(this);
		
		TextRunPosition tablePos;
		if (!lm.containsKey(tagName)) {
			
			tablePos = this.appendSlide(getDefaultLayoutId(), Optional.of("Table "+tagName));
			// Presentation doc2 = getPresentation(PRESENTATION_STRUCTURE);
			// tablePos = SlidesHelper.findTextBoxByPageIdAndIndex(doc2, pageId, 1).get();
			
		} else {
			List<TextRunPosition> tablePosns = lm.get(tagName);
			if (tablePosns.size() >1 ) throw new RuntimeException("Tagged tables must be unique. Delete duplicate tags and try again.");
			tablePos = tablePosns.get(0);
			
		}
		
		String pageId = tablePos.getPageId().get();
		String newTable = replaceElementWithTable(pageId, tablePos.getShapeId().get(), rows, cols, colWidths);
		Presentation doc3 = getPresentation(TABLE_LAYOUT);
		Table table = str(doc3).flatMap(d -> d.getSlides())
			.flatMap(s -> s.getPageElements())
			.get()
			.filter(pe -> pe.getObjectId().equals(newTable))
			.map(pe -> pe.getTable())
			.findFirst().orElseThrow(() -> new RuntimeException("Could not find new table"));
			
		List<LongFormatTable> tmp = df.streamCoerce().collect(Collectors.toList()); 
		request1.writeTableContent(tmp, table, pageId, newTable, Optional.of(tagName));
		
		request1.sendRequest();

		
	}

	private String replaceElementWithTable(String pageId, String shapeId, Integer rows, Integer cols, RNumericVector colWidths) throws IOException {
		Presentation document = getPresentation(LAYOUTS);
		SlidesRequestBuilder requests = new SlidesRequestBuilder(this);
		
		// Create a sequence of requests for each range.
		Size oldSize = pageElements(document)
			.filter(pe -> pe.getObjectId().equals(shapeId))
			.flatMap(pe -> ofNullable(pe.getSize()))
			.findFirst().orElse(defaultSize());
		
		AffineTransform oldTransform = pageElements(document)
			.filter(pe -> pe.getObjectId().equals(shapeId))
			.flatMap(pe -> ofNullable(pe.getTransform()))
			.findFirst().orElse(defaultTransform());
		
		Decomposition d = SlidesHelper.decomposeTransform(oldTransform);
		oldSize = d.rescale(oldSize);
		d.resetRotationAndSkew();
		oldTransform = d.recompose(oldTransform.getUnit());
		
		// Delete all the content in the existing range (including link).
		requests.add(
			new Request().setDeleteObject(new DeleteObjectRequest().setObjectId(shapeId)));

		String id = requests.createTable(pageId, rows, cols, oldSize, oldTransform, colWidths);

		requests.sendRequest();
		return id;
	}
	
	private void replaceElementsWithImage(Presentation document, List<String> shapeIds, URI imageLink, Optional<String> tagName) throws IOException {
		SlidesRequestBuilder requests = new SlidesRequestBuilder(this);
		
		Size defaultSize = defaultSize();
		AffineTransform defaultTransform = defaultTransform();
		
		// Create a sequence of requests for each range.
		for (String shapeId : shapeIds) {
			
			Optional<String> oldPageId = SlidesHelper.pageIdContainingObjectId(document, shapeId);
			
			Size oldSize = pageElements(document)
				.filter(pe -> pe.getObjectId().equals(shapeId))
				.flatMap(pe -> ofNullable(pe.getSize()))
				.findFirst().orElse(defaultSize);
			
			AffineTransform oldTransform = pageElements(document)
				.filter(pe -> pe.getObjectId().equals(shapeId))
				.flatMap(pe -> ofNullable(pe.getTransform()))
				.findFirst().orElse(defaultTransform);
			
			// When the aspect ratio of the provided size does not match the image aspect ratio, 
			// the image is scaled and centered with respect to the size in order to maintain the aspect ratio. 
			// The provided transform is applied after this operation.
			
			// We don;t know the image dimensions. In new page element (unrotated cases) the size is actually always a square
			// and there is scaling to make it the correct size for the page. This causes the wrong kind of scaling to be applied to 
			// images that are not square to being with.
			
			Decomposition d = SlidesHelper.decomposeTransform(oldTransform);
			oldSize = d.rescale(oldSize);
			oldTransform = d.recompose(oldTransform.getUnit());
			
			// Delete all the content in the existing range (including link).
						
			requests.add(
					new Request().setDeleteObject(new DeleteObjectRequest().setObjectId(shapeId)));

			requests.insertImage(imageLink, oldPageId.orElseThrow(() -> new RuntimeException("Cannot find the page to insert the image")), Optional.of(oldSize), Optional.of(oldTransform), tagName);

		}

		requests.sendRequest();
	}
}
