package org.github.terminological.roogledocs;

import static org.github.terminological.roogledocs.StreamHelper.ofNullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.github.terminological.roogledocs.datatypes.Tuple;
import org.github.terminological.roogledocs.datatypes.TupleList;

import com.google.api.services.docs.v1.model.Color;
import com.google.api.services.docs.v1.model.Document;
import com.google.api.services.docs.v1.model.InlineObjectProperties;
import com.google.api.services.docs.v1.model.OptionalColor;
import com.google.api.services.docs.v1.model.ParagraphElement;
import com.google.api.services.docs.v1.model.Range;
import com.google.api.services.docs.v1.model.RgbColor;
import com.google.api.services.docs.v1.model.Size;
import com.google.api.services.docs.v1.model.StructuralElement;
import com.google.api.services.docs.v1.model.Table;
import com.google.api.services.docs.v1.model.TableCell;
import com.google.api.services.docs.v1.model.TableRow;
import com.google.api.services.docs.v1.model.TextStyle;

public class DocumentHelper {

	static Stream<Range> findParagraphsMatching(Document doc, String s) {
		return ofNullable(doc.getBody().getContent())
				.flatMap(d -> ofNullable(d.getParagraph()))
				.filter(p -> {
					String text = ofNullable(p.getElements())
					.flatMap(e -> ofNullable(e.getTextRun()))
					.flatMap(tr -> ofNullable(tr.getContent()))
					.collect(Collectors.joining());
					return s.equals(text);
				}).map(p -> {
					return new Range()
						.setStartIndex(
							ofNullable(p.getElements()).map(e -> e.getStartIndex()).mapToInt(i -> i).min().orElse(-1)
						)
						.setEndIndex(
							ofNullable(p.getElements()).map(e -> e.getEndIndex()).mapToInt(i -> i).max().orElse(-1)
						);
				}).filter(r ->
					r.getStartIndex() != -1 &&
					r.getEndIndex() != -1
				);
	}
	
	static List<TextRunPosition> getSortedTableCells(Table table, int tableStart) {
		List<TextRunPosition> out = new ArrayList<>();
		int i=0;
		for(TableRow row: table.getTableRows()) {
			int j=0;
			for(TableCell cell : row.getTableCells()) {
				out.add(
					TextRunPosition.of(
						null,
						null, // shapeId
						Optional.of(j), //column
						Optional.of(i), //row
						cell.getStartIndex(),
						cell.getEndIndex(),
						Optional.of(tableStart)
					)
				);
				j= j + Optional.ofNullable(cell.getTableCellStyle().getColumnSpan()).orElse(1);
			}
			i= i + 1;
		}
		Collections.sort(out, new TextRunPosition.Compare().reversed());
		return out;
	}
	
	static Stream<TextRunPosition> findTableRanges(Document doc) {
		return ofNullable(doc.getBody().getContent())
			.filter(c -> c.getTable() != null)
			.map(c -> TextRunPosition.of(
						null,
						null,
						c.getStartIndex(),
						c.getEndIndex()
			));
	}
	
	static Stream<ParagraphElement> elements(Document doc) {
		return ofNullable(doc.getBody().getContent())
				.flatMap(d -> ofNullable(d.getParagraph()))
				.flatMap(p -> ofNullable(p.getElements()));
	}
	
	static Stream<ParagraphElement> elements(TableCell cell) {
		return ofNullable(cell.getContent())
				.flatMap(d -> ofNullable(d.getParagraph()))
				.flatMap(p -> ofNullable(p.getElements()));
	}
	
	static Stream<Tuple<StructuralElement,ParagraphElement>> firstTableText(Document doc) {
		
		List<Tuple<StructuralElement,ParagraphElement>> cells = new ArrayList<>();
		for (StructuralElement c: doc.getBody().getContent()) {
			if (c.getTable() != null) {
				TableRow tr = c.getTable().getTableRows().get(0);
				TableCell tc = tr.getTableCells().get(0);
				elements(tc).forEach(p -> cells.add(Tuple.create(c, p))
				);
			}
		}
		return(cells.stream());
	}
	
	static Stream<String> text(ParagraphElement e) {
		return ofNullable(e.getTextRun())
				.flatMap(tr -> ofNullable(tr.getContent()));
	}
	
	static TupleList<String, TextRunPosition> inlineImageIds(Document doc) {
		TupleList<String,TextRunPosition> ids = new TupleList<>();
		ofNullable(doc.getBody())
		.flatMap(b -> ofNullable(b.getContent()))
		.flatMap(e -> ofNullable(e.getParagraph()))
		.flatMap(p -> ofNullable(p.getElements()))
		.forEach(el -> {
			TextRunPosition range = TextRunPosition.of(el.getStartIndex(),el.getEndIndex());
			ofNullable(el.getInlineObjectElement())
				.map(io -> io.getInlineObjectId())
				.forEach(id -> ids.and(id, range));
		});
		return(ids);
	}
	
