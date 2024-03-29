package org.github.terminological.roogledocs.datatypes;

import uk.co.terminological.rjava.types.RCharacter;
import uk.co.terminological.rjava.types.RInteger;
import uk.co.terminological.rjava.types.RNumeric;

public interface LongFormatTable extends TextFormat {

	public RInteger row();
	public RInteger col();
	public RCharacter label();
	public RInteger rowSpan();
	public RInteger colSpan();
	public RCharacter fillColour();
	public RNumeric leftBorderWeight();
	public RNumeric rightBorderWeight();
	public RNumeric topBorderWeight();
	public RNumeric bottomBorderWeight();
	//TODO: border style and colour;
	public RCharacter alignment();
	public RCharacter valignment();
	public RNumeric leftPadding();
	public RNumeric rightPadding();
	public RNumeric topPadding();
	public RNumeric bottomPadding();
 }
