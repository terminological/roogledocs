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
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.github.terminological.roogledocs.datatypes.LongFormatTable;
import org.github.terminological.roogledocs.datatypes.LongFormatText;
import org.github.terminological.roogledocs.datatypes.TextFormat;
import org.github.terminological.roogledocs.datatypes.TupleList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.services.slides.v1.Slides.Presentations.BatchUpdate;
import com.google.api.services.slides.v1.model.AffineTransform;
import com.google.api.services.slides.v1.model.BatchUpdatePresentationRequest;
import com.google.api.services.slides.v1.model.CreateImageRequest;
import com.google.api.services.slides.v1.model.CreateParagraphBulletsRequest;
import com.google.api.services.slides.v1.model.CreateSlideRequest;
import com.google.api.services.slides.v1.model.CreateTableRequest;
import com.google.api.services.slides.v1.model.DeleteObjectRequest;
import com.google.api.services.slides.v1.model.DeleteTextRequest;
import com.google.api.services.slides.v1.model.Dimension;
import com.google.api.services.slides.v1.model.ImageProperties;
import com.google.api.services.slides.v1.model.InsertTextRequest;
import com.google.api.services.slides.v1.model.LayoutPlaceholderIdMapping;
import com.google.api.services.slides.v1.model.LayoutReference;
import com.google.api.services.slides.v1.model.Link;
import com.google.api.services.slides.v1.model.MergeTableCellsRequest;
import com.google.api.services.slides.v1.model.OpaqueColor;
import com.google.api.services.slides.v1.model.PageElementProperties;
import com.google.api.services.slides.v1.model.ParagraphStyle;
import com.google.api.services.slides.v1.model.Request;
import com.google.api.services.slides.v1.model.RgbColor;
import com.google.api.services.slides.v1.model.Size;
import com.google.api.services.slides.v1.model.SolidFill;
import com.google.api.services.slides.v1.model.Table;
import com.google.api.services.slides.v1.model.TableBorderFill;
import com.google.api.services.slides.v1.model.TableBorderProperties;
import com.google.api.services.slides.v1.model.TableCellBackgroundFill;
import com.google.api.services.slides.v1.model.TableCellLocation;
import com.google.api.services.slides.v1.model.TableCellProperties;
import com.google.api.services.slides.v1.model.TableColumnProperties;
import com.google.api.services.slides.v1.model.TableRange;
import com.google.api.services.slides.v1.model.TableRowProperties;
import com.google.api.services.slides.v1.model.TextStyle;
import com.google.api.services.slides.v1.model.UpdateImagePropertiesRequest;
import com.google.api.services.slides.v1.model.UpdateParagraphStyleRequest;
import com.google.api.services.slides.v1.model.UpdateTableBorderPropertiesRequest;
import com.google.api.services.slides.v1.model.UpdateTableCellPropertiesRequest;
import com.google.api.services.slides.v1.model.UpdateTableColumnPropertiesRequest;
import com.google.api.services.slides.v1.model.UpdateTableRowPropertiesRequest;
import com.google.api.services.slides.v1.model.UpdateTextStyleRequest;
import com.google.api.services.slides.v1.model.WeightedFontFamily;
import com.google.api.services.slides.v1.model.WriteControl;

import uk.co.terminological.rjava.types.RNumericVector;


public class SlidesRequestBuilder extends ArrayList<Request> {

	RPresentation document;
	private Logger log = LoggerFactory.getLogger(SlidesRequestBuilder.class);
	
	public SlidesRequestBuilder(RPresentation document) {
		this.document = document;
	}

