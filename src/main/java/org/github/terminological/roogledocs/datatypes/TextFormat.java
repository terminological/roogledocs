package org.github.terminological.roogledocs.datatypes;

import uk.co.terminological.rjava.types.RCharacter;
import uk.co.terminological.rjava.types.RNumeric;

public interface TextFormat {

	
	RCharacter fontName();
	RCharacter fontFace();
	RNumeric fontSize();

}