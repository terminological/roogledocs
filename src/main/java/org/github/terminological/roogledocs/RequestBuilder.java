package org.github.terminological.roogledocs;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.services.docs.v1.model.BatchUpdateDocumentRequest;
import com.google.api.services.docs.v1.model.CreateNamedRangeRequest;
import com.google.api.services.docs.v1.model.DeleteContentRangeRequest;
import com.google.api.services.docs.v1.model.EndOfSegmentLocation;
import com.google.api.services.docs.v1.model.InsertInlineImageRequest;
import com.google.api.services.docs.v1.model.InsertTextRequest;
import com.google.api.services.docs.v1.model.Location;
import com.google.api.services.docs.v1.model.Range;
import com.google.api.services.docs.v1.model.Request;
import com.google.api.services.docs.v1.model.Size;
import com.google.api.services.docs.v1.model.TextStyle;
import com.google.api.services.docs.v1.model.UpdateTextStyleRequest;
import com.google.api.services.docs.v1.model.WriteControl;



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
}
