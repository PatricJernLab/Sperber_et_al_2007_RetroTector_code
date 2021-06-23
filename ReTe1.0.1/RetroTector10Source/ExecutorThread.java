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

/**
* Thread started by a menu command or ExecutorScript to execute an Executor.
*/
class ExecutorThread extends Thread {

/**
* Is set to signal abortion of this as soon as feasible.
*/
	boolean abortFlag = false;
	
	private final Executor THEEXECUTOR;
	private final ParameterFileReader THEREADER;
	
/**
* Makes an ExecutorThread ready for running.
* @param e	The executor to run
* @param p	The file to pick parameters from
*/
	ExecutorThread(Executor e, ParameterFileReader p) {
		THEEXECUTOR = e;
		THEREADER = p;
		RetroTectorEngine.currentThread = this; // to make sure
	} // end of constructor(Executor, ParameterFileReade)
	
/**
* Runs the executor.
*/
	public void run() {
		try {
			THEEXECUTOR.initialize(THEREADER);
			if (THEEXECUTOR.runFlag) {
				RetroTectorEngine.setExecuting(THEEXECUTOR);
				THEEXECUTOR.execute();
			} else {
				RetroTectorEngine.setExecuting(null);
				RetroTectorEngine.currentThread = null;
			}
		} catch (RetroTectorException rpe) {
			RetroTectorEngine.displayError(rpe);
		} catch (RuntimeException e) {
			RetroTectorEngine.displayError(
				new RetroTectorException(THEEXECUTOR.className(), "A Java RuntimeException occurred", THEEXECUTOR.runtimeExceptionComment(),
					"There should be more information in the text window")
			);
			RetroTectorEngine.toLogFile(e.toString());
			throw e;
		} finally {
			RetroTectorEngine.setExecuting(null);
			RetroTectorEngine.currentThread = null;
		}
	} // end of run()

} // end of ExecutorThread
