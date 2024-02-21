package org.github.terminological.roogledocs.datatypes;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class FO2Text extends DefaultHandler {

	private StringBuilder data = null;
	private List<String> output = new ArrayList<>();
	private int block = 0;
	private boolean inCell = false;
	private boolean inBlock = false;

	public FO2Text() {
		data = new StringBuilder();
	}
	
	public List<String> getOutput() {
		return output;
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		
		if (qName.equals("fo:table-row")) {
			block = 0;
			data = new StringBuilder();
		}
		else if (qName.equals("fo:block")) {
			if (inCell) {
				inBlock = true;
			}
			if (block>0) data.append("\t");
		}
		else if (qName.equals("fo:table-cell")) {
			inCell = true;
				
		}
		
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		
		if (qName.equals("fo:table-row")) {
			output.add(data.toString()+"\n");
		}
		else if (qName.equals("fo:block")) {
			inBlock = false;
		}
		else if (qName.equals("fo:table-cell")) {
			inCell = false;
			block = block+1;
			
		} 
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (inCell && inBlock) data.append(new String(ch, start, length));
	}
	
	public static List<String> convert(String html) throws SAXException, IOException, ParserConfigurationException {
		html = "<root>"+html+"</root>";
	    SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
	    // saxParserFactory.setExpandEntityReferences(false);
	    SAXParser saxParser = saxParserFactory.newSAXParser();
	    //XMLReader reader = saxParser.getXMLReader();
	    //reader.setFeature("http://apache.org/xml/features/dom/create-entity-ref-nodes", false);
	    FO2Text handler = new FO2Text();
	    saxParser.parse(new ByteArrayInputStream(html.getBytes()), handler);
	    
	    return handler.getOutput();
	} 
}
