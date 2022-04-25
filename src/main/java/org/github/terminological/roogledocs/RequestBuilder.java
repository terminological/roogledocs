package org.github.terminological.roogledocs;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.github.terminological.roogledocs.datatypes.LongFormatTable;
import org.github.terminological.roogledocs.datatypes.Tuple;
import org.github.terminological.roogledocs.datatypes.TupleList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.services.docs.v1.model.BatchUpdateDocumentRequest;
import com.google.api.services.docs.v1.model.Color;
import com.google.api.services.docs.v1.model.CreateNamedRangeRequest;
import com.google.api.services.docs.v1.model.DeleteContentRangeRequest;
import com.google.api.services.docs.v1.model.Dimension;
import com.google.api.services.docs.v1.model.EndOfSegmentLocation;
import com.google.api.services.docs.v1.model.InsertInlineImageRequest;
import com.google.api.services.docs.v1.model.InsertTableRequest;
import com.google.api.services.docs.v1.model.InsertTextRequest;
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
import com.google.api.services.docs.v1.model.TextStyle;
import com.google.api.services.docs.v1.model.UpdateParagraphStyleRequest;
import com.google.api.services.docs.v1.model.UpdateTableCellStyleRequest;
import com.google.api.services.docs.v1.model.UpdateTableColumnPropertiesRequest;
import com.google.api.services.docs.v1.model.UpdateTextStyleRequest;
import com.google.api.services.docs.v1.model.WeightedFontFamily;
import com.google.api.services.docs.v1.model.WriteControl;

