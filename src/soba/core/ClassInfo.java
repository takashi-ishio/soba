package soba.core;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.JSRInlinerAdapter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import soba.util.files.FileUtil;

/**
 * This class represents a java class.
 */
public class ClassInfo {

	public static final String PACKAGE_SEPARATOR ="/";
	private static final String DEFAULT_PACKAGE = "$default$";
	public static final String LIBRARY_LABEL = "$library$";
	
	private String fileName;
	private String sourceFileName;
	private String packageName;
	private String className;
	private String md5hash;
	private String label;
	private List<MethodInfo> methods = new ArrayList<>();
	private List<FieldInfo> fields = new ArrayList<>();
	
	private String superclassName;
	private List<String> interfaceNames;
	
	/**
	 * Creates a new <code>ClassInfo</code> instance from a binary stream.
	 * @param fileName
	 * @param binaryStream specifies a stream of Java bytecode.
	 * @param loaderLabel specifies a label indicating a location/category for a class.
	 * @throws IOException
	 */
	public ClassInfo(String fileName, InputStream binaryStream, String loaderLabel) throws IOException {
		this(fileName, binaryStream);
		this.label = loaderLabel;
	}

	/**
	 * Creates a new <code>ClassInfo</code> instance from a binary stream.
	 * @param fileName
	 * @param binaryStream specifies a stream of Java bytecode.  
	 * Please note that the stream is not closed by the method; 
	 * the callers must close the stream by themselves.
	 */
	public ClassInfo(String fileName, InputStream binaryStream) throws IOException {
		this.fileName = fileName; 
		byte[] bytes = FileUtil.readFully(binaryStream);
		ClassReader cr1;
		try {
			cr1 = new ClassReader(bytes) {
				/**
				 * This extension reduces the number of allocated strings.
				 * When reading SOBA and GNU Trove class files, 52MB of 72MB strings can be discarded.
				 */
				@Override
				public String readUTF8(int index, char[] buf) {
					return super.readUTF8(index, buf);
				}
				
			};
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new ClassReadFailureException("ASM ClassReader cannot parse the bytecode. " + fileName + " " + e.getLocalizedMessage());
		}
		ClassNode classNode = new ClassNode(Opcodes.ASM5) {
			@Override
			public MethodVisitor visitMethod(int access, String name,
					String desc, String signature, String[] exceptions) {
				return new JSRInlinerAdapter(super.visitMethod(access, name, desc, signature, exceptions), access, name, desc, signature, exceptions);
			}
		};
		cr1.accept(classNode, 0);
		this.className = classNode.name;

		int pkgIndex = className.lastIndexOf(PACKAGE_SEPARATOR);
		if (pkgIndex >= 0) {
			packageName = className.substring(0, pkgIndex);
		} else {
			packageName = DEFAULT_PACKAGE;
		}
		
		if (classNode.sourceFile != null) {
			this.sourceFileName = getClassDirPath() + File.separator + classNode.sourceFile;
		} else {
			this.sourceFileName = null;
		}

		this.md5hash = MD5.getMD5(bytes);

		for (MethodNode m: classNode.methods) {
			methods.add(new MethodInfo(this, m));
		}
		
		for (int i=0; i<classNode.fields.size(); ++i) {
			fields.add(new FieldInfo(this, (FieldNode)classNode.fields.get(i)));
		}
		superclassName = classNode.superName;
		interfaceNames = new ArrayList<String>(classNode.interfaces.size());
		for (int i=0; i<classNode.interfaces.size(); ++i) {
			interfaceNames.add((String)classNode.interfaces.get(i));
		}
	}


	public static ClassInfo createLibraryClass(String fileName, InputStream binaryStream) throws IOException {
		ClassInfo c = new ClassInfo(fileName, binaryStream);
		c.label = LIBRARY_LABEL;
		return c;
	}
	
	/**
	 * @return a package name.
	 * A package name is separated by "/".
	 */
	public String getPackageName() {
		return packageName;
	}

	/**
	 * @return a class name with its package name.
	 * The class name is an internal representation; 
	 * a package name is separated by "/".
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * @return a super class name with its package name.
	 */
	public String getSuperClass() {
		return superclassName;
	}
	
	/**
	 * @return a list of interface names this class implements.
	 */
	public List<String> getInterfaces() {
		return interfaceNames;
	}
	
	/**
	 * @return a MD5 hash value.
	 */
	public String getHash() {
		return md5hash;
	}

	/**
	 * @return a directory path which has this class file.
	 */
	public String getClassDirPath() {
		return packageName.replace('/', File.separatorChar); 
	}

	/**
	 * @return the file name whose containing the class data.
	 * If the class file is contained in a ZIP/JAR file,
	 * the resultant path incidates the zip file and an internal file path in the archive.
	 */
	public String getClassFileName() {
		return fileName;
	}
	
	/**
	 * @return a label attached to this class.
	 * This is expected to distinguish a library class or an application class.
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @return true if this class is a library
	 */
	public boolean isLibrary() {
		return getLabel() == LIBRARY_LABEL;
	}
	
	/**
	 * @return a Java source file name.
	 * The file name is a relative path.
	 * The method may return null. 
	 */
	public String getSourceFileName() {
		return sourceFileName;
	}

	/**
	 * @return the number of methods declared in this class.
	 */
	public int getMethodCount() {
		return methods.size();
	}

	/**
	 * @param methodIndex
	 * @return a <code>MethodInfo</code> object specified by its index.
	 */
	public MethodInfo getMethod(int methodIndex) {
		return methods.get(methodIndex);
	}

	/**
	 * @return a list of <code>MethodInfo</code> objects declared in this class.
	 */
	public List<MethodInfo> getMethods() {
		return methods;
	}

	/**
	 * @param methodName is a method name.
	 * @param methodDesc is a method descriptor without generics information.
	 * @return a <code>MethodInfo</code> object if the class declares the specified method. 
	 */
	public MethodInfo findMethod(String methodName, String methodDesc) {
		for (MethodInfo m: methods) {
			if (m.getMethodName().equals(methodName) &&
					m.getDescriptor().equals(methodDesc)) {
				return m;
			}
		}
		return null;
	}
	
	/**
	 * @return the number of fields declared in this class.
	 */
	public int getFieldCount() {
		return fields.size();
	}

	/**
	 * @param fieldIndex
	 * @return a <code>FieldInfo</code> object specified by its index.
	 */
	public FieldInfo getField(int fieldIndex) {
		return fields.get(fieldIndex);
	}

	/**
	 * @return a list of <code>FieldInfo</code> objects declared in this class.
	 */
	public List<FieldInfo> getFields() {
		return fields;
	}
	
	/**
	 * @param fieldName is a field name.
	 * @param fieldDesc is a field descriptor without generics information.
	 * @return a <code>FieldInfo</code> object if the class declares the specified field.
	 */
	public FieldInfo findField(String fieldName, String fieldDesc) {
		for (FieldInfo f: fields) {
			if (f.getFieldName().equals(fieldName) &&
					f.getDescriptor().equals(fieldDesc)) {
				return f;
			}
		}
		return null;
	}
	
}
