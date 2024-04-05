package org.github.terminological.roogledocs.datatypes;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class FO2LongFormat extends DefaultHandler {

	
	
	private StringBuilder data = null;
	private Stack<Boolean> isBold = new Stack<>();
	private Stack<Boolean> isItalic = new Stack<>();
	private Stack<Boolean> isUnderlined = new Stack<>();
	private Stack<Boolean> isSuperscript = new Stack<>();
	private List<LongFormatText> output = new ArrayList<>();
	private int block = 0;
	private boolean inCell = false;
	private boolean inBlock = false;

	public FO2LongFormat() {
		isItalic.push(Boolean.FALSE);
		isBold.push(Boolean.FALSE);
		isUnderlined.push(Boolean.FALSE);
		isSuperscript.push(Boolean.FALSE);
		data = new StringBuilder();
	}
	
	private String fontStyle() {
		return 	(isBold.peek() ? "bold" : "")+
				(isItalic.peek() ? "italic" : "")+
				(isUnderlined.peek() ? "underlined" : "");
	}
	
	private String n(String x) {
		return x == null ? "" : x;
	}
	
	private String style(Attributes attributes) {
		return n(attributes.getValue("font-style"));
	}
	
//	private String cls(Attributes attributes) {
//		return n(attributes.getValue("class"));
//	}
	
	public List<LongFormatText> getOutput() {
		return output;
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		
		if (qName.equals("fo:table-row")) {
			output.add(LongFormatText.of("\n", "", 8D, null, null));
			isItalic.push(Boolean.FALSE);
			isBold.push(Boolean.FALSE);
			isUnderlined.push(Boolean.FALSE);
			isSuperscript.push(Boolean.FALSE);
			block = 0;
		}
		else if (qName.equals("fo:block")) {
			if (inCell) {
				inBlock = true;
				data = new StringBuilder();
			}
		}
		else if (qName.equals("fo:table-cell")) {
			inCell = true;
			if (block != 0) output.add(LongFormatText.of("\t", "", 8D, null, null));
			
		} else if (qName.equals("fo:inline")) {
			
			output.add(LongFormatText.of(data.toString().trim(), fontStyle(), 8D, null, null));
			
			// output.add(LongFormatText.of(data.toString().trim(), fontStyle(), 8D, null, null, isSuperscript.peek()));
			data = new StringBuilder();
			
			if (style(attributes).contains("italic")) {
				isItalic.push(Boolean.TRUE);
			} else {
				isItalic.push(Boolean.FALSE);
			}
			
			if (style(attributes).contains("bold")) {
				isBold.push(Boolean.TRUE);
			} else {
				isBold.push(Boolean.FALSE);
			}
			
			if (style(attributes).contains("underline")) {
				isUnderlined.push(Boolean.TRUE);
			} else {
				isUnderlined.push(Boolean.FALSE);
			}
			
		} else if (qName.equals("sup")) {
			isSuperscript.push(Boolean.TRUE);
		}
		
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		
		if (qName.equals("fo:table-row")) {
			isItalic.pop();
			isBold.pop();
			isUnderlined.pop();
		}
		else if (qName.equals("fo:block")) {
			if (inCell && inBlock) output.add(LongFormatText.of(data.toString().trim(), fontStyle(), 8D, null, null));
			inBlock = false;
		}
		else if (qName.equals("fo:table-cell")) {
			inCell = false;
			block = block+1;
			
		} else if (qName.equals("fo:inline")) {
			
			output.add(LongFormatText.of(data.toString().trim(), fontStyle(), 8D, null, null));
			isItalic.pop();
			isBold.pop();
			isUnderlined.pop();
			
		}
		
		data = new StringBuilder();
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (inCell && inBlock) data.append(new String(ch, start, length));
	}
	
	public static List<LongFormatText> convert(String html) throws SAXException, IOException, ParserConfigurationException {
		html = "<root>"+html+"</root>";
	    SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
	    // saxParserFactory.setExpandEntityReferences(false);
	    SAXParser saxParser = saxParserFactory.newSAXParser();
	    //XMLReader reader = saxParser.getXMLReader();
	    //reader.setFeature("http://apache.org/xml/features/dom/create-entity-ref-nodes", false);
	    FO2LongFormat handler = new FO2LongFormat();
	    saxParser.parse(new ByteArrayInputStream(html.getBytes()), handler);
	    
	    return handler.getOutput();
	} 
}
