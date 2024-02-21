package org.github.terminological.roogledocs;

import static org.github.terminological.roogledocs.StreamHelper.ofNullable;
import static org.github.terminological.roogledocs.StreamHelper.str;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.github.terminological.roogledocs.datatypes.Tuple;
import org.github.terminological.roogledocs.datatypes.TupleList;

import com.google.api.services.slides.v1.model.AffineTransform;
import com.google.api.services.slides.v1.model.OpaqueColor;
import com.google.api.services.slides.v1.model.OptionalColor;
import com.google.api.services.slides.v1.model.Page;
import com.google.api.services.slides.v1.model.PageElement;
import com.google.api.services.slides.v1.model.Presentation;
import com.google.api.services.slides.v1.model.Range;
import com.google.api.services.slides.v1.model.RgbColor;
import com.google.api.services.slides.v1.model.Size;
import com.google.api.services.slides.v1.model.Table;
import com.google.api.services.slides.v1.model.TableCell;
import com.google.api.services.slides.v1.model.TableCellLocation;
import com.google.api.services.slides.v1.model.TableRow;
import com.google.api.services.slides.v1.model.TextElement;
import com.google.api.services.slides.v1.model.TextRun;



public class SlidesHelper {

//	static Stream<Range> findParagraphsMatching(Document doc, String s) {
//		return ofNullable(doc.getBody().getContent())
//				.flatMap(d -> ofNullable(d.getParagraph()))
//				.filter(p -> {
//					String text = ofNullable(p.getElements())
//					.flatMap(e -> ofNullable(e.getTextRun()))
//					.flatMap(tr -> ofNullable(tr.getContent()))
//					.collect(Collectors.joining());
//					return s.equals(text);
//				}).map(p -> {
//					return new Range()
//						.setStartIndex(
//							ofNullable(p.getElements()).map(e -> e.getStartIndex()).mapToInt(i -> i).min().orElse(-1)
//						)
//						.setEndIndex(
//							ofNullable(p.getElements()).map(e -> e.getEndIndex()).mapToInt(i -> i).max().orElse(-1)
//						);
//				}).filter(r ->
//					r.getStartIndex() != -1 &&
//					r.getEndIndex() != -1
//				);
//	}
	
	static Double calculateHeight(Size size, AffineTransform transform) {
		if (size == null || transform == null) return Double.NaN;
		Double heightToInches = size.getHeight().getUnit()=="PT" ? 72.0 : 12700*72.0;
		Double height = size.getHeight().getMagnitude() / heightToInches;
	    // height = sqrt((shearX*height)^2+(scaleY*height)^2)
		Double shearX = Optional.ofNullable(transform.getShearX()).orElse(0D);
		Double scaleY = Optional.ofNullable(transform.getScaleY()).orElse(1D);
	    return Math.sqrt(Math.pow(shearX*height,2.0)+Math.pow(scaleY*height,2));		
	}
	
	static Double calculateWidth(Size size, AffineTransform transform) {
		if (size == null || transform == null) return Double.NaN;
		Double widthToInches = size.getWidth().getUnit()=="PT" ? 72.0 : 12700*72.0;
		Double width = size.getWidth().getMagnitude() / widthToInches;
		Double shearY = Optional.ofNullable(transform.getShearY()).orElse(0D);
		Double scaleX = Optional.ofNullable(transform.getScaleX()).orElse(1D);
		// width = sqrt((shearY*width)^2+(scaleX*width)^2)	
		return Math.sqrt(Math.pow(shearY*width,2.0)+Math.pow(scaleX*width,2));	
	}
	
	
	