	public void sendRequest() throws IOException {
		if (this.any()) {
			BatchUpdatePresentationRequest batchUpdateRequest =
				new BatchUpdatePresentationRequest()
				.setRequests(this)
				.setWriteControl(new WriteControl().setRequiredRevisionId(document.getPresentation().getRevisionId()));

			BatchUpdate tmp = document.getService().getSlides().presentations().batchUpdate(document.getDocId(), batchUpdateRequest);
			
			// DEBUG
			
//			tmp.setPrettyPrint(true).buildHttpRequest().getContent().writeTo(System.out);
//			System.out.println(); System.out.println();
			
			
			tmp.execute();
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
			return URLDecoder.decode(linkUrl.replace(SlidesRequestBuilder.LINKBASE, ""),"UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void createLinkTag(String tagName, TextRunPosition textRun) {
		this.createPlainLink(textRun, linkUrl(tagName));
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
		
	public void deleteContent(List<TextRunPosition> textRuns) {
		textRuns.sort(new TextRunPosition.Compare().reversed());
		textRuns.forEach(r -> deleteContent(r));
	}
	
	public void deleteContent(TextRunPosition textRun) {
		if (!textRun.isEmpty()) {
			this.add(new Request().setDeleteText(textRun.setPosition(new DeleteTextRequest())));
		}
	}
	
	public void deleteObject(String objectId) {
		deleteObject(Collections.singletonList(objectId));
	}
	
	public void deleteObject(List<String> objectIds) {
		objectIds.forEach(o -> 
			this.add(new Request().setDeleteObject(new DeleteObjectRequest().setObjectId(o))));
	}
	
//	public void updateImageWithUri(String imageId, URI imageLink) {
//		this.add(
//			new Request().setReplaceImage(new ReplaceImageRequest()
//				.setImageObjectId(imageId)
//				.setImageReplaceMethod("CENTER_CROP")
//				.setUrl(imageLink.toString()))
//		);
//	}
	
	// PREDEFINED_LAYOUT_UNSPECIFIED
	public String createSlideAtEnd(String layoutId, List<LayoutPlaceholderIdMapping> mappings) {
		return createSlide(Optional.empty(), layoutId, mappings);
	}
	
	// This will return the pageId of the new slide
	public String createSlide(Optional<Integer> index, String layoutId, List<LayoutPlaceholderIdMapping> mappings) {
		String slideId = UUID.randomUUID().toString();
		CreateSlideRequest tmp = new CreateSlideRequest()
	              .setObjectId(slideId)
	              .setSlideLayoutReference(new LayoutReference()
	                  .setLayoutId(layoutId)
	              );
		if (!mappings.isEmpty()) tmp.setPlaceholderIdMappings(mappings);
	    index.ifPresent(i -> tmp.setInsertionIndex(i));
		this.add(new Request()
		          .setCreateSlide(tmp));
		return(slideId);
	}
	
	public String insertImage(URI imageLink, String pageId, Optional<Size> size, Optional<AffineTransform> transform, Optional<String> tagName) {
		
		String imageId = UUID.randomUUID().toString(); 
		
		
		PageElementProperties loc = new PageElementProperties();
		loc.setPageObjectId(pageId);
		size.ifPresent(s -> loc.setSize(s));
		transform.ifPresent(t -> loc.setTransform(t));
		
		CreateImageRequest iiir = new CreateImageRequest()
				.setObjectId(imageId)
				.setUrl(imageLink.toString())
				.setElementProperties(loc);
		
		this.add(new Request().setCreateImage(iiir));
		
		tagName.ifPresent(tn -> {
				this.add(new Request().setUpdateImageProperties(
						new UpdateImagePropertiesRequest()
							.setObjectId(imageId)
							.setFields("link.url")
							.setImageProperties(
									new ImageProperties().setLink(new Link().setUrl(linkUrl(tn)))
							)));
				});
		return(imageId);
	}
	
	public String createTable(String pageId, int rows, int cols, Size size, AffineTransform transform, RNumericVector colWidths) {
		String tableId = UUID.randomUUID().toString(); 
		
		PageElementProperties loc = new PageElementProperties();
		loc.setPageObjectId(pageId);
		loc.setSize(size);
		loc.setTransform(transform);
		Double maxWidth = SlidesHelper.calculateWidth(size, transform);
		
		this.add(
			new Request().setCreateTable(
	            new CreateTableRequest()
	            	.setObjectId(tableId)
	            	.setElementProperties(loc)
	            	.setRows(rows)
	                .setColumns(cols)));
		
		setColumnWidths(tableId, colWidths, maxWidth);
		
		return tableId;
	}
	
	private void setRowProperties(String tableId) {
		this.add(new Request().setUpdateTableRowProperties(
				new UpdateTableRowPropertiesRequest()
					.setObjectId(tableId)
					.setRowIndices(new ArrayList<>())
					.setTableRowProperties(
							new TableRowProperties()
							.setMinRowHeight(new Dimension().setMagnitude(0D).setUnit("PT"))
							// heights etc.
					).setFields("minRowHeight")
		));
	}
	
	private void setColumnWidths(String tableId, RNumericVector colWidths, Double tableWidthInches) {
		Double total = colWidths.stream().mapToDouble(c -> c.get()).sum();
		for (int i=0; i<colWidths.size(); i++) {
			Double colWidth = colWidths.get(i).get()/total*tableWidthInches;
			this.add(
					new Request()
					.setUpdateTableColumnProperties(new UpdateTableColumnPropertiesRequest()
						.setColumnIndices(Arrays.asList(i)) //Zero based?
						.setTableColumnProperties(new TableColumnProperties()
								.setColumnWidth(new Dimension().setMagnitude(colWidth*72).setUnit("PT"))
						)
						.setObjectId(tableId)
						.setFields("columnWidth")
					)
			);
		}
	}
	
//	private static TableCellBorder border(Double weight) {
//		return new TableCellBorder().setWidth(
//				new Dimension().setMagnitude(weight).setUnit("PT")
//		).setDashStyle("SOLID").setColor(fromHex("#000000"));
//	}

	public void writeTableContent(Collection<LongFormatTable> df, Table skeleton, String pageId, String tableId, Optional<String> tag) {
		TupleList<TableCellLocation, TextRunPosition> cellPos = SlidesHelper.getSortedTableCells(pageId, skeleton, tableId);
		
		// Cell merges
		df.stream()
			.filter(c -> c.colSpan().opt().orElse(1) > 1 || c.rowSpan().opt().orElse(1) > 1)
			.map(c -> new Request()
					.setMergeTableCells(new MergeTableCellsRequest()
							.setObjectId(tableId)
							.setTableRange(new TableRange()
									.setLocation(new TableCellLocation()
											.setRowIndex(c.row().get()-1)
											.setColumnIndex(c.col().get()-1)
											
									)
									.setRowSpan(c.rowSpan().get())
									.setColumnSpan(c.colSpan().get())
							)
					)
			)
			.forEach(this::add);
					
		
		cellPos.stream().forEach(t -> {
			
			df.stream().filter(c -> 
				c.col().get() == t.getFirst().getColumnIndex()+1 && 
				c.row().get() == t.getFirst().getRowIndex()+1)
			.findFirst()
			.ifPresent(c -> {
				TextRunPosition textPos = insertTextContent(t.getSecond(), c.label().opt().map(s -> s == "" ? " " : s).orElse(" "), Optional.empty());
				formatText(textPos, c);
				this.add(new Request()
						.setUpdateParagraphStyle(
								textPos.setPosition(
										new UpdateParagraphStyleRequest()
											.setStyle(new ParagraphStyle()
													.setAlignment(c.alignment().opt().map(checkRange("START","CENTER","END")).orElse("START")) //START,CENTER,END
											).setFields("alignment")
				)));
				
				
				this.removeLineSpacing(textPos);
				this.add(new Request()
						.setUpdateTableCellProperties(
							textPos.setPosition(
								new UpdateTableCellPropertiesRequest()
								.setTableCellProperties(new TableCellProperties()
//										.setBorderBottom(border(c.bottomBorderWeight().opt().orElse(0D)))
//										.setBorderTop(border(c.topBorderWeight().opt().orElse(0D)))
//										.setBorderLeft(border(c.leftBorderWeight().opt().orElse(0D)))
//										.setBorderRight(border(c.rightBorderWeight().opt().orElse(0D)))
										// TODO: foreground colour, border styles
										.setTableCellBackgroundFill(
												new TableCellBackgroundFill().setSolidFill(
														new SolidFill().setColor(
																fromHex(c.fillColour().opt().orElse("#FFFFFF"))
																)))
										.setContentAlignment(c.valignment().opt().map(checkRange("TOP","MIDDLE","BOTTOM")).orElse("TOP")) //TOP,MIDDLE,BOTTOM
//										.setPaddingBottom(new Dimension().setMagnitude(c.bottomPadding().opt().orElse(1.0)).setUnit("PT"))
//										.setPaddingTop(new Dimension().setMagnitude(c.topPadding().opt().orElse(1.0)).setUnit("PT"))
//										.setPaddingLeft(new Dimension().setMagnitude(c.leftPadding().opt().orElse(1.0)).setUnit("PT"))
//										.setPaddingRight(new Dimension().setMagnitude(c.rightPadding().opt().orElse(1.0)).setUnit("PT"))
									)
								// .setTableStartLocation(new Location().setIndex(tableStart))
								.setTableRange(new TableRange()
										.setLocation(t.getFirst())
										// Merges have not happened yet
										.setRowSpan(1)
										.setColumnSpan(1)
								)
								.setFields("tableCellBackgroundFill.solidFill.color,contentAlignment")
							)
						)
				);
				if (c.col().get() == 1 && c.row().get() == 1 && tag.isPresent()) {
					TextRunPosition linkPos = insertTextContent(t.getSecond(), "\u2060", Optional.empty());
					this.createLinkTag(tag.get(), linkPos);
				}
				this.add(new Request().setUpdateTableBorderProperties(border(textPos, "TOP",t.getFirst(),c.topBorderWeight().opt().orElse(0D))));
				this.add(new Request().setUpdateTableBorderProperties(border(textPos, "BOTTOM",t.getFirst(),c.bottomBorderWeight().opt().orElse(0D))));
				this.add(new Request().setUpdateTableBorderProperties(border(textPos, "LEFT",t.getFirst(),c.leftBorderWeight().opt().orElse(0D))));
				this.add(new Request().setUpdateTableBorderProperties(border(textPos, "RIGHT",t.getFirst(),c.rightBorderWeight().opt().orElse(0D))));
			});
		});
			
		
		
		// set row heights to 0 for auto:
		setRowProperties(tableId);
	}

	
	private static UpdateTableBorderPropertiesRequest border(TextRunPosition pos, String side, TableCellLocation cell, Double weight) {
		
		TableBorderProperties prop;
		if (weight > 0) {
			prop = new TableBorderProperties()
			.setWeight(
					new Dimension().setMagnitude(weight).setUnit("PT")
			)
			.setDashStyle("SOLID")
			.setTableBorderFill(
					new TableBorderFill().setSolidFill(
							new SolidFill().setAlpha(1F))
			);
		} else {
			prop = new TableBorderProperties()
					.setTableBorderFill(
							new TableBorderFill().setSolidFill(
									new SolidFill().setAlpha(0F))
					);
			
		}
		
		return pos.setPosition(
				new UpdateTableBorderPropertiesRequest()
					.setBorderPosition(side)
					.setTableRange(new TableRange()
						.setLocation(cell)
						// Merges have not happened yet
						.setRowSpan(1)
						.setColumnSpan(1)
					)
					.setTableBorderProperties(prop)
					.setFields("weight,dashStyle,tableBorderFill")
				);
	}
	
	public static OpaqueColor fromHex(String hex) {
		
		Float red = ((float) Integer.parseInt(hex.substring(1, 3),16))/256F;
		Float green = ((float) Integer.parseInt(hex.substring(3, 5),16))/256F;
		Float blue = ((float) Integer.parseInt(hex.substring(5, 7),16))/256F;
		return new OpaqueColor().setRgbColor(
					new RgbColor().setRed(red).setGreen(green).setBlue(blue)
		);
		
	}
	
	Function<String,String> checkRange(String... range) {
		return (s) -> {
			if(!Arrays.asList(range).contains(s))
				throw new RuntimeException("item must be one of: "+String.join(", ", range));
			return s;
		};
	}
	
	public TextRunPosition insertTextContent(TextRunPosition textRun, String unformattedText, Optional<String> style) {
		String plainText = stripHtml(unformattedText);
		if (plainText == null || plainText == "") return textRun;
		TextRunPosition textRun2 = insertTextRun(textRun, plainText);
		
		Optional<UpdateTextStyleRequest> textStyle = textStyleFromHtml(unformattedText, textRun2);
		
		if (style.isPresent()) {
			String c = style.map(checkRange(
					"BULLET_DISC_CIRCLE_SQUARE",
					"BULLET_DIAMONDX_ARROW3D_SQUARE",
					"BULLET_CHECKBOX",
					"BULLET_ARROW_DIAMOND_DISC",
					"BULLET_STAR_CIRCLE_SQUARE",
					"BULLET_ARROW3D_CIRCLE_SQUARE",
					"BULLET_LEFTTRIANGLE_DIAMOND_DISC",
					"BULLET_DIAMONDX_HOLLOWDIAMOND_SQUARE",
					"BULLET_DIAMOND_CIRCLE_SQUARE",
					"NUMBERED_DIGIT_ALPHA_ROMAN",
					"NUMBERED_DIGIT_ALPHA_ROMAN_PARENS",
					"NUMBERED_DIGIT_NESTED",
					"NUMBERED_UPPERALPHA_ALPHA_ROMAN",
					"NUMBERED_UPPERROMAN_UPPERALPHA_DIGIT",
					"NUMBERED_ZERODIGIT_ALPHA_ROMAN"
					)).get();
			this.add(new Request().setCreateParagraphBullets(
				textRun2.setPosition(
					new CreateParagraphBulletsRequest().setBulletPreset(c)
					)
				)
			);
		}
		
		if (textStyle.isPresent() && !textRun2.isEmpty()) {
			this.add(new Request().setUpdateTextStyle(textStyle.get()));
		}
			
		return textRun2;
	}
	
	static Pattern p = Pattern.compile("^<(sup|sub|b|i|u)>(.*)</(sup|sub|b|i|u)>$");
	
	static String stripHtml(String text) {
		
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
	
	static Optional<UpdateTextStyleRequest> textStyleFromHtml(String text, TextRunPosition textRun) {
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
		UpdateTextStyleRequest out = textRun.setPosition(new UpdateTextStyleRequest()
				.setStyle(tmp)
				.setFields(matched.stream().collect(Collectors.joining(","))));
		return Optional.of(out);
	}
	
	private TextRunPosition insertTextRun(TextRunPosition textRun, String unformattedText) {
		if (unformattedText == null || unformattedText == "") return textRun;
		InsertTextRequest tmp = new InsertTextRequest().setText(unformattedText);
		textRun.setPosition(tmp);
		this.add(new Request().setInsertText(tmp));
		int len = StringUtils.strip(unformattedText,"\n").length();
		int offset = unformattedText.indexOf(StringUtils.strip(unformattedText,"\n"));
		return textRun.offsetStart(offset, offset+len);
	}
	
	public void setFontSize(TextRunPosition textRun, Double fontSize) {
		this.add(
				new Request()
				.setUpdateTextStyle(
						textRun.setPosition(
								new UpdateTextStyleRequest()
										.setStyle(new TextStyle()
												.setFontSize(new Dimension().setMagnitude(fontSize).setUnit("PT"))
										)
										.setFields("fontSize")
				))	
		);
	}
	
	public void removeLineSpacing(TextRunPosition textRun) {
		this.add(
				new Request()
				.setUpdateParagraphStyle(
						textRun.setPosition(
								new UpdateParagraphStyleRequest()
										.setStyle(new ParagraphStyle()
												.setSpaceAbove(new Dimension().setMagnitude(1.0).setUnit("PT"))
												.setSpaceBelow(new Dimension().setMagnitude(1.0).setUnit("PT"))
										)
										.setFields("spaceAbove, spaceBelow")
				))	
		);
	}
	
	public void formatText(TextRunPosition textRun, TextFormat c) {
		c.fontSize().opt().ifPresent(f -> {
			this.add(
					new Request()
					.setUpdateTextStyle(
							textRun.setPosition(
									new UpdateTextStyleRequest()
											.setStyle(new TextStyle()
													.setFontSize(new Dimension().setMagnitude(f).setUnit("PT"))
											)
											.setFields("fontSize")
					))	
			);
		});
		c.fontName().opt().ifPresent(f -> {
			this.add(
					new Request()
					.setUpdateTextStyle(
							textRun.setPosition(
								new UpdateTextStyleRequest()
									.setStyle(new TextStyle()
											.setWeightedFontFamily(new WeightedFontFamily().setFontFamily(f))
									)
									.setFields("weightedFontFamily")
								)
							)
			);
		});
		c.fontFace().opt().ifPresent(f -> {
			this.add(
					new Request()
					.setUpdateTextStyle(
							textRun.setPosition(
								new UpdateTextStyleRequest()
								.setStyle(new TextStyle()
										.setBold(f.contains("bold"))
										.setItalic(f.contains("italic"))
										.setUnderline(f.contains("underlined"))
								)
								.setFields("bold,italic,underline")
					))	
			);
		});
		
	}
	
	public void writeTextContent(TextRunPosition textRun, List<LongFormatText> df) {
		Collections.reverse(df);
		df
			.stream()
			.filter(c -> c.label().get() != null && c.label().get() != "")
			.forEach(c -> {
			
				TextRunPosition inserted = insertTextRun(textRun, c.label().get());
				formatText(inserted, c);
				
				if (!c.link().isNa()) {
					if (c.link().get().startsWith(LINKBASE)) {
						createPlainLink(inserted, c.link().get());
					} else {
						createLink(inserted, c.link().get());
					}
				}
			});
	}

	public void createLink(TextRunPosition textRun, String url) {
		this.add(new Request()
                .setUpdateTextStyle(
                		textRun.setPosition(
                		new UpdateTextStyleRequest()
                        .setStyle(new TextStyle()
                                .setLink(new Link().setUrl(url)))
                        .setFields("link"))));
	}
	
	public void createPlainLink(TextRunPosition textRun, String url) {
		this.add(
				new Request().setUpdateTextStyle(
						textRun.setPosition(
					new UpdateTextStyleRequest()
						.setStyle(
								new TextStyle()
									.setLink(new Link().setUrl(url))
									// decided to leave this out for the time being.
									// useful to have tagged data highlighted but necessary to be able to remove all links.
									// which is now possible in the main API.
 									.setUnderline(Boolean.FALSE)
									.setForegroundColor(SlidesHelper.col(0.05F,0.05F,0.05F))
						)
						.setFields("link,underline,foregroundColor"))
						// .setFields("link"))
					)
				);
	}
	
	public void removeTextLink(TextRunPosition textRun) {
		this.add(
				new Request().setUpdateTextStyle(
					new UpdateTextStyleRequest()
						.setObjectId(textRun.shapeId)
						.setStyle(new TextStyle())
						.setTextRange(textRun.range)
						// .setFields("link,underline,foregroundColor"))
						.setFields("link"))
				);
	}
	
	public void removeImageLink(TextRunPosition textRun) {
		this.add(
				new Request().setUpdateImageProperties(
					new UpdateImagePropertiesRequest()
						.setObjectId(textRun.shapeId)
						.setImageProperties(new ImageProperties())
						// .setFields("link,underline,foregroundColor"))
						.setFields("link"))
				);
	}

	public void removeTableLink(TextRunPosition tableRun) {
		this.add(
				new Request().setDeleteText(
						tableRun.offsetStart(1, 2).setPosition(
						new DeleteTextRequest()
				)));
	}
		
}
