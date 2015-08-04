package soba.core.vta;

import gnu.trove.list.array.TIntArrayList;

import java.util.ArrayList;

import soba.core.MethodInfo;
import soba.core.method.LocalVariables;
import soba.core.signature.TypeConstants;

public class MethodVertices {
	
	
	private LocalVariables locals;
	private int[] variableIndex;

	private int[] vertexIDs;
	private int returnVertexID;

	private int[] paramVertexIDs;
	private int vertexCount = 0;
	
	private ArrayList<String> typenames;

	public MethodVertices(MethodInfo m, LocalVariables table, int startID) {
		this.locals = table;
		this.variableIndex = new int[m.getParamCount()];
		this.typenames = new ArrayList<String>();
		
		paramVertexIDs = new int[m.getParamCount()];
		int vID = startID;
		int var = 0;
		for (int i=0; i<m.getParamCount(); ++i) {
			String type = m.getParamType(i);
			variableIndex[i] = var;
			var += TypeConstants.getWordCount(type);
			if (!TypeConstants.isPrimitiveOrVoid(type)) {
				paramVertexIDs[i] = vID;
				vID++;
				typenames.add(type);
			}
		}
		
		TIntArrayList vertices = new TIntArrayList();
		for (int i=0; i<locals.getVariableEntryCount(); ++i) {
			if (locals.isObjectVariable(i)) {
				if (locals.isParameter(i)) {
					int paramIndex = getParamIndex(locals.getVariableIndex(i));
					vertices.add(paramVertexIDs[paramIndex]);
				} else {
					vertices.add(vID);
					vID++;
					String t = locals.getVariableType(i);
					if (t == null) {
						if (locals.isArrayVariable(i)) {
							t = TypeSet.DEFAULT_UNKNOWN_ARRAYTYPE;
						} else {
							t = TypeSet.DEFAULT_UNKNOWN_TYPE;
						}
					}
					typenames.add(t);
				}
			} else {
				vertices.add(VTAResolver.VERTEX_ERROR);
			}
		}
		vertexIDs = vertices.toArray();
		
		if (!TypeConstants.isPrimitiveOrVoid(m.getReturnType())) {
			returnVertexID = vID;
			vID++;
			typenames.add(m.getReturnType());
		}
		vertexCount = vID - startID;
	}
	
	public int getLocalVertex(int instruction) {
		int entryIndex = locals.findEntryForInstruction(instruction);
		if (entryIndex != -1) {
			return vertexIDs[entryIndex];
		} else {
			// Some local variables are assigned but never used.
			return VTAResolver.VERTEX_ERROR;
		}
	}
	
	public int getReturnVertex() {
		return returnVertexID;
	}
	
	public int getFormalVertex(int paramIndex) {
		return paramVertexIDs[paramIndex];
	}
	
	public boolean hasFormalVertex(int paramIndex) {
		return paramVertexIDs[paramIndex] != VTAResolver.VERTEX_ERROR;
	}
	
	private int getParamIndex(int varIndex) {
		for (int i=0; i<variableIndex.length; ++i) {
			if (variableIndex[i] == varIndex) {
				return i;
			}
		}
		return -1;
	}
	
	public int getVertexCount() {
		return vertexCount;
	}
	
	public String getTypeName(int vertexIndex) { 
		return typenames.get(vertexIndex);
	}

}
