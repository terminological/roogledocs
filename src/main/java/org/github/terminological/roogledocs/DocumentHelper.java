package org.github.terminological.roogledocs;

import static org.github.terminological.roogledocs.StreamHelper.ofNullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.api.services.docs.v1.model.Document;
import com.google.api.services.docs.v1.model.InlineObjectProperties;
import com.google.api.services.docs.v1.model.ParagraphElement;
import com.google.api.services.docs.v1.model.Range;
import com.google.api.services.docs.v1.model.Size;
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
	
	static Stream<ParagraphElement> elements(Document doc) {
		return ofNullable(doc.getBody().getContent())
				.flatMap(d -> ofNullable(d.getParagraph()))
				.flatMap(p -> ofNullable(p.getElements()));
	}
	
	static Stream<String> text(ParagraphElement e) {
		return ofNullable(e.getTextRun())
				.flatMap(tr -> ofNullable(tr.getContent()));
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
	
}
