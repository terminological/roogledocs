package org.github.terminological.roogledocs;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.google.api.services.docs.v1.model.Location;
import com.google.api.services.slides.v1.model.CreateParagraphBulletsRequest;
import com.google.api.services.slides.v1.model.DeleteTextRequest;
import com.google.api.services.slides.v1.model.InsertTextRequest;
import com.google.api.services.slides.v1.model.Range;
import com.google.api.services.slides.v1.model.TableCellLocation;
import com.google.api.services.slides.v1.model.UpdateParagraphStyleRequest;
import com.google.api.services.slides.v1.model.UpdateTableBorderPropertiesRequest;
import com.google.api.services.slides.v1.model.UpdateTableCellPropertiesRequest;
import com.google.api.services.slides.v1.model.UpdateTextStyleRequest;

public class TextRunPosition {
	
	@Override
	public String toString() {
		return "TextRunPosition [shapeId=" + shapeId + ", column=" + column + ", row=" + row + ", range=" + range + "]";
	}

	public static class Compare implements Comparator<TextRunPosition> {

		@Override
		public int compare(TextRunPosition o1, TextRunPosition o2) {
			if (o1.getPageId().orElse("PAGE").compareTo(o2.getPageId().orElse("PAGE")) != 0) 
				return o1.getPageId().orElse("PAGE").compareTo(o2.getPageId().orElse("PAGE"));
			if (o1.getShapeId().orElse("BODY").compareTo(o2.getShapeId().orElse("BODY")) != 0) 
				return o1.getShapeId().orElse("BODY").compareTo(o2.getShapeId().orElse("BODY"));
			if (o1.getTableStart().orElse(0).compareTo(o2.getTableStart().orElse(0)) != 0) 
				return o1.getTableStart().orElse(0).compareTo(o2.getTableStart().orElse(0));
			if (o1.getRow().orElse(0).compareTo(o2.getRow().orElse(0)) != 0)
				return o1.getRow().orElse(0).compareTo(o2.getRow().orElse(0));
			if (o1.getColumn().orElse(0).compareTo(o2.getColumn().orElse(0)) != 0)
				return o1.getColumn().orElse(0).compareTo(o2.getColumn().orElse(0));
			return (o1.getStart().compareTo(o2.getStart()));
		}
	}
	
	String pageId; 
	String shapeId; 
	Optional<Integer> tableStart;
	Optional<Integer> column;
	Optional<Integer> row;
	Range range;

	public Optional<String> getPageId() {
		return Optional.ofNullable(pageId);
	}

	
	public Optional<String> getShapeId() {
		return Optional.ofNullable(shapeId);
	}

	public Optional<Integer> getTableStart() {
		return tableStart.or(() -> Optional.of(range.getStartIndex()));
	}
	
	public Optional<Integer> getColumn() {
		return column;
	}

	public Optional<Integer> getRow() {
		return row;
	}

	public Range getRange() {
		return range;
	}
	
	public com.google.api.services.docs.v1.model.Range getDocsRange() {
		com.google.api.services.docs.v1.model.Range tmp = new com.google.api.services.docs.v1.model.Range()
				.setStartIndex(getStart())
				.setEndIndex(getEnd());
		if (shapeId != null && shapeId != "BODY") {
			tmp.setSegmentId(shapeId);
		}
		return tmp;
	}

	public TextRunPosition offset(int start) {
		return offset(start,start);
	}
	
	
	public TextRunPosition offset(int start, int end) {
		return of(this.pageId, this.shapeId, this.column, this.row, this.getStart()+start, this.getEnd()+end, this.tableStart );
	}
	
	public TextRunPosition offsetStart(int start, int end) {
		return of(this.pageId, this.shapeId, this.column, this.row, this.getStart()+start, this.getStart()+end, this.tableStart );
	}
	
	public static Optional<TextRunPosition> spanning(List<TextRunPosition> positions) {
		if (positions.size() == 0) return Optional.empty();
		Optional<String> pageId = positions.get(0).getPageId();
		Optional<String> id = positions.get(0).getShapeId();
		Integer start = positions.stream().mapToInt(p -> p.getStart()).min().orElse(0);
		Integer end = positions.stream().mapToInt(p -> p.getEnd()).max().orElse(start);
		return Optional.ofNullable(of(pageId.orElse(null), id.orElse(null),start,end));
	}
	
	public static TextRunPosition of(String pageId, String shapeId, Optional<Integer> column, Optional<Integer> row, Optional<Integer> position) {
		return
			of(pageId, shapeId,column,row,
					position
						.map(p-> new Range().setStartIndex(p).setEndIndex(p+1).setType("FIXED_RANGE"))
						.orElse(new Range().setType("ALL")));
	}
	
	public static TextRunPosition of(String pageId,String shapeId, Optional<Integer> column, Optional<Integer> row, Range range) {
		return of(pageId,shapeId, column, row, range, Optional.empty());
	}
	
	public static TextRunPosition of(String pageId,String shapeId, Optional<Integer> column, Optional<Integer> row, Range range, Optional<Integer> tableStart) {
		TextRunPosition out = new TextRunPosition();
		out.pageId = pageId;
		out.shapeId = shapeId;
		out.column = column;
		out.row = row;
		out.range = range;
		out.tableStart = tableStart;
		return out;
	}
	
	public static TextRunPosition of(String pageId, String shapeId, Optional<Integer> column, Optional<Integer> row, Integer start, Integer end, Optional<Integer> tableStart) {
		return of(pageId, shapeId, column, row, new Range().setStartIndex(start).setEndIndex(end).setType("FIXED_RANGE"), tableStart);
	}
	
