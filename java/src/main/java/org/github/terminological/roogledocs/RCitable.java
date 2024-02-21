package org.github.terminological.roogledocs;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.github.terminological.roogledocs.datatypes.FO2Text;
import org.jbibtex.BibTeXDatabase;
import org.jbibtex.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import de.undercouch.citeproc.CSL;
import de.undercouch.citeproc.bibtex.BibTeXConverter;
import de.undercouch.citeproc.bibtex.BibTeXItemDataProvider;

public abstract class RCitable {

	public RCitable() {
		super();
	}
	
	private Logger log = LoggerFactory.getLogger(RCitable.class);

	private Stream<String> citeIds(String tag) {
		return Stream.of(tag.replace("cite:", "").split(";")).map(s -> StringUtils.strip(s,"?"));
	}

	private String[] citeIdArray(String tag) {
		return citeIds(tag).collect(Collectors.toList()).toArray(new String[0]);
	}

	public void updateCitations(String bibTex, String citationStyle) throws IOException, ParseException {
		
		// Load the bibtex and setup the parser.
		BibTeXDatabase db = new BibTeXConverter().loadDatabase(new ByteArrayInputStream(bibTex.getBytes(StandardCharsets.UTF_8)));
		BibTeXItemDataProvider provider = new BibTeXItemDataProvider();
		provider.addDatabase(db);
		List<String> bibtexIds = new ArrayList<>();
		provider.getIds().stream().forEach(l -> bibtexIds.add(l));
				
		CSL citeproc = new CSL(provider, citationStyle);
		//citeproc.setOutputFormat("text");
		
		
		// Get the tags in the document 
		Map<String, List<TextRunPosition>> allDocumentTags = this.updateInlineTags();
		
		// populate citeKeyOrder with the first occurrence of each citation in the document and the start range.
		// TODO: this needs looking at again - probably does not do what is expected in Slides.
		
		Map<String, Integer> citeKeyOrder = new HashMap<>();  
		allDocumentTags.entrySet().stream()
			.filter(e -> e.getKey().startsWith("cite:"))
			.forEach(e -> {
				citeIds(e.getKey())
					.forEach(s -> {
						int firstOccurs = e.getValue().stream().mapToInt(t->t.getStart()).min().orElse(0); 
						// the first index each citation appears int the document
						if (!citeKeyOrder.containsKey(s) || citeKeyOrder.get(s) > firstOccurs) {
							// ensures the value for each citation is the smallest:
							citeKeyOrder.put(s, firstOccurs);
						}
					});
			});
		// sort tmp2 by the value and extract the citation ids. This is the list in which they appear in the 
		// document.
		List<Entry<String, Integer>> sortedKeys = new ArrayList<>(citeKeyOrder.entrySet());
	    sortedKeys.sort(Entry.comparingByValue());
	    
	    // register the keys in appearance order.
	    // once this is done the order we process the keys doesn;t matter.
	    List<String> matched = new ArrayList<>();
	    List<String> notMatched = new ArrayList<>();
	    sortedKeys.stream()
	    		.map(k -> k.getKey())
	    		.forEach(s -> {
	    			if (bibtexIds.contains(s)) {
	    				matched.add(s);
	    			} else {
	    				notMatched.add(s);
	    			};
	    		});
	    citeproc.registerCitationItems(matched);
		
	    // maps tags e.g. {{cite:challen2013;danon2014} to "[1],[2]" display string replacement.
		Map<String,String> tagToCite = new HashMap<>();
		allDocumentTags.keySet().stream()
			.filter(s -> s.startsWith("cite:"))
			.forEach(s -> {
				String[] citeIds = citeIdArray(s);
				//all the citeIds for this specific cite tag should be in the bibtex if we are to proceed with the matching.
				if (Stream.of(citeIds).allMatch(c -> bibtexIds.contains(c))) {
					String citeString = citeproc.makeCitation(citeIds).stream().map(c -> c.getText()).collect(Collectors.joining(","));
					tagToCite.put(s, citeString);
				} else {
					// Not all of the ids were matched. Some might have been though and if 
					// so then they will have been registered and will appear in the bibliography.
					// We can work out which ones and highlight them replacing the text with a 
					String debugString = "{{cite:"+
							Stream.of(citeIds).map(c -> {
								if (!bibtexIds.contains(c)) {return "?"+c+"?";} else {return(c);}
							}).collect(Collectors.joining(";"))
							+"}}";
					tagToCite.put(s, debugString);
				}
			});
		
		
		// find and delete any tag of the format {{bib:[0-9*]}}
		// use position of the first as place to enter bibliography
		
				
		this.updateTaggedText(tagToCite);
		// List<String> bibs = Arrays.asList(citeproc.makeBibliography().getEntries());
		
		citeproc.setOutputFormat("fo");
		String html = citeproc.makeBibliography().makeString();
		List<String> bibs;
		try {
			bibs = FO2Text.convert(html);
		} catch (SAXException | IOException | ParserConfigurationException e1) {
			e1.printStackTrace();
			throw new RuntimeException("Encountered a problem parsing CSL output.");
		}
		
		this.insertReferences(bibs);
		
		if (notMatched.size() > 0) {
			log.info("Unmatched citation keys: ");
			log.info(notMatched.stream().collect(Collectors.joining("; ")));
			log.info("Available keys: ");
			log.info(bibtexIds.stream().collect(Collectors.joining("; ")));
		}
		// citeproc.close();
	}
	
	// https://www.digitalocean.com/community/tutorials/java-sax-parser-example
	//
	
	protected abstract void insertReferences(List<String> bibs) throws IOException;
	protected abstract void updateTaggedText(Map<String, String> tagToCite) throws IOException;
	protected abstract Map<String, List<TextRunPosition>> updateInlineTags() throws IOException;

}