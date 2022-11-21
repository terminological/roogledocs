package org.github.terminological.roogledocs;

import static org.github.terminological.roogledocs.StreamHelper.ofNullable;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
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
import com.google.api.services.docs.v1.model.Location;
import com.google.api.services.docs.v1.model.OptionalColor;
import com.google.api.services.docs.v1.model.ParagraphElement;
import com.google.api.services.docs.v1.model.Range;
import com.google.api.services.docs.v1.model.RgbColor;
import com.google.api.services.docs.v1.model.Size;
import com.google.api.services.docs.v1.model.Table;
import com.google.api.services.docs.v1.model.TableCell;
import com.google.api.services.docs.v1.model.TableCellLocation;
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
	
	static TupleList<TableCellLocation,Range> getSortedTableCells(Table table, int tableStart) {
		TupleList<TableCellLocation,Range> out = new TupleList<>();
		int i=0;
		for(TableRow row: table.getTableRows()) {
			int j=0;
			for(TableCell cell : row.getTableCells()) {
				out.and(
					new TableCellLocation().setRowIndex(i).setColumnIndex(j).setTableStartLocation(new Location().setIndex(tableStart)), 
					new Range().setStartIndex(cell.getStartIndex()).setEndIndex(cell.getEndIndex())
				);
				j= j + Optional.ofNullable(cell.getTableCellStyle().getRowSpan()).orElse(1);
			}
			i= i + 1;
		}
		Collections.sort(out, new Comparator<Tuple<TableCellLocation,Range>>() {
			@Override
			public int compare(Tuple<TableCellLocation, Range> o1, Tuple<TableCellLocation, Range> o2) {
				//descending sort on start index.
				return o2.getSecond().getStartIndex().compareTo(o1.getSecond().getStartIndex());
			}
			
		});
		return out;
	}
	
	static Stream<Range> findTableRanges(Document doc) {
		return ofNullable(doc.getBody().getContent())
			.filter(c -> c.getTable() != null)
			.map(c -> new Range()
					.setStartIndex(c.getStartIndex())
					.setEndIndex(c.getEndIndex())
			);
	}
	
	static Stream<ParagraphElement> elements(Document doc) {
		return ofNullable(doc.getBody().getContent())
				.flatMap(d -> ofNullable(d.getParagraph()))
				.flatMap(p -> ofNullable(p.getElements()));
	}
	
	static Stream<String> text(ParagraphElement e) {
		return ofNullable(e.getTextRun())
				.flatMap(tr -> ofNullable(tr.getContent()));
	}
	
	static TupleList<String,Range> inlineImageIds(Document doc) {
		TupleList<String,Range> ids = new TupleList<>();
		ofNullable(doc.getBody())
		.flatMap(b -> ofNullable(b.getContent()))
		.flatMap(e -> ofNullable(e.getParagraph()))
		.flatMap(p -> ofNullable(p.getElements()))
		.forEach(el -> {
			Range range = new Range().setStartIndex(el.getStartIndex()).setEndIndex(el.getEndIndex());
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
	
	static Map<String,TupleList<Integer,Integer>> findLinks(Document doc) {
		
		Map<String,TupleList<Integer,Integer>> tl = new HashMap<>();
		
		// RDocument.TEXT_LINK_ELEMENTS
		
		elements(doc)
			.forEach(el -> {
				boolean linked = ofNullable(el.getTextRun())
						.flatMap(tr -> ofNullable(tr.getTextStyle().getLink()))
						.anyMatch(l -> l.getUrl().startsWith(RequestBuilder.LINKBASE));
				if (linked) {
					String url = el.getTextRun().getTextStyle().getLink().getUrl();
					String name = RequestBuilder.nameOfLink(url);
					if (!tl.containsKey(name))	tl.put(name, TupleList.create());
					TupleList<Integer, Integer> tl2 = tl.get(name);
					Tuple<Integer, Integer> match = Tuple.create(el.getStartIndex(), el.getEndIndex());
					tl2.add(match);
				}
				
				boolean linkedImage = ofNullable(el.getInlineObjectElement())
						.flatMap(tr -> ofNullable(tr.getTextStyle().getLink()))
						.anyMatch(l -> l.getUrl().startsWith(RequestBuilder.LINKBASE));
				if (linkedImage) {
					String url = el.getInlineObjectElement().getTextStyle().getLink().getUrl();
					String name = RequestBuilder.nameOfLink(url);
					if (!tl.containsKey(name))	tl.put(name, TupleList.create());
					TupleList<Integer, Integer> tl2 = tl.get(name);
					Tuple<Integer, Integer> match = Tuple.create(el.getStartIndex(), el.getEndIndex());
					tl2.add(match);
				}
			});
		
		return(tl);
	}
	
	
	static OptionalColor col(Float r, Float g, Float b) {
		return new OptionalColor().setColor(new Color().setRgbColor(new RgbColor().setRed(r).setBlue(b).setGreen(g)));
	}
}
