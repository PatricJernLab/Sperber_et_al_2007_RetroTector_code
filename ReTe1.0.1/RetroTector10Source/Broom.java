/*
* Copyright (©) 2000-2007, Gšran Sperber. All Rights Reserved.
*/

/**
*   For RetroTector 1.0
*
* @author  Gšran Sperber
* @version 1/1 -07
* Beautified 1/1 -07
*/
package retrotector;

import java.util.*;

/**
* Subclasses of Broom are used by SweepDNA to remove ALUs, LINEs, SINEs etc.
*
*<PRE>
*     Parameters:
*
*   BroomPriority
* Priority relative to other Brooms.
* Default: 0.
*</PRE>
*
* Subclasses may have additional parameters.
*/
public abstract class Broom extends ParameterUser implements Scorable {

/**
* Key for Broom priority = "BroomPriority".
*/
	public static final String BROOMPRIORITYKEY = "BroomPriority";

/**
* All available Brooms, sorted by broomPriority.
*/
	public static Broom[ ] brooms;
	
// static methods
/**
* Collects Broom subclasses from the 'builtins' package and the 'plugins' directory.
* They are stored in brooms.
*/
	public static void collectBrooms() throws RetroTectorException {
		Stack imp = new Stack();
		Class c;
		Class[ ] cc = PluginManager.BUILTANDPLUGINS;
		for (int i=0; i<PluginManager.BUILTANDPLUGINS.length; i++) {
			c = PluginManager.BUILTANDPLUGINS[i];
      if (Utilities.subClassOf(c, "retrotector.Broom")) {
				try {
					imp.push(c.newInstance());
				} catch (InstantiationException ie) {
				} catch (IllegalAccessException iae) {
					throw new RetroTectorException("Broom", "Could not instantiate", c.getName());
				}
			}
		}
		brooms = new Broom[imp.size()];
		imp.copyInto(brooms);
		Utilities.sort(brooms); // sort by priority
	} // end of collectBrooms()


/**
* To know if anything was done by any Broom since this was last zeroed                  .
*/
	public static int doneCount;

/**
* Brooms are applied in descending order of this. Default = 0.
*/
	protected float broomPriority;
	
/**
* To keep count of finds by a particular Broom.
*/
	protected int count;
	
/**
* Sets finds counter to zero.
*/
	public void zeroCount() {
		count = 0;
	} // end of zeroCount()
	
/**
* Increments finds counter.
*/
	protected void incrementCount() {
		count++;
	} // end of incrementCount()
	
/**
* @return		Number of finds.
*/
	public int getCount() {
		return count;
	} // end of int getCount()
	
/**
* @return	Name base for finds in primary strand.
*/
	public abstract String getDirtName();

/**
* @return	Name base for finds in complementary strand.
*/
	public String getCDirtName() {
		return "C" + getDirtName();
	} // end of getCDirtName()

/**
* @param		inDNA		DNA to search in.
* @return	inDNA with finds inserted.
*/
	public abstract DNA findDirt(DNA inDNA) throws RetroTectorException;


/**
* As required by Scorable.
*	@return	broomPriority.
*/
	public float fetchScore() {
		return broomPriority;
	} // end of fetchScore()
	
	
}
