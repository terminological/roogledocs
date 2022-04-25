package org.github.terminological.roogledocs.datatypes;

import uk.co.terminological.rjava.types.RCharacter;
import uk.co.terminological.rjava.types.RInteger;
import uk.co.terminological.rjava.types.RNumeric;

public interface LongFormatTable {

	public RInteger row();
	public RInteger col();
	public RCharacter label();
	public RInteger rowSpan();
	public RInteger colSpan();
	public RCharacter fontName();
	public RCharacter fontFace();
	public RNumeric fontSize();
	public RCharacter fillColour();
	public RNumeric leftBorderWeight();
	public RNumeric rightBorderWeight();
	public RNumeric topBorderWeight();
	public RNumeric bottomBorderWeight();
	// TODO: border style and colour;
	public RCharacter alignment();
	public RCharacter valignment();
	
 }
