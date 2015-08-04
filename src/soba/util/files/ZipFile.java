package soba.util.files;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * A class to select files to be processed.
 *
 */
public class ZipFile implements IClassList {

	private static final String[] zipExt = new String[] { ".jar", ".zip", ".war" };

	public static boolean isZipFile(String filename) { 
		String lowerFileName = filename.toLowerCase();
		for (String ext: zipExt) {
			if (lowerFileName.endsWith(ext)) return true;
		}
		return false;
	}

	public static boolean isZipFile(File f) {
		return f.isFile() && isZipFile(f.getAbsolutePath());
	}


	public static boolean isClassFile(String filename) {
		String lowerFileName = filename.toLowerCase(); 
		return lowerFileName.endsWith(".class");
	}

	public static boolean isClassFile(File f) {
		return f.isFile() && isClassFile(f.getAbsolutePath());
	}

	public static class ClassFileFilter implements FileFilter {

		@Override
		public boolean accept(File f) {
			return f.isDirectory() || isClassFile(f) || isZipFile(f);
		}
	}
	
	private File zip;
	private String label;
	private boolean searchRecursive; 
	
	public ZipFile(File zipFile) {
		assert isZipFile(zipFile);
		this.zip = zipFile;
	}
	
	public void setLabel(String l) {
		this.label = l;
	}
	
	@Override
	public String getLabel() {
		return label;
	}
	
	public void enableRecursiveSearch() {
		searchRecursive = true;
	}

	@Override
	public void process(IClassListCallback c) {
		try {
			processZip(new FileInputStream(zip), zip.getAbsolutePath(), c, true);
		} catch (IOException e) {
			c.reportError(zip.getAbsolutePath(), e);
		}
	}

	private void processZip(InputStream stream, String zipFilename, IClassListCallback c, boolean closeStream) {
		ZipInputStream zip = new ZipInputStream(stream);
		String lastEntry = zipFilename;
		try {
			ZipEntry entry = zip.getNextEntry();
			while (entry != null) {
				lastEntry = zipFilename + "/" + entry.getName();
				if (c.isTarget(entry.getName())) {
					c.process(lastEntry, zip);
				} else if (searchRecursive && ZipFile.isZipFile(entry.getName())) {
					processZip(zip, lastEntry, c, false);
				}
				zip.closeEntry();
				entry = zip.getNextEntry();
			}
			if (closeStream) {
				zip.close();
			}
		} catch (IOException e) {
			c.reportError(lastEntry, e);
		} catch (RuntimeException e) {
			c.reportError(lastEntry, e);
		}
	}

}
