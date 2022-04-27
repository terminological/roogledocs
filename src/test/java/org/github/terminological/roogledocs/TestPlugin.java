package org.github.terminological.roogledocs;

import java.io.IOException;
//import java.nio.charset.StandardCharsets;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

public class TestPlugin {
	
//	@Test
//	public final void testFindExports() throws IOException {
//		Path rDir = Paths.get("/home/terminological/Git/roogledocs/r-library/R");
//		Files.walk(rDir)
//		.filter(f -> !Files.isDirectory(f))
//		.forEach(f -> {
//			try {
//				String s = new String(Files.readAllBytes(f), StandardCharsets.UTF_8);
//				//System.out.println(s);
//				Pattern p = Pattern.compile("@export.*?\\n\\s*([a-zA-Z0-9_\\.]+)\\s*=", Pattern.DOTALL);
//				Matcher m = p.matcher(s);
//				while(m.find()) {
//					// System.out.println(m.group());
//					System.out.println(m.group(1));
//				}
//			} catch (IOException e) {
//				throw new RuntimeException(e);
//			}
//		});
//	}
	
	@Test
	public final void testUrl() throws IOException {
		String s = "https://docs.google.com/document/d/1R8SuJI5uJwoMGBHGMaCdRH6i9R39DPQdcAdAF4BWZ20/edit?usp=sharing";
		System.out.println(RService.extractDocId(s));
	}
	
}