	static TupleList<TableCellLocation,TextRunPosition> getSortedTableCells(String pageId, Table table, String tableId) {
		TupleList<TableCellLocation,TextRunPosition> out = new TupleList<>();
		int i=0;
		for(TableRow row: table.getTableRows()) {
			int j=0;
			for(TableCell cell : row.getTableCells()) {
				out.and(
					new TableCellLocation().setRowIndex(i).setColumnIndex(j), 
					TextRunPosition.of(pageId, tableId,Optional.of(j) /*column*/, Optional.of(i) /*row*/,
							new Range().setStartIndex(0)
							.setEndIndex(0).setType("FIXED_RANGE")
				));
				j= j + Optional.ofNullable(cell.getColumnSpan()).orElse(1);
			}
			i= i + 1;
		}
		// Dont need to sort as each cell is a seperate object 
//		Collections.sort(out, new Comparator<Tuple<TableCellLocation,TextRunPosition>>() {
//			@Override
//			public int compare(Tuple<TableCellLocation, TextRunPosition> o1, Tuple<TableCellLocation, TextRunPosition> o2) {
//				//descending sort on start index.
//				return o2.getSecond().range.getStartIndex().compareTo(o1.getSecond().range.getStartIndex());
//			}
//			
//		});
		Collections.reverse(out);
		return out;
	}
	
//	static Stream<Range> findTableRanges(Document doc) {
//		return ofNullable(doc.getBody().getContent())
//			.filter(c -> c.getTable() != null)
//			.map(c -> new Range()
//					.setStartIndex(c.getStartIndex())
//					.setEndIndex(c.getEndIndex())
//			);
//	}
//	
	static Stream<TextElement> textElements(Presentation doc) {
		return pageElements(doc).flatMap(SlidesHelper::textElements);
	}
	
	static Stream<PageElement> pageElements(Presentation doc) {
		return ofNullable(doc.getSlides())
				.flatMap(d -> ofNullable(d.getPageElements()))
				;
	}
	
	static Stream<Tuple<TextRunPosition,TextRun>> firstTableCells(Presentation doc) {
		
		List<Tuple<TextRunPosition,TextRun>> cells = new ArrayList<>();
		
		for (Page p: doc.getSlides()) {
			for (PageElement pr: p.getPageElements()) {
				
				if (pr.getTable() != null) {
					Table t = pr.getTable();
					TableRow tr = t.getTableRows().get(0);
					TableCell tc = tr.getTableCells().get(0);
					// This is the position of the table the 0,0 is not used to position.
					// But it is used to match on later.
					TextRunPosition pos = TextRunPosition.of(p.getObjectId(), pr.getObjectId(),0,0);
					Optional<TextRun> content = str(tc)
							.map(tc1 -> tc1.getText())
							.flatMap(tc2 -> tc2.getTextElements())
							.map(te -> te.getTextRun()).get().findFirst();
					
					if(content.isPresent()) {
						cells.add(Tuple.create(
									pos,content.get()));
					}
											
					
				}
			}
		}
		return(cells.stream());
				
	}
	
	static Stream<TextElement> textElements(PageElement pageElements) {
		return ofNullable(pageElements.getShape())
				.flatMap(s -> ofNullable(s.getText()))
				.flatMap(t -> ofNullable(t.getTextElements()));
	}
	
	static Stream<TextElement> textElements(TableCell tableCell) {
		return ofNullable(tableCell.getText())
				.flatMap(s -> ofNullable(s.getTextElements()));
	}
	
	static Stream<String> text(TextElement e) {
		return ofNullable(e.getTextRun())
				.flatMap(tr -> ofNullable(tr.getContent()));
	}
	
	static Optional<String> pageIdContainingObjectId(Presentation doc, String objectId) {
		return ofNullable(doc.getSlides())
				.filter(
						p -> ofNullable(p.getPageElements())
						.anyMatch(pe -> pe.getObjectId().equals(objectId))
				)
				.flatMap(pe -> ofNullable(pe.getObjectId()))
				.findFirst();
	}
	
	static Optional<Integer> slideIndexOfPageId(Presentation doc, String pageId) {
		int i = 0;
		for (Page page: doc.getSlides()) {
			if (page.getObjectId().equals(pageId)) return Optional.of(i);
			i = i+1;
		}
		return Optional.empty();
	}
	
	static Optional<PageElement> findElementById(Presentation doc, String shapeId) {
		return pageElements(doc).filter(pe -> pe.getObjectId().equals(shapeId)).findFirst();
	}
	
	static List<PageElement> findShapeByPageIdAndType(Presentation doc, String pageId, String type) {
		return ofNullable(doc.getSlides())
				.filter(pe -> pe.getObjectId().equals(pageId))
				.flatMap(p -> ofNullable(p.getPageElements()))
				.filter(e -> ofNullable(e.getShape()).anyMatch(s -> s.getShapeType().equals(type)))
				.collect(Collectors.toList());
	}
	
