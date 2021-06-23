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
* Interface to CoreExonDynamicMatrix.
*/
public class ExonDynamicMatrix {
	  	
	private CoreExonDynamicMatrix coreExonDynamicMatrix;

/**
* Constructor.
* @param	rect	Defines segment of DNA to work in. Only x and width are relevant.
* @param	block	Collection of useful parameters.
*/
	public ExonDynamicMatrix(Utilities.Rectangle rect, ORFID.ParameterBlock block) throws RetroTectorException, FinishedException {
	
		try {
			coreExonDynamicMatrix = new CoreExonDynamicMatrix(rect, block);
		} catch (RetroTectorException rte) {
			RetroTectorException.sendError(coreExonDynamicMatrix, "ExonDynamicMatrix", rte);
		}
		
	} // end of constructor(Utilities.Rectangle, ORFID.ParameterBlock)
	
/**
* @return		The Path cpnstructed by this.
*/
	public final PathClass getPath() {
		return coreExonDynamicMatrix.getPath();
	} // end of getPath()
		
} // end of ExonDynamicMatrix
