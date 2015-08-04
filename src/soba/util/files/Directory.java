package soba.util.files;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Stack;


public class Directory implements IClassList {

	private File dir;
	private String label;
	private boolean searchZip = false;
	private boolean searchZipRecursive = false;
	private boolean autoOpen = true;
	
	public Directory(File dir) {
		assert dir.isDirectory(): dir.getAbsolutePath() + " is not a directory.";
		this.dir = dir;
	}
	
	public void setLabel(String l) {
		this.label = l;
	}
	
	@Override
	public String getLabel() {
		return label;
	}
	
	/**
	 * Enumerate a list of directories involved in the specified top directory.
	 * @param topDir specifies a directory including sub-directories to be enumerated.
	 * @param depth specifies the maximum number of recursive search.
	 * "0" returns the top directory itself. "1" returns a list of directories in the top directory.
	 * @return
	 */
	public static Directory[] listSubdirectories(File topDir, int depth) {
		ArrayList<File> dirs = new ArrayList<File>();
		dirs.add(topDir);
		
		for (int i=0; i<depth; ++i) {
			ArrayList<File> subdirs = new ArrayList<File>();
			for (File d: dirs) {
				for (File f: d.listFiles()) {
					if (f.isDirectory() && 
						!f.getName().equals(".") &&
						!f.getName().equals("..")) subdirs.add(f);
				}
			}
			dirs = subdirs;
		}
		
		Directory[] result = new Directory[dirs.size()];
		for (int i=0; i<dirs.size(); ++i) {
			result[i] = new Directory(dirs.get(i));
		}
		return result;
	}
	
	/**
	 * @return a directory information corresponding to the object.
	 */
	public File getDirectory() {
		return dir;
	}
	
	public void enableZipSearch() {
		this.searchZip = true;
	}
	
	public void enableRecursiveZipSearch() {
		this.searchZip = true;
		this.searchZipRecursive = true;
	}
	
	public void disableAutoOpen() {
		this.autoOpen = false;
	}
	
	@Override
	public void process(IClassListCallback c) {
		FileFilterCallback filter = new FileFilterCallback(c); 
		Stack<File> worklist = new Stack<File>();
		worklist.push(dir);
		
		while (!worklist.empty()) {
			File f = worklist.pop();
			if (f.isDirectory()) { 
				if (f.exists() && f.canRead()) {
					File[] contents = f.listFiles(filter);
					if (contents != null) {
						for (File content: contents) {
							if (!dir.equals(content)) { 
								worklist.add(content);
							}
						}
					}
				}
			} else if (f.isFile() && c.isTarget(f.getAbsolutePath()) &&  f.canRead()) {
				try {
					if (autoOpen) {
						FileInputStream binaryStream = new FileInputStream(f);
						c.process(f.getCanonicalPath(), binaryStream);
						binaryStream.close();
					} else {
						c.process(f.getCanonicalPath(), null);
					}
				} catch (IOException e) {
					boolean stop = c.reportError(f.getAbsolutePath(), e);
					if (stop) break;
				}
			} else if (ZipFile.isZipFile(f) && f.canRead()) {
				ZipFile zip = new ZipFile(f);
				if (searchZipRecursive) zip.enableRecursiveSearch();
				zip.process(c);
			}
		}
	}
	
	private class FileFilterCallback implements FileFilter { 
		
		private IClassListCallback callback;
		public FileFilterCallback(IClassListCallback c) {
			this.callback = c;
		}
		@Override
		public boolean accept(File f) {
		return f.isDirectory() ||
			(searchZip && ZipFile.isZipFile(f)) || 
			(f.isFile() && callback.isTarget(f.getAbsolutePath()));
		}
	}
}
