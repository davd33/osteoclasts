package org.osteo.scripts;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ScriptFileCollection {

	protected List<ScriptFile> files;
	
	public ScriptFileCollection(ScriptFile...files) {
		Collections.addAll(this.files, files);
	}
	
	public ScriptFileCollection(ScriptFile.Status status, File...files) {
		init(status, files);
	}
	
	public ScriptFileCollection(File...files) {
		init(ScriptFile.Status.NONE, files);
	}
	
	protected void init(ScriptFile.Status status, File...files) {
		this.files = new ArrayList<ScriptFile>();
		for (int i = 0; i < files.length; i++)
			this.files.add(new ScriptFile(files[i], status));
	}
	
	public void add(ScriptFile file) {
		this.files.add(file);
	}
	
	/**
	 * Insert a new file or edit an existing one.
	 * 
	 * @param file
	 * @param status
	 */
	public void edit(File file, ScriptFile.Status status) {
		if (this.contains(file)) {
			ScriptFile scriptFile = this.get(file);
			scriptFile.setStatus(status);
		} else
			this.add(new ScriptFile(file, status));
	}
	
	public ScriptFile get(File file) {
		if (this.contains(file)) {
			for (Iterator<ScriptFile> sfIterator = files.iterator(); sfIterator.hasNext();) {
				ScriptFile file2 = sfIterator.next();
				if (file2.equals(file))
					return file2;
			}
		}
		return null;
	}
	
	public boolean contains(ScriptFile file) {
		for (Iterator<ScriptFile> sfIterator = files.iterator(); sfIterator.hasNext();) {
			ScriptFile file2 = sfIterator.next();
			if (file2.equals(file))
				return true;
		}
		return false;
	}
	
	public boolean contains(File file) {
		for (Iterator<ScriptFile> sfIterator = files.iterator(); sfIterator.hasNext();) {
			ScriptFile file2 = sfIterator.next();
			try {
				if (file2.getFile().getAbsoluteFile().getCanonicalPath()
						.equals(file.getAbsoluteFile().getCanonicalPath()))
					return true;
			} catch (IOException e) { }
		}
		return false;
	}
	
	public boolean contains(Object file) {
		if (file instanceof File)
			return this.contains((File)file);
		if (file instanceof ScriptFile)
			return this.contains((ScriptFile)file);
		return false;
	}
}
