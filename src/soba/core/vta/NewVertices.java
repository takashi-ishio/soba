package soba.core.vta;

import gnu.trove.list.array.TIntArrayList;

import java.util.ArrayList;




import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;

import soba.core.signature.TypeResolver;

public class NewVertices {

	private int[] instructionIndices;
	private int[] vertexIDs;
	private String[] types;

	
	public NewVertices(InsnList instructions, int startID) {
		TIntArrayList indices = new TIntArrayList(); 
		TIntArrayList vIDs = new TIntArrayList(); 
		ArrayList<String> typeNames = new ArrayList<String>();
		int vID = startID;
		for (int i=0; i<instructions.size(); ++i) {
			AbstractInsnNode instruction = instructions.get(i);
			if (instruction.getOpcode() == Opcodes.NEW) {
				indices.add(i);
				vIDs.add(vID);
				vID++;
				typeNames.add(((TypeInsnNode)instruction).desc);
			} else if (instruction.getOpcode() == Opcodes.ANEWARRAY) {
				indices.add(i);
				vIDs.add(vID);
				vID++;
				String desc = ((TypeInsnNode)instruction).desc;
				if (desc.startsWith("[")) {
					typeNames.add(TypeResolver.getTypeName(desc));
				} else {
					typeNames.add(desc + "[]");
				}
			} else if (instruction.getOpcode() == Opcodes.MULTIANEWARRAY) {
				indices.add(i);
				vIDs.add(vID);
				vID++;
				typeNames.add(TypeResolver.getTypeName(((MultiANewArrayInsnNode)instruction).desc));
			}
			// Implementation Note: NEWARRAY is not included because the instruction generates an array of primitive values. 
		}
		instructionIndices = indices.toArray();
		vertexIDs = vIDs.toArray();
		types = new String[typeNames.size()]; 
		for (int i=0; i<types.length; ++i) {
			types[i] = typeNames.get(i);
		}
	}
	
	public int getNewInstructionVertex(int instructionIndex) {
		for (int i=0; i<instructionIndices.length; ++i) {
			if (instructionIndices[i] == instructionIndex) {
				return vertexIDs[i];
			}
		}
		return VTAResolver.VERTEX_ERROR;
	}
	
	public int getVertex(int vertexIndex) {
		return vertexIDs[vertexIndex];
	}
	
	public String getTypeName(int vertexIndex) {
		return types[vertexIndex];
	}
	
	public int getVertexCount() {
		return vertexIDs.length;
	}
}