	static Optional<TextRunPosition> findTextBoxByPageIdAndIndex(Presentation doc, String pageId, int index) {
		List<PageElement> tmp = findShapeByPageIdAndType(doc, pageId, "TEXT_BOX");
		if (index < tmp.size()) {
			Integer minStart = textElements(tmp.get(index))
				.mapToInt(te -> te.getStartIndex())
				.min().orElse(0);
			Integer maxEnd = textElements(tmp.get(index))
					.mapToInt(te -> te.getEndIndex())
					.max().orElse(minStart);
			
			return Optional.of(tmp.get(index)).map(pe -> 
					TextRunPosition.of(
							pageId,
							pe.getObjectId(), 
							Optional.empty(), 
							Optional.empty(), 
							new Range().setStartIndex(minStart).setEndIndex(maxEnd).setType("FIXED_RANGE"))
			);
		}
		
		return Optional.empty();
	}
	
	static Optional<Page> findPageById(Presentation doc, String pageId) {
		return ofNullable(doc.getSlides()).filter(pe -> pe.getObjectId().equals(pageId)).findFirst();
	}
	
//	
//	static TupleList<String,Range> inlineImageIds(Document doc) {
//		TupleList<String,Range> ids = new TupleList<>();
//		ofNullable(doc.getBody())
//		.flatMap(b -> ofNullable(b.getContent()))
//		.flatMap(e -> ofNullable(e.getParagraph()))
//		.flatMap(p -> ofNullable(p.getElements()))
//		.forEach(el -> {
//			Range range = new Range().setStartIndex(el.getStartIndex()).setEndIndex(el.getEndIndex());
//			ofNullable(el.getInlineObjectElement())
//				.map(io -> io.getInlineObjectId())
//				.forEach(id -> ids.and(id, range));
//		});
//		return(ids);
//	}
//	
//	static Map<Integer,Size> imageSizes(Document doc) {
//		Map<String,Integer> idToStart = new HashMap<>();
//		ofNullable(doc.getBody())
//			.flatMap(b -> ofNullable(b.getContent()))
//			.flatMap(e -> ofNullable(e.getParagraph()))
//			.flatMap(p -> ofNullable(p.getElements()))
//			.forEach(el -> {
//				int index = el.getStartIndex();
//				ofNullable(el.getInlineObjectElement())
//					.map(io -> io.getInlineObjectId())
//					.forEach(id -> idToStart.put(id,index));
//			});
//		Map<Integer,Size> startToProperties = new HashMap<>();
//		ofNullable(doc.getInlineObjects())
//			.flatMap(d -> d.values().stream())
//			.filter(io -> idToStart.containsKey(io.getObjectId()))
//			.forEach(io -> {
//				InlineObjectProperties iop = io.getInlineObjectProperties();
//				Integer start = idToStart.getOrDefault(io.getObjectId(), null);
//				if (start != null && iop != null) startToProperties.put(start, iop.getEmbeddedObject().getSize());
//			});
//		return startToProperties;
//	}
//	
//	static Optional<Size> imageSize(Document doc, String tagName) {
//		
//		Set<Integer> starts = ofNullable(doc.getNamedRanges())
//			.flatMap(d -> d.values().stream())
//			.filter(nrl -> nrl.getName().equals(tagName))
//			.flatMap(nrl -> ofNullable(nrl.getNamedRanges()))
//			.flatMap(nr -> ofNullable(nr.getRanges()))
//			.map(r -> r.getStartIndex())
//			.collect(Collectors.toSet());
//		
//		Map<Integer,Size> startToProperties = imageSizes(doc);
//		return startToProperties.entrySet().stream()
//			.filter(e -> starts.contains(e.getKey()))
//			.map(e -> e.getValue())
//			.findFirst();
//					
//			
//	}
	

//	
//	static Optional<TextStyle> textStyle(Document doc, Range range) {
//		return elements(doc)
//			.filter(e -> e.getStartIndex() <= range.getStartIndex() & e.getEndIndex()>= range.getEndIndex())
//			.flatMap(e -> ofNullable(e.getTextRun()))
//			.findFirst()
//			.map(tr -> tr.getTextStyle());
//	}
//	
//	
	/**
	 * @param doc a presentation from RPresentation.getPresentation(RPresentation.PRESENTATION_STRUCTURE)
	 * @param images include image links
	 * @param test include text links
	 * @return a mapping of link name to a shapeId; list of start and end pairs.
	 */
	static Map<String,List<TextRunPosition>> findLinks(Presentation doc, boolean images, boolean text, boolean tables) {
		
		Map<String,List<TextRunPosition>> tl = new HashMap<>();
		
		ofNullable(doc.getSlides()).forEach(slides -> {
			ofNullable(slides.getPageElements())
			.forEach(s -> {
				if (text) {
					textElements(s).forEach(el -> {
					boolean linked = ofNullable(el.getTextRun())
							.flatMap(tr -> ofNullable(tr.getStyle()))
							.flatMap(tr -> ofNullable(tr.getLink()))
							.anyMatch(l -> l.getUrl().startsWith(SlidesRequestBuilder.LINKBASE));
					if (linked) {
						
							String url = el.getTextRun().getStyle().getLink().getUrl();
							String name = SlidesRequestBuilder.nameOfLink(url);
							if (!tl.containsKey(name)) tl.put(name, new ArrayList<>());
							List<TextRunPosition> tl2 = tl.get(name);
							TextRunPosition match = TextRunPosition.of(
										slides.getObjectId(),
										s.getObjectId(),
										Optional.empty(),
										Optional.empty(),
										new Range()
											.setStartIndex(Optional.ofNullable(el.getStartIndex()).orElse(0))
											.setEndIndex(Optional.ofNullable(el.getEndIndex()).orElse(0))
											.setType("FIXED_RANGE")
									);
									//el.getTuple.create();
							tl2.add(match);
						}});
					
				}
				
				if (images) {
					boolean linkedImage = ofNullable(s.getImage())
							.flatMap(i -> ofNullable(i.getImageProperties()))
							.flatMap(ip -> ofNullable(ip.getLink()))
							.anyMatch(l -> l.getUrl().startsWith(SlidesRequestBuilder.LINKBASE));
					if (linkedImage) {
						String url = s.getImage().getImageProperties().getLink().getUrl();
						String name = SlidesRequestBuilder.nameOfLink(url);
						if (!tl.containsKey(name))	tl.put(name, new ArrayList<>());
						List<TextRunPosition> tl2 = tl.get(name);
						TextRunPosition match = TextRunPosition.of(
									slides.getObjectId(),
									s.getObjectId(),
									Optional.empty(),
									Optional.empty(),
									new Range().setType("ALL")
								);
						tl2.add(match);
					}
				}
			});
		});
		
		if (tables) {
			firstTableCells(doc)
				.forEach(t -> {
					TextRun t2 = t.getSecond();
					boolean linked = ofNullable(t2)
						.flatMap(tr -> ofNullable(tr.getStyle()))
						.flatMap(tr -> ofNullable(tr.getLink()))
						.anyMatch(l -> l.getUrl().startsWith(SlidesRequestBuilder.LINKBASE));
					
					if (linked) {
							
						String url = t2.getStyle().getLink().getUrl();
						String name = SlidesRequestBuilder.nameOfLink(url);
						if (!tl.containsKey(name)) tl.put(name, new ArrayList<>());
						List<TextRunPosition> tl2 = tl.get(name);
						TextRunPosition match = t.getFirst();
						tl2.add(match);
					};	
				});
		}	
		
		return(tl);
		
	}
	
//	
	static OptionalColor col(Float r, Float g, Float b) {
		return new OptionalColor().setOpaqueColor(new OpaqueColor().setRgbColor(new RgbColor().setRed(r).setBlue(b).setGreen(g)));
	}
	
	
	static class Decomposition {
		Double translationX;
		Double translationY;
		Double rotation;
		Double scaleX;
		Double scaleY;
		Double skewX;
		Double skewY;
		
