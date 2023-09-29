package org.github.terminological.roogledocs;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.github.terminological.roogledocs.datatypes.LongFormatTable;
import org.github.terminological.roogledocs.datatypes.LongFormatText;
import org.github.terminological.roogledocs.datatypes.TextFormat;
import org.github.terminological.roogledocs.datatypes.Tuple;
import org.github.terminological.roogledocs.datatypes.TupleList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.services.docs.v1.model.BatchUpdateDocumentRequest;
import com.google.api.services.docs.v1.model.Color;
import com.google.api.services.docs.v1.model.DeleteContentRangeRequest;
import com.google.api.services.docs.v1.model.Dimension;
import com.google.api.services.docs.v1.model.EndOfSegmentLocation;
import com.google.api.services.docs.v1.model.InsertInlineImageRequest;
import com.google.api.services.docs.v1.model.InsertTableRequest;
import com.google.api.services.docs.v1.model.InsertTextRequest;
import com.google.api.services.docs.v1.model.Link;
import com.google.api.services.docs.v1.model.Location;
import com.google.api.services.docs.v1.model.MergeTableCellsRequest;
import com.google.api.services.docs.v1.model.OptionalColor;
import com.google.api.services.docs.v1.model.ParagraphStyle;
import com.google.api.services.docs.v1.model.Range;
import com.google.api.services.docs.v1.model.ReplaceImageRequest;
import com.google.api.services.docs.v1.model.Request;
import com.google.api.services.docs.v1.model.RgbColor;
import com.google.api.services.docs.v1.model.Size;
import com.google.api.services.docs.v1.model.Table;
import com.google.api.services.docs.v1.model.TableCellBorder;
import com.google.api.services.docs.v1.model.TableCellLocation;
import com.google.api.services.docs.v1.model.TableCellStyle;
import com.google.api.services.docs.v1.model.TableColumnProperties;
import com.google.api.services.docs.v1.model.TableRange;
import com.google.api.services.docs.v1.model.TableRowStyle;
import com.google.api.services.docs.v1.model.TextStyle;
import com.google.api.services.docs.v1.model.UpdateParagraphStyleRequest;
import com.google.api.services.docs.v1.model.UpdateTableCellStyleRequest;
import com.google.api.services.docs.v1.model.UpdateTableColumnPropertiesRequest;
import com.google.api.services.docs.v1.model.UpdateTableRowStyleRequest;
import com.google.api.services.docs.v1.model.UpdateTextStyleRequest;
import com.google.api.services.docs.v1.model.WeightedFontFamily;
import com.google.api.services.docs.v1.model.WriteControl;

import uk.co.terminological.rjava.types.RNumeric;
import uk.co.terminological.rjava.types.RNumericVector;


public class RequestBuilder extends ArrayList<Request> {

	RDocument document;
	private Logger log = LoggerFactory.getLogger(RequestBuilder.class);
	
	public RequestBuilder(RDocument document) {
		this.document = document;
	}

	public void sendRequest() throws IOException {
		if (this.any()) {
			BatchUpdateDocumentRequest batchUpdateRequest =
				new BatchUpdateDocumentRequest()
				.setRequests(this)
				.setWriteControl(new WriteControl().setRequiredRevisionId(document.getDoc().getRevisionId()));

			document.getService().getDocs().documents().batchUpdate(document.getDocId(), batchUpdateRequest).execute();
		} else {
			log.debug("No requests made");
		}
	}
	
	protected static String LINKBASE = "https://terminological.github.io/roogledocs/tag.html?";
	
