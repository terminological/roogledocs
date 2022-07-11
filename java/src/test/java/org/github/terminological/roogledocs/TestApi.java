package org.github.terminological.roogledocs;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.SystemUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.DefaultConfiguration;
import org.github.terminological.roogledocs.datatypes.Tuple;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.services.docs.v1.model.Document;
import com.google.api.services.docs.v1.model.Size;

import uk.co.terminological.rjava.UnconvertableTypeException;
import uk.co.terminological.rjava.types.RDataframe;
import uk.co.terminological.rjava.types.RNumeric;
import uk.co.terminological.rjava.types.RVector;

class TestApi {

	static final Path TOKENDIR = Paths.get(SystemUtils.getUserHome().getPath(),".roogledocs");
	
	RService singleton = null;
	RDocument testDoc = null;
	String testImageId = null;
	private Logger log = LoggerFactory.getLogger(TestApi.class);
	
	@BeforeAll
	public static void setUpClass() {
		Configurator.initialize(new DefaultConfiguration());
	    Configurator.setRootLevel(Level.ALL);
	}
	
	@BeforeEach
	void setUp() throws Exception {
		if (singleton == null) {
			singleton = RService.with(TOKENDIR.toString());
		}
	}

	@Test
	final void testFindOrCreate() throws IOException {
		if (testDoc == null)
			testDoc = singleton.getOrCreate("Test document");
		 
	}

	@Test
	final void testUploadAndShare() throws IOException, URISyntaxException {
		RDocument test2 = singleton.getOrCreate("Roogledocs example 2");
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss");
		LocalDateTime now = LocalDateTime.now();
		String tmp = dtf.format(now);  
		if (testImageId == null) {
			URI file = TestApi.class.getResource("/logo.png").toURI();
			testImageId = singleton.upload("test logo "+tmp,Paths.get(file));
		}
		URI uri = singleton.getThumbnailUri(testImageId);
		test2.updateTaggedImage("image-here", uri, 4.0,4.0);
		test2.updateTaggedText("image-update", tmp);
	}
	
	@Test
	final void testDelete() throws IOException, URISyntaxException {
		//testUploadAndShare();
		List<Tuple<String, String>> tmp = singleton.search("test logo", RService.MIME_PNG);
		for (Tuple<String, String> tmp2 : tmp) {
			log.debug("id: "+tmp2.getKey()+" file: "+tmp2.getSecond());
			singleton.delete(tmp2.getKey());
		}
	}
	
	@Test
	final void testRegex() {
		String e = "some random text with a {{tagged 1 2 3}} in it. and another {{tagged 3 4 5}} also";
		Pattern p = Pattern.compile("\\{\\{tagged([^\\}]*)\\}\\}");
		Matcher m = p.matcher(e);
		while(m.find()) {
			System.out.println(m.start()+" "+m.end()+" "+e.substring(m.start(),m.end()));
		}
	}
	
	@Test
	final void testRegex2() {
		String e = "https://docs.google.com/document/d/1woDbkXAkf6RbvtjGlXMPBOl7zvDCviAvAWDZGNerYCk/edit?usp=sharing";
		Pattern p = Pattern.compile("/([^/]+)/[^/]+$");
		Matcher m = p.matcher(e);
		while(m.find()) {
			System.out.println(m.start(1)+" "+m.end(1)+" "+e.substring(m.start(1),m.end(1)));
		}
	}
	
	
	
	@Test
	final void testStructure() throws IOException {
		RDocument test1 = singleton.getOrCreate("Roogledocs example 1");
		System.out.print(test1.updateInlineTags());
		Document doc = test1.getDoc(RDocument.MINIMAL);
		System.out.print(doc.toPrettyString());
	}
	
	@Test
	final void testRApi() throws IOException, GeneralSecurityException {
		RoogleDocs rd = new RoogleDocs(TOKENDIR.toString(),false);
		rd.findOrCreateDocument("Roogledocs example 1");
		System.out.print(rd.tagsDefined());
		
	}
	
	@Test
	final void testText() throws IOException {
		RDocument test2 = singleton.getOrCreate("Roogledocs example 2");
		test2.updateTaggedText("text-tag-1", "123REPLACEMENT321");
		Document doc = test2.getDoc();
		System.out.print(doc.toPrettyString());
	}
	
	@Test
	final void testImage() throws IOException {
		RDocument test2 = singleton.getOrCreate("Roogledocs example 2");
		test2.updateTaggedImage("image-here", URI.create("https://www.pngall.com/wp-content/uploads/5/Black-Dog-PNG.png"));
		Document doc = test2.getDoc();
		System.out.print(doc.toPrettyString());
	}
	
	@Test
	final void testImage2() throws IOException {
		RDocument test2 = singleton.getOrCreate("Roogledocs example 2");
		test2.updateTaggedImage("image-here", URI.create("https://drive.google.com/uc?id=1C9fUcM6vLWxHLWFc3_8zCJW6jSTwSwfu&export=download"), 4.0,4.0);
		Document doc = test2.getDoc();
		System.out.print(doc.toPrettyString());
	}
	
