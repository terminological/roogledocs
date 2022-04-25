package org.github.terminological.roogledocs;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
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
import uk.co.terminological.rjava.types.RCharacterVector;
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
			singleton = new RService(TOKENDIR.toString());
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
		Document doc = test1.getDoc(RDocument.IMAGE_POSITIONS);
		System.out.print(doc.toPrettyString());
	}
	
	@Test
	final void testRApi() throws IOException, GeneralSecurityException {
		RoogleDocs rd = new RoogleDocs(TOKENDIR.toString());
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
				.withCol("fontFace", RVector.with("bold","italic","bold.italic","plain","plain"))
				.withCol("fontName", RVector.with("Roboto","Arial","Courier New","Times New Roman","Pacifico"))
				.withCol("fontSize", RVector.with(8D,8D,12D,20D,6D))
				.withCol("alignment", RVector.with("START","CENTER","START","CENTER","END"))
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
		test2.updateOrInsertInlineImage(1, URI.create("https://www.pngall.com/wp-content/uploads/5/Black-Dog-PNG.png"), 4.0, 4.0);
		Document doc = test2.getDoc();
		System.out.print(doc.toPrettyString());
	}
}