	static Map<Integer,Size> imageSizes(Document doc) {
		Map<String,Integer> idToStart = new HashMap<>();
		ofNullable(doc.getBody())
			.flatMap(b -> ofNullable(b.getContent()))
			.flatMap(e -> ofNullable(e.getParagraph()))
			.flatMap(p -> ofNullable(p.getElements()))
			.forEach(el -> {
				int index = el.getStartIndex();
				ofNullable(el.getInlineObjectElement())
					.map(io -> io.getInlineObjectId())
					.forEach(id -> idToStart.put(id,index));
			});
		Map<Integer,Size> startToProperties = new HashMap<>();
		ofNullable(doc.getInlineObjects())
			.flatMap(d -> d.values().stream())
			.filter(io -> idToStart.containsKey(io.getObjectId()))
			.forEach(io -> {
				InlineObjectProperties iop = io.getInlineObjectProperties();
				Integer start = idToStart.getOrDefault(io.getObjectId(), null);
				if (start != null && iop != null) startToProperties.put(start, iop.getEmbeddedObject().getSize());
			});
		return startToProperties;
	}
	
	static Optional<Size> imageSize(Document doc, String tagName) {
		
		Set<Integer> starts = ofNullable(doc.getNamedRanges())
			.flatMap(d -> d.values().stream())
			.filter(nrl -> nrl.getName().equals(tagName))
			.flatMap(nrl -> ofNullable(nrl.getNamedRanges()))
			.flatMap(nr -> ofNullable(nr.getRanges()))
			.map(r -> r.getStartIndex())
			.collect(Collectors.toSet());
		
		Map<Integer,Size> startToProperties = imageSizes(doc);
		return startToProperties.entrySet().stream()
			.filter(e -> starts.contains(e.getKey()))
			.map(e -> e.getValue())
			.findFirst();
					
			
	}
	
	static Optional<TextStyle> textStyle(Document doc, Range range) {
		return elements(doc)
			.filter(e -> e.getStartIndex() <= range.getStartIndex() & e.getEndIndex()>= range.getEndIndex())
			.flatMap(e -> ofNullable(e.getTextRun()))
			.findFirst()
			.map(tr -> tr.getTextStyle());
	}
	
	
	/**
	 * @param doc a document from RDocument.getDoc(RDocument.TEXT_AND_IMAGE_LINK_ELEMENTS)
	 * @return a mapping of link name to a list of start and end pairs.
	 */
	static Map<String, List<TextRunPosition>> findLinks(Document doc) {
		
		Map<String,List<TextRunPosition>> tl = new HashMap<>();
		
		elements(doc)
			.forEach(el -> {
				boolean linked = ofNullable(el.getTextRun())
						.flatMap(tr -> ofNullable(tr.getTextStyle().getLink()))
						.anyMatch(l -> l.getUrl().startsWith(DocumentRequestBuilder.LINKBASE));
				if (linked) {
					String url = el.getTextRun().getTextStyle().getLink().getUrl();
					String name = DocumentRequestBuilder.nameOfLink(url);
					if (!tl.containsKey(name))	tl.put(name, new ArrayList<>());
					List<TextRunPosition> tl2 = tl.get(name);
					TextRunPosition match = TextRunPosition.of(el.getStartIndex(), el.getEndIndex());
					tl2.add(match);
				}
				
				boolean linkedImage = ofNullable(el.getInlineObjectElement())
						.flatMap(tr -> ofNullable(tr.getTextStyle().getLink()))
						.anyMatch(l -> l.getUrl().startsWith(DocumentRequestBuilder.LINKBASE));
				if (linkedImage) {
					String url = el.getInlineObjectElement().getTextStyle().getLink().getUrl();
					String name = DocumentRequestBuilder.nameOfLink(url);
					if (!tl.containsKey(name))	tl.put(name, new ArrayList<>());
					List<TextRunPosition> tl2 = tl.get(name);
					TextRunPosition match = TextRunPosition.of(el.getStartIndex(), el.getEndIndex());
					tl2.add(match);
				}
			});
		
		// for tables we look for the link in the first paragraph element of the first cell.
		// but we return the range of the table
		firstTableText(doc)
			.forEach(el -> {
				boolean linked = ofNullable(el.getSecond().getTextRun())
						.flatMap(tr -> ofNullable(tr.getTextStyle().getLink()))
						.anyMatch(l -> l.getUrl().startsWith(DocumentRequestBuilder.LINKBASE));
				if (linked) {
					String url = el.getSecond().getTextRun().getTextStyle().getLink().getUrl();
					String name = DocumentRequestBuilder.nameOfLink(url);
					if (!tl.containsKey(name))	tl.put(name, new ArrayList<>());
					List<TextRunPosition> tl2 = tl.get(name);
					TextRunPosition match = TextRunPosition.of(el.getFirst().getStartIndex(), el.getFirst().getEndIndex());
					tl2.add(match);
				}
			});
		
		return(tl);
	}
	
	
	static OptionalColor col(Float r, Float g, Float b) {
		return new OptionalColor().setColor(new Color().setRgbColor(new RgbColor().setRed(r).setBlue(b).setGreen(g)));
	}
}
