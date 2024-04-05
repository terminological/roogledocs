package org.github.terminological.roogledocs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.commons.text.StringEscapeUtils;
import org.github.terminological.roogledocs.datatypes.Tuple;
import org.github.terminological.roogledocs.datatypes.TupleList;
import org.junit.jupiter.api.Test;




class TestRegex {

	private static Stream<Tuple<String,String>> tests() {
		return TupleList
				.with("<b>BOLD</b>","BOLD")
				.and("<i><b>BOLDITALIC</b></i>", "BOLDITALIC")
				.and("<i><b>BOLD&nbsp;&amp;&nbsp;ITALIC</b></i>", "BOLD & ITALIC")
				.and("<i><p>BOLD&nbsp;&amp;&nbsp;ITALIC</p></i>", "<p>BOLD & ITALIC</p>")
				.stream();
	}
	
	@Test
	void test() {
		tests().forEach(s -> {
			String tmp = s.getFirst();
			Pattern p = Pattern.compile("^<(sup|sub|b|i|u)>(.*)</(sup|sub|b|i|u)>$");
			while (tmp.startsWith("<")) {
							
				Matcher m = p.matcher(tmp);
				if (m.find()) {
					System.out.println(m.group(1));
					tmp = m.group(2);
				} else {
					// unsupported format for regex
					break;
				}
				
				
			}
			tmp = StringEscapeUtils.unescapeHtml4(tmp);
			System.out.println(tmp);
			assertEquals(tmp, s.getSecond());
			
		});
	}

}