		public AffineTransform recompose(String unit) {
			double[][] translateData = {{1,0,translationX},{0,1,translationY},{0,0,1}};
			RealMatrix t = MatrixUtils.createRealMatrix(translateData);
			double[][] rotateData = {{Math.cos(rotation),-Math.sin(rotation),0},{Math.sin(rotation),Math.cos(rotation),0},{0,0,1}};
			RealMatrix r = MatrixUtils.createRealMatrix(rotateData);
			double[][] scaleData = {{scaleX,0,0},{0,scaleY,0},{0,0,1}};
			RealMatrix sc = MatrixUtils.createRealMatrix(scaleData);
			double[][] skewData = {{1,skewX,0},{skewY,1,0},{0,0,1}};
			RealMatrix sk = MatrixUtils.createRealMatrix(skewData);
			RealMatrix tmp = t.multiply(r).multiply(sc).multiply(sk);
			return new AffineTransform()
					.setScaleX(tmp.getEntry(0, 0))
					.setScaleY(tmp.getEntry(1, 1))
					.setShearX(tmp.getEntry(0, 1))
					.setShearY(tmp.getEntry(1, 0))
					.setTranslateX(tmp.getEntry(0, 2))
					.setTranslateY(tmp.getEntry(1, 2))
					.setUnit(unit);
		}
		
