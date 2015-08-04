package soba.util.files;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import soba.core.ClassInfo;

/**
 * The factory creates a list of JAR files in the system class paths.
 */
public class ClasspathUtil {
	
	public static IClassList[] getClassList(String[] files) {
		return getClassList(Arrays.asList(files), null);
	}

	public static IClassList[] getClassList(String[] files, String label) {
		return getClassList(Arrays.asList(files), label);
	}

	public static IClassList[] getClassList(List<String> files) { 
		return getClassList(files, null);
	}

	public static IClassList[] getClassList(String[] appFiles, String[] libFiles) {
		IClassList[] apps = getClassList(appFiles);
		IClassList[] libs = getClassList(libFiles, ClassInfo.LIBRARY_LABEL);
		return merge(apps, libs);
	}
	
	public static IClassList[] getClassList(List<String> files, String label) { 
		List<IClassList> result = new ArrayList<IClassList>();
		for (String filepath: files) {
			File f = new File(filepath);
			if (f.isDirectory()) {
				Directory dir = new Directory(f);
				dir.enableRecursiveZipSearch();
				dir.setLabel(label);
				result.add(dir);
			} else if (ZipFile.isZipFile(f)) {
				ZipFile zip = new ZipFile(f);
				zip.enableRecursiveSearch();
				zip.setLabel(label);
				result.add(zip);
			} else if (ZipFile.isClassFile(f)) {
				SingleFile file = new SingleFile(f);
				file.setLabel(label);
				result.add(file);				
			}
		}
		return result.toArray(new IClassList[0]);
	}

	public static List<String> enumerateSystemClasspath() {
		final String PATH_SPLIT_REGEX = "\\s*" + File.pathSeparatorChar + "\\s*";
		List<String> classpath = new ArrayList<String>(1024);
		
        String classPath = System.getProperty("java.class.path");
        for (String path: classPath.split(PATH_SPLIT_REGEX)) {
        	classpath.add(new File(path).getAbsolutePath());
        }
        
        String bootClassPath = System.getProperty("sun.boot.class.path");
        for (String path: bootClassPath.split(PATH_SPLIT_REGEX)) {
        	classpath.add(new File(path).getAbsolutePath());
        }
        
        String extDirs = System.getProperty("java.ext.dirs");
        for (String extDirPath: extDirs.split(PATH_SPLIT_REGEX)) {
        	File extDir = new File(extDirPath);
        	if (extDir.isDirectory() && extDir.canRead()) {
        		File[] extFiles = extDir.listFiles(new FileFilter() {
					@Override
					public boolean accept(File pathname) {
						String lowerFilename = pathname.getAbsolutePath();
						return lowerFilename.endsWith(".jar") || lowerFilename.endsWith(".zip");
					}
				});
        		for (File extFile: extFiles) {
        			classpath.add(extFile.getAbsolutePath());
        		}
        	}
        }
		return classpath;
	}

	public static IClassList[] merge(IClassList[] list1, IClassList[] list2) {
		IClassList[] result = Arrays.copyOf(list1, list1.length + list2.length);
		System.arraycopy(list2, 0, result, list1.length, list2.length);
		return result;
	}
}
