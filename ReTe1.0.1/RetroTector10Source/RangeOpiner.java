/*
* Copyright (©) 2000-2006, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  G. Sperber
* @version 22/9 -06
* Beautified 22/9 -06
*/

package retrotector;

public interface RangeOpiner {

/**
*	@param	specification	See Range.SPECIFICATION.
* @return	Allowed range of something indicated by specification.
*/
	public Range rangeOpinion(int specification);
	
} // end of RangeOpiner