	public static TextRunPosition of(String pageId, String shapeId, Range range) {
		return of(pageId, shapeId, Optional.empty(),Optional.empty(), range);
	}
	
	public static TextRunPosition of(String pageId, String shapeId, Integer start, Integer end) {
		return of(pageId, shapeId, Optional.empty(),Optional.empty(), new Range().setStartIndex(start).setEndIndex(end).setType("FIXED_RANGE"));
	}
	
	public static TextRunPosition of(String pageId, String shapeId) {
		return of(pageId, shapeId, Optional.empty(),Optional.empty(), new Range().setStartIndex(0).setEndIndex(0).setType("FIXED_RANGE"));
	}
	
	public static TextRunPosition of(Integer start, Integer end) {
		return of(null, null, Optional.empty(),Optional.empty(), new Range().setStartIndex(start).setEndIndex(end).setType("FIXED_RANGE"));
	}
	
	public static TextRunPosition of(com.google.api.services.docs.v1.model.Range range) {
		return of(null, range.getSegmentId(), range.getStartIndex(), range.getEndIndex());
	}
	
	public UpdateTextStyleRequest setPosition(UpdateTextStyleRequest req) {
		req.setObjectId(shapeId);
		if (row.isPresent() && column.isPresent()) {
			req.setCellLocation(new TableCellLocation().setColumnIndex(column.get()).setRowIndex(row.get()));
		}
		req.setTextRange(range);
		return req;
	}
	
	public InsertTextRequest setPosition(InsertTextRequest req) {
		req.setObjectId(shapeId);
		if (row.isPresent() && column.isPresent()) {
			req.setCellLocation(new TableCellLocation().setColumnIndex(column.get()).setRowIndex(row.get()));
		}
		req.setInsertionIndex(range.getStartIndex());
		return req;
	}
	
	public DeleteTextRequest setPosition(DeleteTextRequest req) {
		req.setObjectId(shapeId);
		if (row.isPresent() && column.isPresent()) {
			req.setCellLocation(new TableCellLocation().setColumnIndex(column.get()).setRowIndex(row.get()));
		}
		req.setTextRange(range);
		return req;
	}
	
	public CreateParagraphBulletsRequest setPosition(CreateParagraphBulletsRequest req) {
		req.setObjectId(shapeId);
		if (row.isPresent() && column.isPresent()) {
			req.setCellLocation(new TableCellLocation().setColumnIndex(column.get()).setRowIndex(row.get()));
		}
		req.setTextRange(range);
		return req;
	}
	
	public UpdateParagraphStyleRequest setPosition(UpdateParagraphStyleRequest req) {
		req.setObjectId(shapeId);
		if (row.isPresent() && column.isPresent()) {
			req.setCellLocation(new TableCellLocation().setColumnIndex(column.get()).setRowIndex(row.get()));
		}
		req.setTextRange(range);
		return req;
	}
	
	public UpdateTableCellPropertiesRequest setPosition(UpdateTableCellPropertiesRequest req) {
		req.setObjectId(shapeId);
		// req.setTableRange(null)
		return req;
	}
	
	public UpdateTableBorderPropertiesRequest setPosition(UpdateTableBorderPropertiesRequest req) {
		req.setObjectId(shapeId);
		return req;
	}
	
	public com.google.api.services.docs.v1.model.UpdateParagraphStyleRequest setPosition(
			com.google.api.services.docs.v1.model.UpdateParagraphStyleRequest req) {
		req.setRange(getDocsRange());
		return req;
	}

	@Override
	public int hashCode() {
		return Objects.hash(column, range, row, shapeId);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TextRunPosition other = (TextRunPosition) obj;
		return Objects.equals(column, other.column) && 
				(Objects.equals(range,  other.range) ||
						Objects.equals(range.getStartIndex(), other.range.getStartIndex()) &&
						Objects.equals(range.getEndIndex(), other.range.getEndIndex())
				) &&
				Objects.equals(row, other.row) && Objects.equals(shapeId, other.shapeId);
	}

	public boolean isEmpty() {
		if (this.range.getType() == "ALL") return false;
		if (
				this.range.getStartIndex() != null &&
				this.range.getEndIndex() != null &&
				this.range.getStartIndex() >= this.range.getEndIndex()
			) return true;
		if (this.range.getEndIndex() != null && this.range.getEndIndex() == 0) return true;
		return false;
	}

	public Integer getStart() {
		if (range == null) return 0;
		if (range.getStartIndex() == null) return 0;
		return range.getStartIndex();
	}
	
	public Integer getEnd() {
		if (range == null) return 0;
		if (range.getEndIndex() == null) return getStart();
		return range.getEndIndex();
	}

	public com.google.api.services.docs.v1.model.TableCellLocation getTableCellLocation() {
		Location loc = new Location()
				.setIndex(this.getTableStart().get());
		if (getShapeId().isPresent()) {
			loc.setSegmentId(this.getShapeId().get());
		};
		return new com.google.api.services.docs.v1.model.TableCellLocation()
				.setRowIndex(this.getRow().get())
				.setColumnIndex(this.getColumn().get())
				.setTableStartLocation(loc);
	}
	
	public Location getTableStartLocation() {
		Location loc = new Location()
				.setIndex(this.getTableStart().orElse(getStart()));
		return(loc);
	}

	

	



	
	
}