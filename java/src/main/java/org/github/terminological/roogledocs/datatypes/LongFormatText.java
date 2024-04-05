package org.github.terminological.roogledocs.datatypes;

import uk.co.terminological.rjava.types.RCharacter;
import uk.co.terminological.rjava.types.RLogical;
import uk.co.terminological.rjava.types.RNumeric;

public interface LongFormatText extends TextFormat {

	public RCharacter label();
	public RCharacter link();
	
	public static LongFormatText of(String label, String fontFace, Double fontSize, String fontName, String link) {
//		return (of(label,fontFace,fontSize,fontName,link,false));
//	}
//	
//	public static LongFormatText of(String label, String fontFace, Double fontSize, String fontName, String link, Boolean supercript) {
		return new LongFormatText() {

			@Override
			public RCharacter fontName() {
				return RCharacter.from(fontName);
			}

			@Override
			public RCharacter fontFace() {
				return RCharacter.from(fontFace);
			}

			@Override
			public RNumeric fontSize() {
				return RNumeric.from(fontSize);
			}

			@Override
			public RCharacter label() {
				return RCharacter.from(label);
			}

			@Override
			public RCharacter link() {
				return RCharacter.from(link);
			}

//			@Override
//			public RLogical superscript() {
//				// TODO Auto-generated method stub
//				return null;
//			}
//
//			@Override
//			public RLogical subscript() {
//				// TODO Auto-generated method stub
//				return null;
//			}
//
//			@Override
//			public RLogical strikethrough() {
//				// TODO Auto-generated method stub
//				return null;
//			}
//
//			@Override
//			public RLogical smallCaps() {
//				// TODO Auto-generated method stub
//				return null;
//			}
//
//			@Override
//			public RCharacter textColor() {
//				// TODO Auto-generated method stub
//				return null;
//			}
			
		};
	}
}