	@Test
	final void testImageSize() throws IOException {
		RDocument test2 = singleton.getOrCreate("Roogledocs example 2");
		Document doc = test2.getDoc();
		Optional<Size> size = DocumentHelper.imageSize(doc, "image-here");
		System.out.print(size);	
	}
	
	@Test
	final void testImageSize2() throws IOException {
		Size size = RoogleDocs.getImageDim(
				"/home/terminological/Git/bristol-cases/output/icu-subgroups/fig1-data-flow-2022-04-26.png", 300);
		System.out.print(size);	
	}
	
	@Test
	final void testRevert() throws IOException {
		RDocument test2 = singleton.getOrCreate("Roogledocs example 2");
		test2.revertTags();
		Document doc = test2.getDoc();
		System.out.print(doc.toPrettyString());
	}
	
	@Test
	final void testTableInsert() throws IOException, UnconvertableTypeException {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss");
		LocalDateTime now = LocalDateTime.now();
		String tmp = dtf.format(now); 
		RDataframe df = RDataframe.create()
				.withCol("label", RVector.with("A1",tmp,"B1","B2","B3"))
				.withCol("col", RVector.with(1,2,1,2,3))
				.withCol("row", RVector.with(1,1,2,2,2))
				.withCol("colSpan", RVector.with(1,2,1,1,1))
				.withCol("rowSpan", RVector.with(1,1,1,1,1))
				.withCol("fontFace", RVector.with("bold","italic","bold.italic",null,"plain"))
				.withCol("fontName", RVector.with("Roboto","Arial","Courier New","Times New Roman","Pacifico"))
				.withCol("fontSize", RVector.with(8D,8D,12D,20D,6D))
				.withCol("alignment", RVector.with("START","CENTER",null,"CENTER","END"))
				.withCol("valignment", RVector.with("TOP","MIDDLE","BOTTOM","MIDDLE","TOP"))
				.withCol("bottomBorderWeight", RVector.with(0D,0D,1D,1D,1D))
				.withCol("topBorderWeight", RVector.with(1D,1D,0D,0D,0D))
				.withCol("leftBorderWeight", RVector.with(1D,0D,1D,0D,0D))
				.withCol("rightBorderWeight", RVector.with(0D,1D,0D,0D,1D))
				.withCol("fillColour", RVector.with("#FFFFFF","#DDFFFF","#FFDDFF","#FFFFDD","#DDDDDD"));
		
		System.out.println(df.asCsv());
		RDocument test2 = singleton.getOrCreate("Roogledocs example 2");
		
		test2.updateOrInsertTable(1, df, RVector.with(2D,4D,3D), RNumeric.from(4D));				
	}
	
	@Test
	final void testImageInsert() throws IOException {
		RDocument test2 = singleton.getOrCreate("Roogledocs example 2");
		test2.updateOrInsertInlineImage(1, URI.create("https://www.pngall.com/wp-content/uploads/5/Black-Dog-PNG.png"), 3.0, 3.0);
		Document doc = test2.getDoc();
		System.out.print(doc.toPrettyString());
	}
	
	@Test
	final void testClone() throws IOException {
		RDocument d = singleton.getOrClone("roogledocs-demo", "https://docs.google.com/document/d/1R8SuJI5uJwoMGBHGMaCdRH6i9R39DPQdcAdAF4BWZ20/edit?usp=sharing");
		// d.saveAsPdf("/home/terminological/tmp/template.pdf");
	}
	
	@Test
	final void testTextInsert() throws IOException {
		RDocument test2 = singleton.getOrCreate("Roogledocs example 2");
		test2.appendText("First header2\nSecond header2", Optional.of("HEADING_2"));	
	}
	
	@Test
	final void testFormattedInsert() throws IOException, UnconvertableTypeException {
		
//			RDataframe df = RDataframe.create()
//				.withCol("label", RVector.with("Some bold"," and italic.\n","Bold and italic on new line in 12pt\n","NOW 20pt plain\n","\tand 6pt normal\t"))
//				.withCol("link", RVector.with("http://www.example.com",null,null,null,null))
//				.withCol("fontName", RVector.with("Courier New",null,null,null,null))
//				.withCol("fontFace", RVector.with("bold","italic","bold.italic",null,"underlined"))
//				.withCol("fontSize", RVector.with(8D,8D,12D,20D,6D))
//				//.withCol("style", RVector.with("NORMAL_TEXT",null,null,"HEADING_1","NORMAL_TEXT"))
//				;
//		
//		System.out.println(df.asCsv());
		RDocument test2 = singleton.getOrCreate("Roogledocs example 2");
		
		// test2.appendText(df);	
		
		
		RDataframe df2 = RDataframe.create()
				.withCol("label", RVector.with("Roogledocs", " is also able to add text at the end of the document with complex formatting. ", "Supporting fonts and formatting such as ", "bold, ", "italic ", "and underlined", " amongst other things."))
				.withCol("link", RVector.with("https://terminological.github.io/roogledocs/r-library/docs/", null, null, null, null, null, null))
				.withCol("fontName", RVector.with("Courier New", null, null, null, null, null, null))
				.withCol("fontFace", RVector.with("plain", "plain", "plain", "bold", "italic", "underlined", "plain"));

		test2.appendText(df2);
	}
}
