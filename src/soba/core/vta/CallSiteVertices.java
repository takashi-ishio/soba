/**
 * 
 */
package soba.core.vta;


import gnu.trove.list.array.TIntArrayList;

import java.util.ArrayList;

import soba.core.method.CallSite;
import soba.core.signature.MethodSignatureReader;
import soba.core.signature.TypeConstants;

/**
 * An instance of MethodParameters represents 
 * a set of formal parameters of a method 
 * or a set of actual parameters of a method call.
 */
class CallSiteVertices {
	
	/**
	 * The number of formal parameters of the method.
	 * This includes "this", excludes a return value.
	 */
	private int paramCount;
	
	/**
	 * paramIndex[vertexIndex] specifies the position 
	 * of a method argument corresponding to the vertex.
	 * If paramIndex[vertexIndex] == paramCount, 
	 * the vertex represents a return value. 
	 */
	private int[] paramIndex;


	/**
	 * vertexIDs[vertexIndex] indicates a vertex ID.
	 */
	private int[] vertexIDs;
	
	/**
	 * vertexTypes[vertexIndex] indicates a type name
	 * corresponding to the vertex.
	 */
	private String[] vertexTypes;
	
	/**
	 * Additional parameter for for actual parameters.
	 */
	private CallSite callsite;
	
	public CallSiteVertices(CallSite c, int startID) {
		this.callsite = c;
		MethodSignatureReader sig = new MethodSignatureReader(c.getDescriptor());
		if (!c.isStaticMethod()) {
			paramCount = sig.getParamCount() + 1;
		} else {
			paramCount = sig.getParamCount();
		}

		TIntArrayList params = new TIntArrayList(paramCount);
		ArrayList<String> types = new ArrayList<String>(paramCount);
		int thisCount = 0;
		if (!c.isStaticMethod()) {
			thisCount = 1;
			params.add(0);
			types.add(c.getClassName());
		}
		
		for (int i=0; i<sig.getParamCount(); ++i) {
			String t = sig.getParamType(i);
			if (!TypeConstants.isPrimitiveOrVoid(t)) {
				params.add(i+thisCount);
				types.add(t+thisCount);
			}
		}
		if (!TypeConstants.isPrimitiveOrVoid(sig.getReturnType())) {
			params.add(paramCount);
			types.add(sig.getReturnType());
		}
		paramIndex = params.toArray();
		vertexTypes = types.toArray(new String[0]);
		vertexIDs = new int[paramIndex.length];
		for (int i=0; i<paramIndex.length; ++i) {
			vertexIDs[i] = startID + i;
		}
	}
	
	/**
	 * @return a call site information.
	 * This method returns non-null for objects 
	 * representing actual-parameters.
	 */
	public CallSite getCallSite() {
		return callsite;
	}
	
	public boolean isObjectParam(int param) {
		for (int i=0; i<paramIndex.length; ++i) {
			if (paramIndex[i] == param) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * @param param specifies the position of a method parameter
	 * in the parameter list.
	 * @return
	 */
	public int getParamVertexId(int param) {
		for (int i=0; i<paramIndex.length; ++i) {
			if (paramIndex[i] == param) {
				return vertexIDs[i];
			}
		}
		return VTAResolver.VERTEX_ERROR;
	}
	
	/**
	 * @return the number of input parameters.
	 */
	public int getParamCount() {
		return paramCount;
	}
	
	public boolean hasReturnValue() {
		if (paramIndex.length > 0) { 
			return paramIndex[paramIndex.length-1] == paramCount;
		} else {
			return false;
		}
	}
	
	public int getReturnValueVertex() {
		if (hasReturnValue()) {
			return vertexIDs[paramIndex.length-1];
		} else {
			return VTAResolver.VERTEX_ERROR;
		}
	}
	
	/**
	 * @return the number of vertices 
	 */
	public int getVertexCount() {
		return paramIndex.length;
	}
	
	/**
	 * @param vertexIndex specifies a vertex: 0 .. getVertexCount()-1. 
	 * @return vertex ID.
	 */
	public int getVertex(int vertexIndex) {
		return vertexIDs[vertexIndex];
	}
	
	public String getTypeName(int vertexIndex) {
		return vertexTypes[vertexIndex];
	}
	
	public String getReturnValueTypeName() {
		if (hasReturnValue()) {
			return vertexTypes[paramIndex.length-1];
		} else {
			assert false;
			return null;
		}
	}
	
}