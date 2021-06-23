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

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.zip.*;

/**
* For transparent handling of zipped files.
*/
public class ZFile extends File {

/**
* = "/".
*/
	public static final String SLASH = "/";

/**
* The ZipFile containing this.
*/
	public final ZipFile ZIPFILE;
	
/**
* The ZipEntry in ZIPFILE corresponding to this.
*/
	public final ZipEntry ZIPENTRY;
	
/**
* The name of this, / terminated if directory.
*/
	public final String ENTRYNAME;
	
/**
* The directory to which this belongs.
*/
	public final File PARENT;
	
/**
* True if this is a directory.
**/
	public boolean ISDIRECTORY;
	
/**
* Constructor.
* @param	directory	The containing directory (ordinary or zip file).
* @param	childName	The name of the file, ending with .zip, or / if directory.
*/
	public ZFile(File directory, String childName) throws RetroTectorException {
		super(directory, childName);
		PARENT = directory;
		if (directory instanceof ZFile) {
			ZFile zf = (ZFile) directory;
			ZIPFILE = zf.ZIPFILE;
			ENTRYNAME = zf.ENTRYNAME + childName;
			ZIPENTRY = ZIPFILE.getEntry(ENTRYNAME);
			if (ZIPENTRY == null) {
				RetroTectorException.sendError(this, "There is no entry " + ENTRYNAME + " in " + PARENT.getPath());
			}
			ISDIRECTORY = ZIPENTRY.isDirectory();

			if (ISDIRECTORY && !ENTRYNAME.endsWith(SLASH)) {
				RetroTectorException.sendError(this, "Zipped directory name " + ENTRYNAME, "does not end with " + SLASH);
			}

		} else if (childName.endsWith(".zip") | childName.endsWith(".ZIP")) {
			try {
				ZIPFILE = new ZipFile(this);
			} catch (ZipException ze) {
				throw new RetroTectorException("ZFile", "Could not create ZipFile");
			} catch (IOException ioe) {
				throw new RetroTectorException("ZFile", "Could not create ZipFile");
			}
			ENTRYNAME = "";
			ZIPENTRY = null;
			ISDIRECTORY = true;
		} else {
			throw new RetroTectorException("ZFile", "File does not exist or name extension is not .zip", directory.getPath(), childName);
		}
	} // end of constructor(File, String)
	
/**
* See File.canRead().
*/
	public boolean canRead() {
		return !isDirectory();
	} // end of canRead()
	
/**
* See File.canWrite().
*/
	public boolean canWrite() {
		return false;
	} // end of canWrite()
	
/**
* See File.createNewFile().
*/
	public boolean createNewFile() {
		return false;
	} // end of createNewFile()
	
/**
* See File.delete().
*/
	public boolean delete() {
		return false;
	} // end of delete()
	
/**
* See File.getParentFile().
*/
	public File getParentFile() {
		return PARENT;
	} // end of getParentFile()
	
/**
* See File.getName(). / terminated if directory.
*/
	public String getName() {
		if (isDirectory()) {
			return super.getName() + SLASH;
		} else {
			return super.getName();
		}
	} // end of getName()
	
/**
* See File.isDirectory().
*/
	public boolean isDirectory() {
		return ISDIRECTORY;
	} // end of isDirectory()
	
/**
* See File.isFile().
*/
	public boolean isFile() {
		return false;
	} // end of isFile()
	
/**
* See File.lastModified().
*/
	public long lastModified() {
		if (ZIPENTRY == null) {
			return super.lastModified();
		} else {
			return ZIPENTRY.getTime();
		}
	} // end of lastModified()
	
/**
* See File.length().
*/
	public long length() {
		if (ZIPENTRY == null) {
			return super.length();
		} else {
			return ZIPENTRY.getSize();
		}
	} // end of length()
	
/**
* See File.listFiles().
*/
	public File[ ] listFiles() {
		if (!isDirectory()) {
			return null;
		}
		Enumeration enu = ZIPFILE.entries();
		String s;
		ZipEntry ze;
		int ind;
		Stack st = new Stack();
		while (enu.hasMoreElements()) {
			ze = (ZipEntry) enu.nextElement();
			s = ze.getName();
			if (s.startsWith(ENTRYNAME) && (s.length() > ENTRYNAME.length())) {
				ind = s.indexOf(SLASH, ENTRYNAME.length());
				if ((ind < 0) | (ind == s.length() - 1)) {
					s = s.substring(ENTRYNAME.length());
					if (!s.startsWith("__")) {
						try {
							st.push(new ZFile(this, s));
						} catch (RetroTectorException e) {
							RetroTectorEngine.displayError(e);
						}
					}
				}
			}
		}
		ZFile[ ] ss = new ZFile[st.size()];
		st.copyInto(ss);
		Arrays.sort(ss);
		return ss;
	} // end of listFiles()
	
/**
* See File.listFiles(FileFilter).
*/
	public File[ ] listFiles(FileFilter filter) {
		if (!isDirectory()) {
			return null;
		}
		Enumeration enu = ZIPFILE.entries();
		String s;
		ZipEntry ze;
		int ind;
		Stack st = new Stack();
		ZFile f;
		while (enu.hasMoreElements()) {
			ze = (ZipEntry) enu.nextElement();
			s = ze.getName();
			if (s.startsWith(ENTRYNAME) && (s.length() > ENTRYNAME.length())) {
				ind = s.indexOf(SLASH, ENTRYNAME.length());
				if ((ind < 0) | (ind == s.length() - 1)) {
					try {
						f = new ZFile(this, s.substring(ind + 1));
						if (filter.accept(f)) {
							st.push(f);
						}
					} catch (RetroTectorException e) {
						RetroTectorEngine.displayError(e);
					}
				}
			}
		}
		ZFile[ ] ss = new ZFile[st.size()];
		st.copyInto(ss);
		Arrays.sort(ss);
		return ss;
	} // end of listFiles(FileFilter)
	
/**
* See File.list(.
*/
	public String[ ] list() {
		File[ ] ff = listFiles();
		String[ ] ss = new String[ff.length];
		for (int i=0; i<ff.length; i++) {
			ss[i] = ff[i].getName();
		}
		Arrays.sort(ss);
		return ss;
	} // end of list()
	
/**
* See File.list(FilenameFilter).
*/
	public String[ ] list(final FilenameFilter filter) {
		File[ ] ff = listFiles(
				new FileFilter() {
						public boolean accept(File f) {
							return filter.accept(f.getParentFile(), f.getName());
						}
				}
		);
		String[ ] ss = new String[ff.length];
		for (int i=0; i<ff.length; i++) {
			ss[i] = ff[i].getName();
		}
		Arrays.sort(ss);
		return ss;
	} // end of list(FilenameFilter)
	
/**
* See File.mkdir.
*/
	public boolean mkdir() {
		return false;
	} // end of mkdir()
	
/**
* See File.mkdirs.
*/
	public boolean mkdirs() {
		return false;
	} // end of mkdirs()
	
/**
* See File.setLastModified.
*/
	public boolean setLastModified(long time) {
		return false;
	} // end of setLastModified(long)
	
/**
* See File.renameTo.
*/
	public boolean renameTo(File dest) {
		return false;
	} // end of renameTo(File)
	
/**
* See File.setReadOnly.
*/
	public boolean setReadOnly() {
		return true;
	} // end of setReadOnly()
	
/**
* See File.toURI.
*/
	public URI toURI() {
		return null;
	} // end of toURI()
	
/**
* See File.toURL.
*/

	public URL toURL() {
		return null;
	} // end of toURL()
	
} // end of ZFile