	protected static String linkUrl(String tagName) {
		try {
			return LINKBASE+URLEncoder.encode(tagName, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
	protected static String nameOfLink(String linkUrl) {
		try {
			return URLDecoder.decode(linkUrl.replace(RequestBuilder.LINKBASE, ""),"UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void createLinkTag(String tagName, int start, int end, String segmentId) {
		Range tmp = new Range()
				.setStartIndex(start)
				.setEndIndex(end);
		if (segmentId != null) {
			tmp.setSegmentId(segmentId);
		}
		this.createPlainLink(tmp, linkUrl(tagName));
	}
	
	public void createLinkTag(String tagName, int start, int end) {
		createLinkTag(tagName,start,end,null);
	}
	
	public void createLinkTag(String tagName, Range range) {
		createLinkTag(tagName,range.getStartIndex(), range.getEndIndex(), range.getSegmentId());
	}
	
//	public void createNamedRange(String tagName, int start, int end, String segmentId) {
//		Range tmp = new Range()
//				.setStartIndex(start)
//				.setEndIndex(end);
//		if (segmentId != null) {
//			tmp.setSegmentId(segmentId);
//		}
//		this.add(
//				new Request().setCreateNamedRange(
//					new CreateNamedRangeRequest()
//						.setName(tagName)
//						.setRange(tmp))
//				);
//	}
//	
//	public void createNamedRange(String tagName, int start, int end) {
//		createNamedRange(tagName,start,end,null);
//	}
//	
//	public void createNamedRange(String tagName, Range range) {
//		createNamedRange(tagName,range.getStartIndex(), range.getEndIndex(), range.getSegmentId());
//	}

	public boolean any() {
		return this.size() > 0;
	}
	
	static Comparator<Tuple<Integer,Integer>> descendingTuples = new Comparator<Tuple<Integer,Integer>>() {
		@Override
		public int compare(Tuple<Integer, Integer> o1, Tuple<Integer, Integer> o2) {
			return o2.getFirst().compareTo(o1.getFirst());
		}
	};
	
	static Comparator<Range> descendingRange = new Comparator<Range>() {
		@Override
		public int compare(Range o1, Range o2) {
			return o2.getStartIndex().compareTo(o1.getStartIndex());
		}
	};
	
	public void deleteContent(TupleList<Integer,Integer> ranges) {
		
		ranges.sort(descendingTuples);
		ranges.forEach(r -> deleteContent(r));
	}
	
	public void deleteContent(List<Range> ranges) {
		ranges.sort(descendingRange);
		ranges.forEach(r -> deleteContent(r));
	}
	
	public void deleteContent(Range range) {
		this.add(new Request().setDeleteContentRange(new DeleteContentRangeRequest().setRange(range)));
	}
	
	public void deleteContent(Tuple<Integer,Integer> range) {
		deleteContent(new Range()
			.setStartIndex(range.getFirst())
			.setEndIndex(range.getSecond())
		);
	}
	
	public void updateImageWithUri(String imageId, URI imageLink) {
		this.add(
			new Request().setReplaceImage(new ReplaceImageRequest()
				.setImageObjectId(imageId)
				.setImageReplaceMethod("CENTER_CROP")
				.setUri(imageLink.toString()))
		);
	}
	
	public void insertImageAtEnd(URI imageLink, Size size) {
		insertImage(imageLink, Optional.empty(), Optional.ofNullable(size));
	}
	
	public Range insertImage(URI imageLink, Integer start, Size size) {
		return insertImage(imageLink, Optional.of(start), Optional.ofNullable(size)).get();
	}
	
	public Optional<Range> insertImage(URI imageLink, Optional<Integer> start, Optional<Size> size) {
		
		InsertInlineImageRequest iiir = new InsertInlineImageRequest()
				.setUri(imageLink.toString());
		
		if (start.isPresent()) {
			iiir.setLocation(
					new Location()
					.setIndex(start.get()));
		} else {
			iiir.setEndOfSegmentLocation(new EndOfSegmentLocation());
		}
			
		size.ifPresent(s -> iiir.setObjectSize(s));
		
		this.add(new Request().setInsertInlineImage(iiir));
		return start.map(s -> new Range()
				.setStartIndex(s)
				.setEndIndex(s+1));
	}

	public void createTable(int position, int rows, int cols, RNumericVector colWidths, RNumeric tableWidthInches) {
		this.add(
			new Request()
	        .setInsertTable(
	            new InsertTableRequest()
	            	.setLocation(new Location().setIndex(position))
	                .setRows(rows)
	                .setColumns(cols)));
		setColumnWidths(position+1, colWidths, tableWidthInches);
	}
	
	private void setRowProperties(int position) {
		this.add(new Request().setUpdateTableRowStyle(
				new UpdateTableRowStyleRequest()
					.setTableStartLocation(new Location().setIndex(position))
					.setRowIndices(new ArrayList<>())
					.setTableRowStyle(new TableRowStyle()
							.setMinRowHeight(new Dimension().setMagnitude(0D).setUnit("PT"))
							// heights etc.
					)
					.setFields("minRowHeight")
		));
	}
	
	private void setColumnWidths(int position, RNumericVector colWidths, RNumeric tableWidthInches) {
		Double total = colWidths.stream().mapToDouble(c -> c.get()).sum();
		for (int i=0; i<colWidths.size(); i++) {
			Double colWidth = colWidths.get(i).get()/total*tableWidthInches.get();
			this.add(
					new Request()
					.setUpdateTableColumnProperties(new UpdateTableColumnPropertiesRequest()
						.setColumnIndices(Arrays.asList(i)) //Zero based?
						.setTableColumnProperties(new TableColumnProperties()
								.setWidth(new Dimension().setMagnitude(colWidth*72).setUnit("PT"))
								.setWidthType("FIXED_WIDTH")
						)
						.setTableStartLocation(new Location().setIndex(position))
						.setFields("width,widthType")
					)
			);
		}
	}
	
	private static TableCellBorder border(Double weight) {
		return new TableCellBorder().setWidth(
				new Dimension().setMagnitude(weight).setUnit("PT")
		).setDashStyle("SOLID").setColor(fromHex("#000000"));
	}

	public void writeTableContent(Collection<LongFormatTable> df, Table skeleton, int tableStart) {
		TupleList<TableCellLocation, Range> cellPos = DocumentHelper.getSortedTableCells(skeleton, tableStart);
		cellPos.stream().forEach(t -> {
			df.stream().filter(c -> 
				c.col().get() == t.getFirst().getColumnIndex()+1 && 
				c.row().get() == t.getFirst().getRowIndex()+1)
			.findFirst()
			.ifPresent(c -> {
				int insertPosition = t.getSecond().getStartIndex()+1;
				Range textPos = insertTextContent(insertPosition, c.label().opt().map(s -> s == "" ? " " : s).orElse(" "), Optional.of("NORMAL_TEXT"));
				formatText(textPos, c);
				this.add(new Request()
						.setUpdateParagraphStyle(new UpdateParagraphStyleRequest()
							.setRange(textPos)
							.setParagraphStyle(new ParagraphStyle()
									.setAlignment(c.alignment().opt().map(checkRange("START","CENTER","END")).orElse("START")) //START,CENTER,END
							).setFields("alignment")
				));
				this.add(new Request()
						.setUpdateTableCellStyle(
							new UpdateTableCellStyleRequest()
								.setTableCellStyle(new TableCellStyle()
										.setBorderBottom(border(c.bottomBorderWeight().opt().orElse(0D)))
										.setBorderTop(border(c.topBorderWeight().opt().orElse(0D)))
										.setBorderLeft(border(c.leftBorderWeight().opt().orElse(0D)))
										.setBorderRight(border(c.rightBorderWeight().opt().orElse(0D)))
										// TODO: foreground colour, border styles
										.setBackgroundColor(fromHex(c.fillColour().opt().orElse("#FFFFFF")))
										.setContentAlignment(c.valignment().opt().map(checkRange("TOP","MIDDLE","BOTTOM")).orElse("TOP")) //TOP,MIDDLE,BOTTOM
										.setPaddingBottom(new Dimension().setMagnitude(c.bottomPadding().opt().orElse(1.0)).setUnit("PT"))
										.setPaddingTop(new Dimension().setMagnitude(c.topPadding().opt().orElse(1.0)).setUnit("PT"))
										.setPaddingLeft(new Dimension().setMagnitude(c.leftPadding().opt().orElse(1.0)).setUnit("PT"))
										.setPaddingRight(new Dimension().setMagnitude(c.rightPadding().opt().orElse(1.0)).setUnit("PT"))
									)
								// .setTableStartLocation(new Location().setIndex(tableStart))
								.setTableRange(new TableRange()
										.setTableCellLocation(t.getFirst())
										// Merges have not happened yet
										.setRowSpan(1)
										.setColumnSpan(1)
								)
								.setFields("borderBottom,borderTop,borderLeft,borderRight,paddingBottom,paddingTop,paddingLeft,paddingRight,backgroundColor,contentAlignment")
						)
					);
			});
			// Cell merges
			df.stream()
				.filter(c -> c.colSpan().opt().orElse(1) > 1 || c.rowSpan().opt().orElse(1) > 1)
				.map(c -> new Request()
						.setMergeTableCells(new MergeTableCellsRequest()
								.setTableRange(new TableRange()
										.setTableCellLocation(new TableCellLocation()
												.setRowIndex(c.row().get()-1)
												.setColumnIndex(c.col().get()-1)
												.setTableStartLocation(new Location().setIndex(tableStart))
										)
										.setRowSpan(c.rowSpan().get())
										.setColumnSpan(c.colSpan().get())
								)
						)
				)
				.forEach(this::add);
			
			
		});
		// set row heights to 0 for auto:
		setRowProperties(tableStart);
	}

	public static OptionalColor fromHex(String hex) {
		
		Float red = ((float) Integer.parseInt(hex.substring(1, 3),16))/256F;
		Float green = ((float) Integer.parseInt(hex.substring(3, 5),16))/256F;
		Float blue = ((float) Integer.parseInt(hex.substring(5, 7),16))/256F;
		return new OptionalColor().setColor(new Color().setRgbColor(
			new RgbColor().setRed(red).setGreen(green).setBlue(blue)
		));
		
	}
	
	Function<String,String> checkRange(String... range) {
		return (s) -> {
			if(!Arrays.asList(range).contains(s))
				throw new RuntimeException("item must be one of: "+String.join(", ", range));
			return s;
		};
	}
	
	public Range insertTextContent(int position, String unformattedText, Optional<String> style) {
		if (unformattedText == null || unformattedText == "") return new Range().setStartIndex(position).setEndIndex(position);
		Range textRun = insertTextRun(position, unformattedText);
			
		String c = style.map(checkRange("NORMAL_TEXT","TITLE","SUBTITLE","HEADING_1","HEADING_2","HEADING_3","HEADING_4","HEADING_5","HEADING_6")).orElse("NORMAL_TEXT");
		
		if (style.isPresent()) {
			this.add(new Request().setUpdateParagraphStyle(new UpdateParagraphStyleRequest()
	            .setRange(textRun)
	            .setParagraphStyle(new ParagraphStyle()
	                    .setNamedStyleType(c))
	            .setFields("namedStyleType")
			));
		}
			
		return textRun;
	}
	
	private Range insertTextRun(int position, String unformattedText) {
		if (unformattedText == null || unformattedText == "") return new Range().setStartIndex(position).setEndIndex(position);
		this.add(
				new Request()
					.setInsertText(
						new InsertTextRequest()
						.setText(unformattedText)
						.setLocation(new Location().setIndex(position) //the plus one here is to make sure we are in the paragraph in the cell.
				))
			);
			int len = StringUtils.strip(unformattedText,"\n").length();
			int offset = unformattedText.indexOf(StringUtils.strip(unformattedText,"\n"));
			Range textRun = new Range()
					.setStartIndex(position+offset)
					.setEndIndex(position+offset+len);
			
		return textRun;
	}
	
	private void formatText(Range textRun, TextFormat c) {
		c.fontSize().opt().ifPresent(f -> {
			this.add(
					new Request()
					.setUpdateTextStyle(
						new UpdateTextStyleRequest()
						.setRange(textRun)
						.setTextStyle(new TextStyle()
								.setFontSize(new Dimension().setMagnitude(f).setUnit("PT"))
						)
						.setFields("fontSize")
					)	
			);
		});
		c.fontName().opt().ifPresent(f -> {
			this.add(
					new Request()
					.setUpdateTextStyle(
						new UpdateTextStyleRequest()
						.setRange(textRun)
						.setTextStyle(new TextStyle()
								.setWeightedFontFamily(new WeightedFontFamily().setFontFamily(f))
						)
						.setFields("weightedFontFamily")
					)	
			);
		});
		c.fontFace().opt().ifPresent(f -> {
			this.add(
					new Request()
					.setUpdateTextStyle(
						new UpdateTextStyleRequest()
						.setRange(textRun)
						.setTextStyle(new TextStyle()
								.setBold(f.contains("bold"))
								.setItalic(f.contains("italic"))
								.setUnderline(f.contains("underlined"))
						)
						.setFields("bold,italic,underline")
					)	
			);
		});
	}
	
	public void writeTextContent(int position, List<LongFormatText> df) {
		Collections.reverse(df);
		df
			.stream()
			.filter(c -> c.label().get() != null && c.label().get() != "")
			.forEach(c -> {
			
				Range textRun = insertTextRun(position, c.label().get());
				formatText(textRun, c);
				
				if (!c.link().isNa()) {
					createLink(textRun, c.link().get());
				}
			});
	}

	public void createLink(Range range, String url) {
		this.add(new Request()
                .setUpdateTextStyle(new UpdateTextStyleRequest()
                        .setRange(range)
                        .setTextStyle(new TextStyle()
                                .setLink(new Link().setUrl(url)))
                        .setFields("link")));
	}
	
	public void createPlainLink(Range range, String url) {
		// TODO: somehow somewhere here the update resets the format of other links in a paragraph
		// almost as if the range is incorrect or interpreted incorrectly.
		// alternatively createLink is being called on everything and losing its link formatting in the process?
		this.add(
				new Request().setUpdateTextStyle(
					new UpdateTextStyleRequest()
						.setTextStyle(
								new TextStyle()
									.setLink(new Link().setUrl(url))
									// decided to leave this out for the time being.
									// useful to have tagged data highlighted but necessary to be able to remove all links.
									// which is now possible in the main API.
 									// .setUnderline(Boolean.FALSE)
									// .setForegroundColor(DocumentHelper.col(1.0F,0F,0F))
						)
						.setRange(range)
						// .setFields("link,underline,foregroundColor"))
						.setFields("link"))
				);
	}
	
	public void removeLink(Range range) {
		this.add(
				new Request().setUpdateTextStyle(
					new UpdateTextStyleRequest()
						.setTextStyle(new TextStyle())
						.setRange(range)
						// .setFields("link,underline,foregroundColor"))
						.setFields("link"))
				);
	}
		
}
