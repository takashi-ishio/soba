package soba.core.signature;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;


/**
 * This class translates a method signature to 
 * a list of parameters, a return type, and a list of exception types.
 * TODO The current implementation ignores type arguments for generic class.
 */
public class MethodSignatureReader {

	private int paramCount;
	private TypeVisitor returnTypeVisitor;
	private List<TypeVisitor> exceptionTypeVisitors;
	private List<TypeVisitor> paramTypeVisitors;

	/**
	 * Creates a new <code>MethodSignatureReader</code> instance.
	 * @param signature
	 */
	public MethodSignatureReader(String signature) {
		exceptionTypeVisitors = new ArrayList<TypeVisitor>();
		paramTypeVisitors = new ArrayList<TypeVisitor>();
		paramCount = 0;

		SignatureReader sigReader = new SignatureReader(signature);
		sigReader.accept(new SignatureVisitor(Opcodes.ASM5) {
			
			/* 
			 * SignatureVisitor for a method signature receives a following method call sequence.
			 * ( visitFormalTypeParameter visitClassBound? visitInterfaceBound* )* 
			 * ( visitParameterType* 
			 *   visitReturnType 
			 *   visitExceptionType* )
			 */

			@Override
			public void visitFormalTypeParameter(String name) {
			}
			
			@Override
			public SignatureVisitor visitParameterType() {
				paramCount++;
				TypeVisitor param = new TypeVisitor();
				paramTypeVisitors.add(param);
				return param;
			}

			@Override
			public SignatureVisitor visitReturnType() {
				returnTypeVisitor = new TypeVisitor();
				return returnTypeVisitor;
			}
			
			@Override
			public SignatureVisitor visitExceptionType() {
				TypeVisitor exceptionTypeVisitor = new TypeVisitor();
				exceptionTypeVisitors.add(exceptionTypeVisitor);
				return exceptionTypeVisitor;
			}
			
			
			@Override
			public void visitTypeVariable(String name) {
			}
			
			@Override
			public SignatureVisitor visitTypeArgument(char wildcard) {
				return this;
			}
			
			@Override
			public void visitTypeArgument() {
			}
			
			@Override
			public SignatureVisitor visitSuperclass() {
				return this;
			}
			
			@Override
			public SignatureVisitor visitInterfaceBound() {
				return this;
			}
			
			@Override
			public SignatureVisitor visitInterface() {
				return this;
			}
			
			@Override
			public void visitInnerClassType(String name) {
			}

			@Override
			public void visitEnd() {
			}
			
			@Override
			public void visitClassType(String name) {
			}
			
			@Override
			public SignatureVisitor visitClassBound() {
				return this;
			}
			
			@Override
			public void visitBaseType(char descriptor) {
			}
			
			@Override
			public SignatureVisitor visitArrayType() {
				return null;
			}
		});
		assert paramTypeVisitors.size() == paramCount: 
			    "Failed to read a method signature: paramTypes=" + Integer.toString(paramTypeVisitors.size()) + ", paramCount=" + Integer.toString(paramCount);
	}
	
	public int getParamCount() {
		return paramCount;
	}
	
	public String getParamType(int paramIndex) {
		return paramTypeVisitors.get(paramIndex).getTypeName();
	}

	public boolean isGenericType(int paramIndex) {
		return paramTypeVisitors.get(paramIndex).isGenericType();
	}
	
	public String getReturnType() {
		return returnTypeVisitor.getTypeName();
	}

	public boolean isGenericReturnType() {
		return returnTypeVisitor.isGenericType();
	}
	
	public int getExceptionCount() {
		return exceptionTypeVisitors.size();
	}
	
	public String getExceptionType(int exceptionIndex) {  
		return exceptionTypeVisitors.get(exceptionIndex).getTypeName();
	}

	public boolean isGenericExceptionType(int exceptionIndex) {  
		return exceptionTypeVisitors.get(exceptionIndex).isGenericType();
	}

}
