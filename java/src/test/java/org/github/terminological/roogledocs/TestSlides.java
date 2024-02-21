package org.github.terminological.roogledocs;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.SystemUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.DefaultConfiguration;
import org.github.terminological.roogledocs.datatypes.Tuple;
import org.jbibtex.ParseException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.services.slides.v1.model.Presentation;

import uk.co.terminological.rjava.UnconvertableTypeException;
import uk.co.terminological.rjava.types.RDataframe;
import uk.co.terminological.rjava.types.RNamedList;
import uk.co.terminological.rjava.types.RNumeric;
import uk.co.terminological.rjava.types.RVector;

class TestSlides {

	static final Path TOKENDIR = Paths.get(SystemUtils.getUserHome().getPath(),".roogledocs-test");
	
	RService singleton = null;
	RPresentation testDoc = null;
	String testImageId = null;
	// private Logger log = LoggerFactory.getLogger(TestSlides.class);
	
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
			testDoc = singleton.getOrCreatePresentation("Test slides");
		 
	}

	
	@Test
	final void testStructure() throws IOException {
		//RPresentation test1 = singleton.getOrCreatePresentation("roogleslides-demo");
		RPresentation test1 = singleton.getOrCreatePresentation("UoB Test");
		//Presentation doc = test1.getPresentation(RPresentation.FULL_LAYOUTS);
		Presentation doc = test1.getPresentation();
		System.out.print(doc.toPrettyString());
	}
	
	@Test
	final void testFindLinks() throws IOException {
		RPresentation test1 = singleton.getOrCreatePresentation("Structure test");
		System.out.print(test1.updateInlineTags());
	}
	
	@Test
	final void testUpdateText() throws IOException {
		RPresentation test1 = singleton.getOrCreatePresentation("Structure test");
		test1.updateTaggedText("date", LocalDate.now().toString());
		test1.setDefaultLayout("TITLE_AND_BODY");
		HashMap<String,String> toChange = new HashMap<>();
		toChange.put("text-tag-2", "1234-text-tag-2-4321");
		toChange.put("text-tag-3", "1234-text-tag-3-4321");
		toChange.put("text-tag-4", "1234-text-tag-4-4321");
		test1.updateTaggedText(toChange);
	}
	
	@Test
	final void testGetLayouts() throws IOException {
		RPresentation test1 = singleton.getOrCreatePresentation("roogleslides-demo");
		test1.getLayouts().forEach(System.out::println);
	}
	
	@Test
	final void testAppendSlide() throws IOException {
		RPresentation test1 = singleton.getOrCreatePresentation("Structure test");
		String layoutId = test1.getLayoutIdForName("TITLE_AND_BODY");
		TextRunPosition id = test1.appendSlide(layoutId, Optional.of("A new slide"));
		test1.setSlideBody(id, List.of("This","\tThat","The other"), Optional.of("BULLET_DISC_CIRCLE_SQUARE"), Optional.empty());
	}
	
	@Test
	final void testGetDimensions() throws IOException {
		RPresentation test1 = singleton.getOrClonePresentation("roogleslides-demo",
				"https://docs.google.com/presentation/d/18jqzzDI1zBruO3Rc0RlzX_rxhsDiOuKbTM4vnwuimn4/edit?usp=sharing");
		test1.getDimensions("TITLE_AND_BODY").forEach(System.out::println);
		System.out.println(test1.getDefaultLayoutName());
		Tuple<Double, Double> tmp = test1.getBodyDimensions();
		System.out.println( RNamedList
				.with("width", RNumeric.from(tmp.getFirst()))
				.and("height", RNumeric.from(tmp.getSecond()))
				.rCode());
	}
	
	@Test
	final void testDefaultBody() throws IOException {
		RPresentation test1 = singleton.getOrCreatePresentation("UoB Test");
		System.out.println(test1.getDefaultLayoutName());
		Tuple<Double, Double> tmp = test1.getBodyDimensions();
		System.out.println( RNamedList
				.with("width", RNumeric.from(tmp.getFirst()))
				.and("height", RNumeric.from(tmp.getSecond()))
				.rCode());
	}
	
	@Test
	final void testRemoveLinks() throws IOException {
		try {
			singleton.deleteByName("roogleslides-clone",RService.MIME_SLIDES);
		} catch (IOException e) {
			//probably wasn;t there
		}
		RPresentation p = singleton.getOrClonePresentation("roogleslides-clone", singleton.findUniqueId("roogleslides-demo", RService.MIME_SLIDES));
		p.removeTags();
	}
	
	
	@Test
	final void testAppendImage() throws IOException {
		RPresentation test2 = singleton.getOrCreatePresentation("UoB Test");
		String layoutId = test2.getDefaultLayoutId();
		TextRunPosition id = test2.appendSlide(layoutId, Optional.of("A new slide"));
		test2.setSlideBody(id, URI.create("https://upload.wikimedia.org/wikipedia/commons/7/77/Avatar_cat.png"), "cat-image", Optional.empty());
	}
	
		@Test
	final void testImage() throws IOException {
		RPresentation test2 = singleton.getOrCreatePresentation("Structure test");
		test2.updateTaggedImage("dog-picture", URI.create("https://www.pngall.com/wp-content/uploads/5/Black-Dog-PNG.png"));
	}
	
	@Test
	final void testImage2() throws IOException {
		RPresentation test2 = singleton.getOrCreatePresentation("Structure test");
		test2.updateTaggedImage("dog-picture", URI.create("https://upload.wikimedia.org/wikipedia/commons/7/77/Avatar_cat.png"));
	}
	
	
	@Test
	final void testImage3() throws IOException {
		RPresentation test2 = singleton.getOrCreatePresentation("roogleslides-demo");
		test2.updateTaggedImage(
				"landscape-image", 
				URI.create("https://upload.wikimedia.org/wikipedia/commons/7/7f/Khorinsk_View_Landscape.png"));
	}
	
	
	@Test
	final void testImage4() throws IOException {
		RPresentation test2 = singleton.getOrCreatePresentation("roogleslides-demo");
		test2.updateTaggedImage(
				"portrait-image", 
				URI.create("https://upload.wikimedia.org/wikipedia/commons/1/15/Betty_Grable_-_Studio_portrait_%281935%29.png"));
	}
	
	
