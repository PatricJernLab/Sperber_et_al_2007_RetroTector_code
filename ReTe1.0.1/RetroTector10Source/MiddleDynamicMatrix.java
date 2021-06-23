/*
* Copyright (©) 2000-2006, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  G. Sperber
* @version 21/9 -06
* Beautified 21/9 -06
*/

package retrotector;

import retrotectorcore.*;
import builtins.*;

/**
* Interface to CoreMiddleDynamicMatrix.
*/
public class MiddleDynamicMatrix {
	  	
	private CoreMiddleDynamicMatrix coreMiddleDynamicMatrix;

/**
* Constructor.
* @param	rect	DNA (x-direction) and master (y-direction) ranges to include.
* @param	block	Collection of useful parameters.
* @param	put		Putein to receive result.
*/
	public MiddleDynamicMatrix(Utilities.Rectangle rect, ORFID.ParameterBlock block, Putein put) throws RetroTectorException, FinishedException {
	
		try {
			coreMiddleDynamicMatrix = new CoreMiddleDynamicMatrix(rect, block, put);
		} catch (RetroTectorException rte) {
			RetroTectorException.sendError(coreMiddleDynamicMatrix, "MiddleDynamicMatrix", rte);
		}
		
	} // end of constructor(Utilities.Rectangle, ORFID.ParameterBlock, Putein)
	
	
/**
* @return		The Path cpnstructed by this.
*/
	public final PathClass getPath() {

		return coreMiddleDynamicMatrix.getPath();

	} // end of getPath()
	
} // end of MiddleDynamicMatrix
