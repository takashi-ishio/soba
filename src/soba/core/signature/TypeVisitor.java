package soba.core.signature;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.signature.SignatureVisitor;

/**
 * TypeVisitor processes a type signature representing a single type.
 */
public class TypeVisitor extends SignatureVisitor {
	
	// TypeVisitor receives the following method calls:
	// ( visitBaseType | 
	//   visitTypeVariable | 
	//   visitArrayType | 
	//   ( visitClassType visitTypeArgument* ( visitInnerClassType visitTypeArgument* )* visitEnd )
	// )
	
	private int arrayDimension = 0;

	private String typename = null;
	private boolean generictype = false;
	private List<TypeVisitor> typeArguments = null;
	private List<String> innerTypeNames = null;
	private List<List<TypeVisitor>> innerTypeArguments = null;
	private boolean parsingInner = false;
	
	public TypeVisitor() {
		super(Opcodes.ASM5);
	}
	
	/**
	 * @return a type name.
	 * A return value may include generic types
	 * such as "java/util/List<java/lang/String>" and "java/util/List<T>".
	 */
	public String getTypeName() {
		StringBuilder buf = new StringBuilder();
		buf.append(typename);
		
		if (typeArguments != null) {
			buf.append("<");
			for (int i=0; i<typeArguments.size(); ++i) {
				if (i>0) buf.append(",");
				buf.append(typeArguments.get(i).getTypeName());
			}
			buf.append(">");
		}
		if (innerTypeNames != null) {
			for (int i=0; i<innerTypeNames.size(); ++i) {
				buf.append(".");
				buf.append(innerTypeNames.get(i));
				
				List<TypeVisitor> arguments = innerTypeArguments.get(i);
				if (arguments.size() > 0) {
					buf.append("<");
					for (int innerArg=0; innerArg<arguments.size(); ++innerArg) {
						if (innerArg>0) buf.append(",");
						buf.append(arguments.get(innerArg).getTypeName());
					}
					buf.append(">");
				}
				
			}
		}
		
		for (int i=0; i<arrayDimension; ++i) {
			buf.append("[]");
		}
		return buf.toString(); 
	}
	
	public boolean isGenericType() {
		return generictype;
	}
	

	@Override
	public void visitBaseType(char descriptor) {
		switch (descriptor) {
		case 'Z':
			typename = TypeConstants.BOOLEAN;
			break;
		case 'B':
			typename = TypeConstants.BYTE;
			break;
		case 'C': 
			typename = TypeConstants.CHAR;
			break;
		case 'S':
			typename = TypeConstants.SHORT;
			break;
		case 'I': 
			typename = TypeConstants.INT;
			break;
		case 'J': 
			typename = TypeConstants.LONG;
			break;
		case 'F':
			typename = TypeConstants.FLOAT;
			break;
		case 'D':
			typename = TypeConstants.DOUBLE;
			break;
		case 'V':
			typename = TypeConstants.VOID;
			break;
		default:
			typename = Character.toString(descriptor);
		}
	}

	@Override
	public void visitTypeVariable(String name) {
		typename = name;
		generictype = true;
	}

	@Override
	public SignatureVisitor visitArrayType() {
		arrayDimension++;
		return this; // reuse the object to parse the base type.
	}

	@Override
	public void visitClassType(String name) {
		typename = name;
	}

	private TypeVisitor createTypeArgumentVisitor() {
		TypeVisitor argument = new TypeVisitor();
		if (parsingInner) {
			int size = innerTypeArguments.size();
			innerTypeArguments.get(size-1).add(argument);
		} else {
			if (typeArguments == null) {
				typeArguments = new ArrayList<TypeVisitor>();
			}
			typeArguments.add(argument);
		}
		return argument;
	}
	
	@Override
	public void visitTypeArgument() {
		// Add a visitor and directly store data to the visitor.
		TypeVisitor argument = createTypeArgumentVisitor();
		argument.typename = "?";
		argument.generictype = true;
	}

	@Override
	public SignatureVisitor visitTypeArgument(char wildcard) {
		TypeVisitor argument = createTypeArgumentVisitor();
		return argument;
	}

	// Inner class type is separated from class type 
	// in order to process a nested generic class such as "C<T1>.INNER<T2>".
	@Override
	public void visitInnerClassType(String name) {
		parsingInner = true;
		if (innerTypeNames == null) {
			innerTypeNames = new ArrayList<String>();
			innerTypeArguments = new ArrayList<List<TypeVisitor>>();
		}
		innerTypeNames.add(name);
		innerTypeArguments.add(new ArrayList<TypeVisitor>());
	}

	@Override
	public void visitEnd() {
	}

	

	// Following methods are not used.
	
	@Override
	public SignatureVisitor visitInterface() {
		assert false: "This method never used for parsing a type.";
		return null;
	}

	@Override
	public SignatureVisitor visitInterfaceBound() {
		assert false: "This method never used for parsing a type.";
		return null;
	}

	@Override
	public SignatureVisitor visitClassBound() {
		assert false: "This method never used for parsing a type.";
		return null;
	}

	@Override
	public SignatureVisitor visitExceptionType() {
		assert false: "This method never used for parsing a type.";
		return null;
	}

	@Override
	public void visitFormalTypeParameter(String name) {
		assert false: "This method never used for parsing a type.";
	}

	@Override
	public SignatureVisitor visitParameterType() {
		assert false: "This method never used for parsing a type.";
		return null;
	}

	@Override
	public SignatureVisitor visitReturnType() {
		assert false: "This method never used for parsing a type.";
		return null;
	}

	@Override
	public SignatureVisitor visitSuperclass() {
		assert false: "This method never used for parsing a type.";
		return null;
	}
	
}