//	@Test
//	final void testImageSize() throws IOException {
//		RDocument test2 = singleton.getOrCreateDocument("Roogledocs example 2");
//		Document doc = test2.getDoc();
//		Optional<Size> size = DocumentHelper.imageSize(doc, "image-here");
//		System.out.print(size);	
//	}
//	
//	@Test
//	final void testImageSize2() throws IOException {
//		Size size = RoogleDocs.getImageDim(
//				"/home/terminological/Git/bristol-cases/output/icu-subgroups/fig1-data-flow-2022-04-26.png", 300);
//		System.out.print(size);	
//	}
	
//	@Test
//	final void testRevert() throws IOException {
//		RDocument test2 = singleton.getOrCreateDocument("Roogledocs example 1");
//		test2.revertTags();
//		Document doc = test2.getDoc();
//		System.out.print(doc.toPrettyString());
//	}
	
	@Test
	final void testCitation() throws IOException, URISyntaxException, ParseException {
		RPresentation test2 = singleton.getOrClonePresentation("roogleslides-demo",
				"https://docs.google.com/presentation/d/18jqzzDI1zBruO3Rc0RlzX_rxhsDiOuKbTM4vnwuimn4/edit?usp=sharing");
		String bibtex = Files.readString(Paths.get(TestApi.class.getResource("/test.bib").toURI()));
		test2.updateCitations(bibtex, "ieee");
		
	}
	
	@Test
	final void testTaggedTableInsert() throws IOException, UnconvertableTypeException {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss");
		LocalDateTime now = LocalDateTime.now();
		String tmp = dtf.format(now); 
		RDataframe df = RDataframe.create()
				.withCol("label", RVector.with("TAGGED2",tmp,"B1","B2","B3"))
				.withCol("col", RVector.with(1,2,1,2,3))
				.withCol("row", RVector.with(1,1,2,2,2))
				.withCol("colSpan", RVector.with(1,2,1,1,1))
				.withCol("rowSpan", RVector.with(1,1,1,1,1))
				.withCol("fontFace", RVector.with("bold","italic","bold.italic",null,"plain"))
				.withCol("fontName", RVector.with("Roboto","Arial","Courier New","Times New Roman","Pacifico"))
				.withCol("fontSize", RVector.with(8D,8D,12D,20D,6D))
				.withCol("alignment", RVector.with("START","CENTER",null,"CENTER","END"))
				.withCol("valignment", RVector.with("TOP","MIDDLE","BOTTOM","MIDDLE","TOP"))
				.withCol("bottomBorderWeight", RVector.with(0D,0D,4D,4D,4D))
				.withCol("topBorderWeight", RVector.with(4D,4D,0D,0D,0D))
				.withCol("leftBorderWeight", RVector.with(1D,0D,1D,0D,0D))
				.withCol("rightBorderWeight", RVector.with(0D,1D,0D,0D,1D))
				.withCol("fillColour", RVector.with("#FFFFFF","#DDFFFF","#FFDDFF","#FFFFDD","#DDDDDD"));
		
		System.out.println(df.asCsv());
		RPresentation test2 = singleton.getOrCreatePresentation("roogleslides-demo");
		
		test2.updateTaggedTable("tag-table", df, RVector.with(2D,4D,3D));				
	}
	
	
	