import uk.co.terminological.rjava.types.RBoundDataframe;
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
	
	public void createNamedRange(String tagName, int start, int end, String segmentId) {
		Range tmp = new Range()
				.setStartIndex(start)
				.setEndIndex(end);
		if (segmentId != null) {
			tmp.setSegmentId(segmentId);
		}
		this.add(
				new Request().setCreateNamedRange(
					new CreateNamedRangeRequest()
						.setName(tagName)
						.setRange(tmp))
				);
	}
	
	public void createNamedRange(String tagName, int start, int end) {
		createNamedRange(tagName,start,end,null);
	}
	
	public void createNamedRange(String tagName, Range range) {
		createNamedRange(tagName,range.getStartIndex(), range.getEndIndex(), range.getSegmentId());
	}

	public boolean any() {
		return this.size() > 0;
	}
	
	public Range insertText(String newText, Integer start) {
		return insertText(newText, Optional.of(start), Optional.empty()).get();
	}
	
	public Optional<Range> insertText(String newText, Optional<Integer> start, Optional<TextStyle> style) {
		InsertTextRequest itr = new InsertTextRequest();
		if (start.isPresent()) {
			
			itr.setLocation(
					new Location()
					.setIndex(start.get()))
			.setText(newText);
			
			this.add(
				new Request()
				.setInsertText(itr));
			
			Range rng = new Range().setStartIndex(start.get()).setEndIndex(start.get() + newText.length());
			
			style.ifPresent(s -> formatText(rng,s));
			
			return Optional.of(rng);
			
		} else {
			
			// TODO: figure out how to set the format of this without getting the whole document again.
			itr.setEndOfSegmentLocation(new EndOfSegmentLocation());
			this.add(new Request().setInsertText(itr));
			return Optional.empty();
		}
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
	
	public void formatText(Range range, TextStyle style) {
		this.add(
				new Request()
					.setUpdateTextStyle(
						new UpdateTextStyleRequest()
							.setRange(range)
							.setTextStyle(style)
							.setFields("*")
					)
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
	
	public void createTableAtEnd(int position, int rows, int cols, RNumericVector colWidths, RNumeric tableWidthInches) {
		this.add(
			new Request()
	        .setInsertTable(
	            new InsertTableRequest()
	            	.setEndOfSegmentLocation(new EndOfSegmentLocation())
	                .setRows(rows)
	                .setColumns(cols)));
		setColumnWidths(position, colWidths, tableWidthInches);
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
			Optional<List<Request>> tmp = df.stream().filter(c -> 
				c.col().get() == t.getFirst().getColumnIndex()+1 && 
				c.row().get() == t.getFirst().getRowIndex()+1)
			.findFirst()
			.map(c -> {
				int len = c.label().get().length();
				return Arrays.asList(
					new Request()
						.setInsertText(
							new InsertTextRequest()
							.setText(c.label().get())
							.setLocation(new Location().setIndex(t.getSecond().getStartIndex()+1)) //the plus one here is to make sure we are in the paragraph in the cell.
						),
					new Request()
						.setUpdateTextStyle(
							new UpdateTextStyleRequest()
							.setRange(new Range()
									.setStartIndex(t.getSecond().getStartIndex()+1)
									.setEndIndex(t.getSecond().getStartIndex()+len+1)
							)
							.setTextStyle(new TextStyle()
									.setBold(c.fontFace().get().contains("bold"))
									.setItalic(c.fontFace().get().contains("italic"))
									.setFontSize(new Dimension().setMagnitude(c.fontSize().get()).setUnit("PT"))
									.setWeightedFontFamily(new WeightedFontFamily().setFontFamily(c.fontName().get()))
							)
							.setFields("bold,italic,fontSize,weightedFontFamily")
						),
					new Request()
						.setUpdateParagraphStyle(new UpdateParagraphStyleRequest()
							.setRange(new Range()
									.setStartIndex(t.getSecond().getStartIndex()+1)
									.setEndIndex(t.getSecond().getStartIndex()+len+1)
							)
							.setParagraphStyle(new ParagraphStyle()
									.setAlignment(c.alignment().get()) //START,CENTER,END
							)
							.setFields("alignment")
						),
					new Request()
						.setUpdateTableCellStyle(
							new UpdateTableCellStyleRequest()
								.setTableCellStyle(new TableCellStyle()
										.setBorderBottom(border(c.bottomBorderWeight().get()))
										.setBorderTop(border(c.topBorderWeight().get()))
										.setBorderLeft(border(c.leftBorderWeight().get()))
										.setBorderRight(border(c.rightBorderWeight().get()))
										// TODO: foreground colour
										.setBackgroundColor(fromHex(c.fillColour().get()))
										.setContentAlignment(c.valignment().get()) //TOP,MIDDLE,BOTTOM
									)
								// .setTableStartLocation(new Location().setIndex(tableStart))
								.setTableRange(new TableRange()
										.setTableCellLocation(t.getFirst())
										.setRowSpan(1)
										.setColumnSpan(1)
								)
								.setFields("borderBottom,borderTop,borderLeft,borderRight,backgroundColor,contentAlignment")
						)
								
						
					);
			});
			tmp.ifPresent(this::addAll);
			// Cell merges
			df.stream()
				.filter(c -> c.colSpan().get() > 1 || c.rowSpan().get() > 1)
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
	}

	public static OptionalColor fromHex(String hex) {
		
		Float red = ((float) Integer.parseInt(hex.substring(1, 3),16))/256F;
		Float green = ((float) Integer.parseInt(hex.substring(3, 5),16))/256F;
		Float blue = ((float) Integer.parseInt(hex.substring(5, 7),16))/256F;
		return new OptionalColor().setColor(new Color().setRgbColor(
			new RgbColor().setRed(red).setGreen(green).setBlue(blue)
		));
		
	}
	
//	public void updateTableFormat(List<LongFormatTable> df, Table skeleton) {
//		// int tableStart = skeleton.getTableRows().get(0).getTableCells().get(0).getStartIndex();
//				TupleList<TableCellLocation, Range> cellPos = DocumentHelper.getSortedTableCells(skeleton);
//				cellPos.stream().forEach(t -> {
//					Optional<List<Request>> tmp = df.stream().filter(c -> 
//					c.col().get() == t.getFirst().getColumnIndex()+1 && 
//					c.row().get() == t.getFirst().getRowIndex()+1)
//				.findFirst()
//				.map(c -> {
//					
//				});
//				
//	}
}