		public Size rescale(Size oldSize) {
			Double width = oldSize.getWidth().getMagnitude()*this.scaleX;
			Double height = oldSize.getHeight().getMagnitude()*this.scaleY;
			oldSize.getWidth().setMagnitude(width);
			oldSize.getHeight().setMagnitude(height);
			this.scaleX = 1.0;
			this.scaleY = 1.0;
			return oldSize;
		}
		
		public void resetRotationAndSkew() {
			this.rotation = 0D;
			this.skewX = 0D;
			this.skewY = 0D;
		}
	}
	
	// From https://frederic-wang.fr/decomposition-of-2d-transform-matrices.html
	// Assumes translate - rotate - scale - skew in that order
	// Decomposition does not always result in a clean output
	static Decomposition decomposeTransform(AffineTransform t) {
		Double a = Optional.ofNullable(t.getScaleX()).orElse(1D);
		Double b = Optional.ofNullable(t.getShearY()).orElse(0D);
		Double c = Optional.ofNullable(t.getShearX()).orElse(0D);
		Double d = Optional.ofNullable(t.getScaleY()).orElse(1D);
		Double e = Optional.ofNullable(t.getTranslateX()).orElse(0D);
		Double f = Optional.ofNullable(t.getTranslateY()).orElse(0D);

		Double delta = a * d - b * c;

		Decomposition result = new Decomposition();
		result.translationX = e;
		result.translationY = f;
		
		// Apply the QR-like decomposition.
		if (a != 0 || b != 0) {
			Double r = Math.sqrt(a * a + b * b);
			result.rotation = b > 0 ? Math.acos(a / r) : -Math.acos(a / r);
			result.scaleX = r;
			result.scaleY = delta / r;
			result.skewX = Math.atan((a * c + b * d) / (r * r));
			result.skewY = 0.0;
		} else if (c != 0 || d != 0) {
			Double s = Math.sqrt(c * c + d * d);
			result.rotation =
					Math.PI / 2 - (d > 0 ? Math.acos(-c / s) : -Math.acos(c / s));
			result.scaleX = delta / s;
			result.scaleY = s;
			result.skewX = 0.0;
			result.skewY = Math.atan((a * c + b * d) / (s * s));
		} else {
		   // a = b = c = d = 0
		}

		return result;
		
	}

	public static Double calculateArea(Size size, AffineTransform transform) {
		return calculateHeight(size,transform)*calculateWidth(size,transform);
	}

	public static Optional<TextRunPosition> checkCurrent(Presentation doc, TextRunPosition bodyPos) {
		Optional<PageElement> tmp = ofNullable(doc.getSlides())
			.filter(pe -> pe.getObjectId().equals(bodyPos.getPageId().orElse("ZZZ_PAGE")))
			.flatMap(p -> ofNullable(p.getPageElements()))
			.filter(e -> e.getObjectId().equals(bodyPos.getShapeId().orElse("ZZZ_BODY")))
			.findFirst();
		
		Integer minStart = tmp.stream()
				.flatMap(t-> textElements(t))
				.mapToInt(te -> te.getStartIndex())
				.min().orElse(0);
		
		Integer maxEnd = tmp.stream()
				.flatMap(t-> textElements(t))
				.mapToInt(te -> te.getEndIndex())
				.max().orElse(minStart);
	
		return tmp.map(pe -> 
				TextRunPosition.of(
						bodyPos.pageId,
						bodyPos.shapeId,
						new Range().setStartIndex(minStart).setEndIndex(maxEnd).setType("FIXED_RANGE")));
	}

}