//	@Test
//	final void testImageInsert() throws IOException {
//		RDocument test2 = singleton.getOrCreateDocument("Roogledocs example 2");
//		test2.updateOrInsertInlineImage(1, URI.create("https://www.pngall.com/wp-content/uploads/5/Black-Dog-PNG.png"), 3.0, 3.0);
//		Document doc = test2.getDoc();
//		System.out.print(doc.toPrettyString());
//	}
//	
//	@Test
//	final void testClone() throws IOException {
//		//RDocument d = 
//		singleton.getOrCloneDocument("roogledocs-clone", "https://docs.google.com/document/d/1XnrBgBJFz7jEMYtw3o3YKbOuMdWvUkzIul4hb2B-SC4/edit?usp=sharing");
//		// d.saveAsPdf("/home/terminological/tmp/template.pdf");
//	}
//	
//	@Test
//	final void testTextInsert() throws IOException {
//		RDocument test2 = singleton.getOrCreateDocument("Roogledocs example 2");
//		test2.appendText("First header2\nSecond header2", Optional.of("HEADING_2"));	
//	}
//	
//	@Test
//	final void testFormattedInsert() throws IOException, UnconvertableTypeException {
		
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
//		RDocument test2 = singleton.getOrCreateDocument("Roogledocs example 2");
//		
//		// test2.appendText(df);	
//		
//		
//		RDataframe df2 = RDataframe.create()
//				.withCol("label", RVector.with("Roogledocs", " is also able to add text at the end of the document with complex formatting. ", "Supporting fonts and formatting such as ", "bold, ", "italic ", "and underlined", " amongst other things."))
//				.withCol("link", RVector.with("https://terminological.github.io/roogledocs/r-library/docs/", null, null, null, null, null, null))
//				.withCol("fontName", RVector.with("Courier New", null, null, null, null, null, null))
//				.withCol("fontFace", RVector.with("plain", "plain", "plain", "bold", "italic", "underlined", "plain"));
//
//		test2.appendText(df2);
//	}
	

	
	
}
