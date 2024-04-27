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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.github.terminological.roogledocs.datatypes.LongFormatTable;
import org.github.terminological.roogledocs.datatypes.LongFormatText;
import org.github.terminological.roogledocs.datatypes.TextFormat;
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


public class DocumentRequestBuilder extends ArrayList<Request> {

	RDocument document;
	private Logger log = LoggerFactory.getLogger(DocumentRequestBuilder.class);
	
	public DocumentRequestBuilder(RDocument document) {
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
			return URLDecoder.decode(linkUrl.replace(DocumentRequestBuilder.LINKBASE, ""),"UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void createLinkTag(String tagName, TextRunPosition position) {
		this.createPlainLink(position, linkUrl(tagName));
	}
	
	public void createLinkTag(String tagName, Range range) {
		createLinkTag(tagName, TextRunPosition.of(range));
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
	
//	static Comparator<Tuple<Integer,Integer>> descendingTuples = new Comparator<Tuple<Integer,Integer>>() {
//		@Override
//		public int compare(Tuple<Integer, Integer> o1, Tuple<Integer, Integer> o2) {
//			return o2.getFirst().compareTo(o1.getFirst());
//		}
//	};
//	
//	static Comparator<Range> descendingRange = new Comparator<Range>() {
//		@Override
//		public int compare(Range o1, Range o2) {
//			return o2.getStartIndex().compareTo(o1.getStartIndex());
//		}
//	};
	
	public void deleteContent(List<TextRunPosition> ranges) {
		
		ArrayList<TextRunPosition> tmp = new ArrayList<>(ranges);
		tmp.sort(new TextRunPosition.Compare().reversed());
		tmp.forEach(r -> deleteContent(r));
	}
	
	
	public void deleteContent(TextRunPosition range) {
		this.add(new Request().setDeleteContentRange(new DeleteContentRangeRequest().setRange(range.getDocsRange())));
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
	
	public TextRunPosition insertImage(URI imageLink, TextRunPosition range, Size size) {
		return insertImage(imageLink, Optional.of(range), Optional.ofNullable(size)).get();
	}
	
	public Optional<TextRunPosition> insertImage(URI imageLink, Optional<TextRunPosition> start, Optional<Size> size) {
		
		InsertInlineImageRequest iiir = new InsertInlineImageRequest()
				.setUri(imageLink.toString());
		
		if (start.isPresent()) {
			iiir.setLocation(
					new Location()
					.setIndex(start.get().getStart()));
		} else {
			iiir.setEndOfSegmentLocation(new EndOfSegmentLocation());
		}
			
		size.ifPresent(s -> iiir.setObjectSize(s));
				
		this.add(new Request().setInsertInlineImage(iiir));
		return start.map(s -> s.offsetStart(0,1));
	}

	public void createTable(TextRunPosition tablePos, int rows, int cols, RNumericVector colWidths, RNumeric tableWidthInches) {
		
		this.add(
			new Request()
	        .setInsertTable(
	            new InsertTableRequest()
	            	.setLocation(tablePos.getTableStartLocation())
	                .setRows(rows)
	                .setColumns(cols)));
		setColumnWidths(tablePos.offset(1), colWidths, tableWidthInches);
		
		
		
		
	}
	
	private void setRowProperties(TextRunPosition tablePos) {
		this.add(new Request().setUpdateTableRowStyle(
				new UpdateTableRowStyleRequest()
					.setTableStartLocation(tablePos.getTableStartLocation())
					.setRowIndices(new ArrayList<>())
					.setTableRowStyle(new TableRowStyle()
							.setMinRowHeight(new Dimension().setMagnitude(0D).setUnit("PT"))
							// heights etc.
					)
					.setFields("minRowHeight")
		));
	}
	
	private void setColumnWidths(TextRunPosition tablePos, RNumericVector colWidths, RNumeric tableWidthInches) {
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
						.setTableStartLocation(tablePos.getTableStartLocation())
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

	public void writeTableContent(Collection<LongFormatTable> df, Table skeleton, TextRunPosition tablePos, Optional<String> tag) {
		List<TextRunPosition> cellPos = DocumentHelper.getSortedTableCells(skeleton, tablePos.getStart());
		
		// Cell merges
		df.stream()
			.filter(c -> c.colSpan().opt().orElse(1) > 1 || c.rowSpan().opt().orElse(1) > 1)
			.map(c -> new Request()
					.setMergeTableCells(new MergeTableCellsRequest()
							.setTableRange(new TableRange()
									.setTableCellLocation(new TableCellLocation()
											.setRowIndex(c.row().get()-1)
											.setColumnIndex(c.col().get()-1)
											.setTableStartLocation(new Location().setIndex(tablePos.getTableStart().get()))
									)
									.setRowSpan(c.rowSpan().get())
									.setColumnSpan(c.colSpan().get())
							)
					)
			)
			.forEach(this::add);
		
		cellPos.stream().forEach(t -> {
			df.stream().filter(c -> 
				c.col().get() == t.getColumn().get()+1 && 
				c.row().get() == t.getRow().get()+1)
			.findFirst()
			.ifPresent(c -> {
				// I can no longer remember why the offset is needed. Possible every cell starts with a paragraph marker
				TextRunPosition insertPosition = t.offset(1);
				TextRunPosition textPos = insertTextContent(insertPosition, c.label().opt().map(s -> s == "" ? " " : s).orElse(" "), Optional.of("NORMAL_TEXT"));
				formatText(textPos, c);
				this.add(new Request()
						.setUpdateParagraphStyle(new UpdateParagraphStyleRequest()
							.setRange(textPos.getDocsRange())
							.setParagraphStyle(new ParagraphStyle()
									.setAlignment(c.alignment().opt().map(checkRange("START","CENTER","END")).orElse("START")) //START,CENTER,END
							).setFields("alignment")
				));
				
				// If we are in the first cell and tag is present we insert it at the beginning as a single
				// create a blank table size rows/cols as position or end of document
				// https://invisible-characters.com/2060-WORD-JOINER.html
				// Use invisible char as base for a link.
				
				if (c.col().get() == 1 && c.row().get() == 1 && tag.isPresent()) {
					TextRunPosition linkPos = insertTextContent(insertPosition, "\u2060", Optional.of("NORMAL_TEXT"));
					this.createLinkTag(tag.get(), linkPos);
				}
				
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
										.setTableCellLocation(t.getTableCellLocation())
										// Merges have not happened yet
										.setRowSpan(1)
										.setColumnSpan(1)
								)
								.setFields("borderBottom,borderTop,borderLeft,borderRight,paddingBottom,paddingTop,paddingLeft,paddingRight,backgroundColor,contentAlignment")
						)
				);
				
				
			});
		});
		
		
			
			
		
		// set row heights to 0 for auto:
		setRowProperties(tablePos);
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
	
	static Pattern p = Pattern.compile("^<(sup|sub|b|i|u)>(.*)</(sup|sub|b|i|u)>$");
	
	private static String stripHtml(String text) {
		
		while (text.startsWith("<")) {
			Matcher m = p.matcher(text);
			if (m.find()) {
				// System.out.println(m.group(1));
				text = m.group(2);
			} else {
				// unsupported format for regex
				break;
			}
		} 
		return StringEscapeUtils.unescapeHtml4(text);
	}
	
	private static Optional<UpdateTextStyleRequest> textStyleFromHtml(String text, TextRunPosition textRun) {
		TextStyle tmp = new TextStyle();
		Set<String> matched = new HashSet<>(); 
		while (text.startsWith("<")) {
			Matcher m = p.matcher(text);
			if (m.find()) {
				String tag = m.group(1);
				switch (tag) {
					case "sup":
						tmp.setBaselineOffset("SUPERSCRIPT");
						matched.add("baselineOffset");
						break;
					case "sub":
						tmp.setBaselineOffset("SUBSCRIPT");
						matched.add("baselineOffset");
						break;
					case "b":
						tmp.setBold(Boolean.TRUE);
						matched.add("bold");
						break;
					case "i":
						tmp.setItalic(Boolean.TRUE);
						matched.add("italic");
						break;
					case "u":
						tmp.setUnderline(Boolean.TRUE);
						matched.add("underline");
						break;
				}
				text = m.group(2);
			} else {
				// unsupported format for regex
				break;
			}
		} 
		if (matched.isEmpty()) return Optional.empty();
		UpdateTextStyleRequest out = new UpdateTextStyleRequest()
				.setRange(textRun.getDocsRange())
				.setTextStyle(tmp)
				.setFields(matched.stream().collect(Collectors.joining(",")));
		return Optional.of(out);
	}
	
	
	public TextRunPosition insertTextContent(TextRunPosition range, String unformattedText, Optional<String> style) {
		
		String plainText = stripHtml(unformattedText);
		TextRunPosition textRun = insertTextRun(range, plainText);
		
		// identify html elements in unformattedText
		Optional<UpdateTextStyleRequest> textStyle = textStyleFromHtml(unformattedText, textRun);
					
		String c = style.map(checkRange("NORMAL_TEXT","TITLE","SUBTITLE","HEADING_1","HEADING_2","HEADING_3","HEADING_4","HEADING_5","HEADING_6")).orElse("NORMAL_TEXT");
		
		if (style.isPresent() && !textRun.isEmpty()) {
			this.add(new Request().setUpdateParagraphStyle(new UpdateParagraphStyleRequest()
	            .setRange(textRun.getDocsRange())
	            .setParagraphStyle(new ParagraphStyle()
	                    .setNamedStyleType(c))
	            .setFields("namedStyleType")
			));
		}
		
		if (textStyle.isPresent() && !textRun.isEmpty()) {
			this.add(new Request().setUpdateTextStyle(textStyle.get()));
		}
			
		return textRun;
	}
	
	private TextRunPosition insertTextRun(TextRunPosition position, String unformattedText) {
		if (unformattedText == null || unformattedText == "") TextRunPosition.of(
				null, //pageId
				position.getShapeId().orElse(null),
				position.getStart(),
				position.getStart());
		this.add(
				new Request()
					.setInsertText(
						new InsertTextRequest()
						.setText(unformattedText)
						.setLocation(new Location().setIndex(position.getStart()) //the plus one here is to make sure we are in the paragraph in the cell.
				))
			);
			int len = StringUtils.strip(unformattedText,"\n").length();
			int offset = unformattedText.indexOf(StringUtils.strip(unformattedText,"\n"));
			TextRunPosition textRun = TextRunPosition.of(
					null, //pageId
					position.getShapeId().orElse(null),
					position.getStart()+offset,
					position.getStart()+offset+len);
			
		return textRun;
	}
	
	private void formatText(TextRunPosition textRun, TextFormat c) {
		c.fontSize().opt().ifPresent(f -> {
			this.add(
					new Request()
					.setUpdateTextStyle(
						new UpdateTextStyleRequest()
						.setRange(textRun.getDocsRange())
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
						.setRange(textRun.getDocsRange())
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
						.setRange(textRun.getDocsRange())
						.setTextStyle(new TextStyle()
								.setBold(f.contains("bold"))
								.setItalic(f.contains("italic"))
								.setUnderline(f.contains("underlined"))
						)
						.setFields("bold,italic,underline")
					)	
			);
		});
//		c.smallCaps().opt().ifPresent(f -> {
//			this.add(
//					new Request()
//					.setUpdateTextStyle(
//						new UpdateTextStyleRequest()
//						.setRange(textRun.getDocsRange())
//						.setTextStyle(new TextStyle().setSmallCaps(f))
//						.setFields("smallCaps")
//					)	
//			);
//		});
//		c.superscript().opt().filter(b -> b.booleanValue()).ifPresent(f -> {
//			this.add(
//					new Request()
//					.setUpdateTextStyle(
//						new UpdateTextStyleRequest()
//						.setRange(textRun.getDocsRange())
//						.setTextStyle(new TextStyle().setBaselineOffset("SUPERSCRIPT"))
//						.setFields("baselineOffset")
//					)
//			);
//		});
//		c.subscript().opt().filter(b -> b.booleanValue()).ifPresent(f -> {
//			this.add(
//					new Request()
//					.setUpdateTextStyle(
//						new UpdateTextStyleRequest()
//						.setRange(textRun.getDocsRange())
//						.setTextStyle(new TextStyle().setBaselineOffset("SUBSCRIPT"))
//						.setFields("baselineOffset")
//					)
//			);
//		});
//		c.strikethrough().opt().filter(b -> b.booleanValue()).ifPresent(f -> {
//			this.add(
//					new Request()
//					.setUpdateTextStyle(
//						new UpdateTextStyleRequest()
//						.setRange(textRun.getDocsRange())
//						.setTextStyle(new TextStyle().setStrikethrough(true))
//						.setFields("strikethrough")
//					)
//			);
//		});
	}
	
	public void writeTextContent(TextRunPosition position, List<LongFormatText> df) {
		Collections.reverse(df);
		df
			.stream()
			.filter(c -> c.label().get() != null && c.label().get() != "")
			.forEach(c -> {
			
				TextRunPosition textRun = insertTextRun(position, c.label().get());
				formatText(textRun, c);
				
				if (!c.link().isNa()) {
					createLink(textRun, c.link().get());
				}
			});
	}

	public void createLink(TextRunPosition textRun, String url) {
		this.add(new Request()
                .setUpdateTextStyle(new UpdateTextStyleRequest()
                        .setRange(textRun.getDocsRange())
                        .setTextStyle(new TextStyle()
                                .setLink(new Link().setUrl(url)))
                        .setFields("link")));
	}
	
	public void createPlainLink(TextRunPosition range, String url) {
		this.add(
				new Request().setUpdateTextStyle(
					new UpdateTextStyleRequest()
						.setTextStyle(
								new TextStyle()
									.setLink(new Link().setUrl(url))
									// decided to leave this out for the time being.
									// useful to have tagged data highlighted but necessary to be able to remove all links.
									// which is now possible in the main API.
 									.setUnderline(Boolean.FALSE)
									.setForegroundColor(DocumentHelper.col(0.3F,0.3F,0.3F))
						)
						.setRange(range.getDocsRange())
						.setFields("link,underline,foregroundColor"))
						//.setFields("link"))
				);
	}
	
	public void removeLink(TextRunPosition range) {
		this.add(
				new Request().setUpdateTextStyle(
					new UpdateTextStyleRequest()
						.setTextStyle(new TextStyle())
						.setRange(range.getDocsRange())
						// .setFields("link,underline,foregroundColor"))
						.setFields("link"))
				);
	}
		
}
