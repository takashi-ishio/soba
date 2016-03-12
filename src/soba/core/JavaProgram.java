package soba.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import soba.util.files.IClassList;
import soba.util.files.IClassListCallback;


/**
 * This class represents a Java program.
 */
public class JavaProgram {
	
	private Map<String, ClassInfo> classes;
	private ClassHierarchy classHierarchy;
	private List<ClassInfo> loaded;
	private List<ClassInfo> duplicated;
	private List<String> filtered;
	private List<ErrorMessage> errors;

	/**
	 * Creates a new <code>JavaProgram</code> instance.
	 * @param lists
	 */
	public JavaProgram(final IClassList[] lists) {
		this(lists, null);
	}
	
	/**
	 * Creates a new <code>JavaProgram</code> instance specifying classes to be analyzed.
	 * @param fileEnumeraters
	 * @param filter specifies classes to be analyzed.
	 */
	public JavaProgram(final IClassList[] lists, final IClassFilter filter) {
		classes = new HashMap<String, ClassInfo>(65536);
		errors = new ArrayList<ErrorMessage>(1024);
		loaded = new ArrayList<ClassInfo>(65536);
		duplicated = new ArrayList<ClassInfo>(1024);
		filtered = new ArrayList<String>(1024);
		classHierarchy = new ClassHierarchy();
		
		for (final IClassList list: lists) {
			if (list == null) continue;
			
			list.process(new IClassListCallback() {
				
				@Override
				public boolean reportError(String name, Exception e) {
					errors.add(new ErrorMessage(name, e));
					return false;
				}
				
				@Override
				public void process(String name, InputStream stream) throws IOException {
					if (filter == null || filter.loadClass(name)) {
						ClassInfo c = new ClassInfo(name, stream, list.getLabel());
						if (filter == null || filter.acceptClass(c)) {
							if (!classes.containsKey(c.getClassName())) {
								classes.put(c.getClassName(), c);
								loaded.add(c);
								classHierarchy.registerClass(c);
							} else {
								duplicated.add(c);
							}
						} else {
							filtered.add(name);
						}
					} else {
						filtered.add(name);
					}
				}
				
				@Override
				public boolean isTarget(String name) {
					return name.endsWith(".class");
				}
			});
		}
	}
		
	/**
	 * @return a list of loaded <code>ClassInfo</code> objects.
	 */
	public List<ClassInfo> getClasses() {
		return loaded;
	}
	
	/**
	 * @return a list of filtered classes.
	 */
	public List<String> getFiltered() {
		return filtered;
	}
	
	/**
	 * @return a list of duplicated <code>ClassInfo</code> objects.
	 * If the analyzed files contain classes whose names are same
	 * (with their package names), this method returns a non-empty list. 
	 */
	public List<ClassInfo> getDuplicated() {
		return duplicated;
	}

	/**
	 * @return a list of library classes.
	 */
	public List<ClassInfo> getLibraryClasses() {
		List<ClassInfo> libs = new ArrayList<>();
		for (ClassInfo c: classes.values()) {
			if (c.isLibrary()) {
				libs.add(c);
			}
		}
		return libs;
	}
	
	/**
	 * @param className is a class name including its package name.
	 * @return a <code>ClassInfo</code> object specified by its name.
	 */
	public ClassInfo getClassInfo(String className) {
		return classes.get(className);
	}
	
	/**
	 * @return a <code>ClassHierarchy</code> object which has hierarchy information of the analyzed classes.
	 */
	public ClassHierarchy getClassHierarchy() {
		return classHierarchy;
	}
	
	/**
	 * @return a list of error messages.
	 */
	public List<ErrorMessage> getErrors() {
		return errors;
	}
	
	public static class ErrorMessage { 
		private String dataName;
		private Exception exception;
		public ErrorMessage(String name, Exception e) {
			this.dataName = name;
			this.exception = e;
		}
		public String getDataName() {
			return dataName;
		}
		public Exception getException() {
			return exception;
		}
	}
}
