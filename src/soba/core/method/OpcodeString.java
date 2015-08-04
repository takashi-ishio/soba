package soba.core.method;


import java.util.List;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import soba.core.signature.TypeResolver;



public class OpcodeString {
	
	public static final String TYPE_PRIMITIVE = "P";
	public static final String TYPE_REFERENCE = "R";
	public static final String TYPE_UNKNOWN = "U";
	

	/**
	 * This method returns a local variable index accessed by 
	 * the specified instruction node.
	 * 
	 * The same index may be used for two or more local variables.
	 * To distinguish such variables, use findLocalVariable method. 
	 */
	public static int getVarIndex(AbstractInsnNode node) {
		if (node.getType() == AbstractInsnNode.VAR_INSN) {
			return ((VarInsnNode)node).var;
		} else if (node.getType() == AbstractInsnNode.IINC_INSN) {
			return ((IincInsnNode)node).var;
		} else {
			assert false: "getVarIndex is called for an instruction without variable informaiton.";
			return -1;
		}
	}
	
	/**
	 * This method returns true if a specified instruction 
	 * reads or writes a local variable represented by 
	 * the specified local variable node. 
	 * 
	 * @param insn specifies an instruction.
	 * @param var specifies a local variable.
	 * @return true if the instruction accesses the variable.
	 * If var is null, this method always returns false.
	 */
	public static boolean isAccess(AbstractInsnNode insn, LocalVariableNode var) {
		if (insn == null || var == null) return false;
		
		int varIndex = getVarIndex(insn);
		if (var.index == varIndex) {
			// Check whether the variable's scope includes insn. 
			AbstractInsnNode scopeEndInstruction; 
			if (isStoreOperation(insn) || isIncrementOperation(insn)) {
				if (var.start == insn.getNext()) return true;
				else {
					if (var.start != var.end &&
						var.end.getPrevious() != null && 
						(isStoreOperation(var.end.getPrevious()) || 
						isIncrementOperation(var.end.getPrevious()))) {
						scopeEndInstruction = var.end.getPrevious(); 
					} else {
						scopeEndInstruction = var.end;
					}
				}
			} else {
				assert isLoadOperation(insn) || isRET(insn);
				scopeEndInstruction = var.end;
			}
			boolean inScope = false;
			AbstractInsnNode pos = var.start;
			while ((pos != null)&&(pos != scopeEndInstruction)) {
				if (pos == insn) {
					inScope = true;
					break;
				}
				pos = pos.getNext();
			}
			return inScope;
			
		} else {
			return false;
		}
	}
	
	/**
	 * This method returns a local variable node accessed by 
	 * the specified VarInsnNode (if exists). 
	 * 
	 * This method is necessary because two or more variables may 
	 * have the same varIndex.
	 * It should be noted that a variable "x" becomes in-scope only AFTER
	 * its variable declaration such as "C x = v;".
	 * The STORE instruction accessing "x" is out of scope of the variable.
	 */
	public static LocalVariableNode findLocalVariable(MethodNode method, AbstractInsnNode insn) {
		if (method.localVariables == null) return null;

		List<?> variables = method.localVariables;
		for (int i=0; i<variables.size(); ++i) {
			LocalVariableNode var = (LocalVariableNode)variables.get(i);
			if (isAccess(insn, var)) {
				return var;
			}
		}
		return null;
	}

	
	/**
	 * Return true if a node is an instruction to load a return address
	 * that is stored into an anonymous local variable.
	 * @param node
	 * @return
	 */
	public static boolean isAfterJSR(AbstractInsnNode node) {
		if (node.getOpcode() != Opcodes.ALOAD) return false;
		
		AbstractInsnNode n = node.getPrevious();
		if (n.getType() == AbstractInsnNode.LABEL) {
			n = n.getPrevious();
		}
		return n != null && n.getOpcode() == Opcodes.JSR;
	}
	
	public static boolean isDefUseOperation(AbstractInsnNode node) {
		// Note: VarInsn and IincInsn except for "RET" instruction.
		return isStoreOperation(node) || isLoadOperation(node) || isIncrementOperation(node);
	}
	
	public static boolean isIncrementOperation(AbstractInsnNode node) {
		return node.getOpcode() == Opcodes.IINC;
	}
	
	public static boolean isPrimitiveOperation(AbstractInsnNode node) {
		int opcode = node.getOpcode();
		return (opcode == Opcodes.ISTORE ||
				opcode == Opcodes.FSTORE ||
				opcode == Opcodes.LSTORE ||
				opcode == Opcodes.DSTORE ||
				opcode == Opcodes.ILOAD ||
				opcode == Opcodes.FLOAD ||
				opcode == Opcodes.LLOAD ||
				opcode == Opcodes.DLOAD ||
				opcode == Opcodes.IINC);
	}
	
	public static boolean isStoreOperation(AbstractInsnNode node) {
		int opcode = node.getOpcode();
		return (opcode == Opcodes.ISTORE ||
			opcode == Opcodes.ASTORE ||
			opcode == Opcodes.FSTORE ||
			opcode == Opcodes.LSTORE ||
			opcode == Opcodes.DSTORE);
	}

	public static boolean isRET(AbstractInsnNode node) {
		return node.getOpcode() == Opcodes.RET;
	}
	
	public static boolean isLocalReferenceOperation(AbstractInsnNode node) {
		return isLoadOperation(node) || node.getOpcode() == Opcodes.IINC;
	}
	
	public static boolean isLoadOperation(AbstractInsnNode node) {
		int opcode = node.getOpcode();
		return (opcode == Opcodes.ILOAD ||
				opcode == Opcodes.ALOAD ||
				opcode == Opcodes.FLOAD ||
				opcode == Opcodes.LLOAD ||
				opcode == Opcodes.DLOAD);
	}
	
	public static boolean isReturnOperation(AbstractInsnNode node) {
		int opcode = node.getOpcode();
		return (opcode == Opcodes.IRETURN ||
				opcode == Opcodes.LRETURN ||
				opcode == Opcodes.FRETURN ||
				opcode == Opcodes.DRETURN ||
				opcode == Opcodes.ARETURN ||
				opcode == Opcodes.RETURN);
	}
	
	public static boolean isConstantOperation(int opcode) {
		switch (opcode) {		
		case Opcodes.ACONST_NULL:
		case Opcodes.BIPUSH:
		case Opcodes.DCONST_0:
		case Opcodes.DCONST_1:
		case Opcodes.FCONST_0:
		case Opcodes.FCONST_1:
		case Opcodes.FCONST_2:
		case Opcodes.ICONST_0:
		case Opcodes.ICONST_1:
		case Opcodes.ICONST_2:
		case Opcodes.ICONST_3:
		case Opcodes.ICONST_4:
		case Opcodes.ICONST_5:
		case Opcodes.ICONST_M1:
		case Opcodes.LCONST_0:
		case Opcodes.LCONST_1:
		case Opcodes.LDC:
		case Opcodes.SIPUSH:
			return true;
			
		default:
			return false;
		}
	}
	
	public static String getVariableString(LocalVariableNode node) { 
		return node.name + ": " + TypeResolver.getTypeName(node.desc);
	}
	

	/**
	 * @return a string representation for a label node.
	 * @see getLabelString(MethodNode, int)
	 */
	public static String getLabelString(MethodNode method, LabelNode label) {
		return getLabelString(method, method.instructions.indexOf(label));
	}

	/**
	 * @return a string representation for a label located in a position specified by index.
	 * While label.toString() returns a different string for each execution,
	 * this method returns the same string if a label is located in the same position in a method.
	 */
	public static String getLabelString(MethodNode method, int index) {
		assert method.instructions.get(index).getType() == AbstractInsnNode.LABEL;
		String right = "00000" + Integer.toString(index);
		return "L" + right.substring(right.length()-5);
	}
	
	/**
	 * @param method specifies a method containing an instruction.
	 * @param index specifies the position of an instruction in the list of instructions.
	 * @return a string representation of an instruction.
	 */
	public static String getInstructionString(MethodNode method, int index) {
		if (index == -1) return "ARG";
		
		AbstractInsnNode node = method.instructions.get(index);
		int opcode = node.getOpcode();
		String op = Integer.toString(index) + ": " + OpcodeString.getString(opcode);

		switch (node.getType()) {
		case AbstractInsnNode.VAR_INSN:
		case AbstractInsnNode.IINC_INSN:
			
			LocalVariableNode n = OpcodeString.findLocalVariable(method, node);
			if (n != null) {
				return op + " " + Integer.toString(n.index) + " (" + n.name + ")";
			} else {
				int varIndex = OpcodeString.getVarIndex(node);
				return op + " " + Integer.toString(varIndex);
			}
		
		case AbstractInsnNode.FIELD_INSN:
			FieldInsnNode fieldNode = (FieldInsnNode)node;
			return op + " " + fieldNode.owner + "#" + fieldNode.name + ": " + TypeResolver.getTypeName(fieldNode.desc);
			
		case AbstractInsnNode.METHOD_INSN:
			MethodInsnNode methodInsnNode = (MethodInsnNode)node;
			return op + " " + methodInsnNode.owner + "#" + methodInsnNode.name + methodInsnNode.desc;
		
		case AbstractInsnNode.LINE:
			LineNumberNode lineNode = (LineNumberNode)node;
			return Integer.toString(index) + ": " + "(line=" + lineNode.line + ")";
			
		case AbstractInsnNode.LABEL:
			return Integer.toString(index) + ": " + "(" + getLabelString(method, index) + ")";
			
		case AbstractInsnNode.JUMP_INSN:
			JumpInsnNode jumpNode = (JumpInsnNode)node;
			return op + " " + getLabelString(method, jumpNode.label);
			
		case AbstractInsnNode.FRAME:
			FrameNode frameNode = (FrameNode)node;
			return Integer.toString(index) + ": FRAME-OP(" + frameNode.type + ")";
			
		case AbstractInsnNode.LDC_INSN:
			LdcInsnNode ldc = (LdcInsnNode)node;
			return op + " " + ldc.cst.toString();
		
		default: 
			return op; 
		}
	}
	
	private static String getString(int opcode) {
		if (0 <= opcode && opcode < opcodeNames.length) {
			return opcodeNames[opcode];
		} else {
			return Integer.toString(opcode);
		}
	}
	
	private static String[] opcodeNames = new String[] { 
	    "NOP", "ACONST_NULL", "ICONST_M1", "ICONST_0", 
	    "ICONST_1", "ICONST_2", "ICONST_3", "ICONST_4", 
	    "ICONST_5", "LCONST_0", "LCONST_1", "FCONST_0",
	    "FCONST_1", "FCONST_2", "DCONST_0", "DCONST_1",
	    "BIPUSH", "SIPUSH", "LDC", "LDC_W", 
	    "LDC2_W", "ILOAD", "LLOAD", "FLOAD", 
	    "DLOAD", "ALOAD", "ILOAD_0", "ILOAD_1",
	    "ILOAD_2", "ILOAD_3", "LLOAD_0", "LLOAD_1",
	    "LLOAD_2", "LLOAD_3", "FLOAD_0", "FLOAD_1",
	    "FLOAD_2", "FLOAD_3", "DLOAD_0", "DLOAD_1",
	    "DLOAD_2", "DLOAD_3", "ALOAD_0", "ALOAD_1",
	    "ALOAD_2", "ALOAD_3", "IALOAD", "LALOAD",
	    "FALOAD", "DALOAD", "AALOAD", "BALOAD",
	    "CALOAD", "SALOAD", "ISTORE", "LSTORE",
	    "FSTORE", "DSTORE", "ASTORE", "ISTORE_0",
	    "ISTORE_1", "ISTORE_2", "ISTORE_3", "LSTORE_0",
	    "LSTORE_1", "LSTORE_2", "LSTORE_3", "FSTORE_0",
	    "FSTORE_1", "FSTORE_2", "FSTORE_3", "DSTORE_0",
	    "DSTORE_1", "DSTORE_2", "DSTORE_3", "ASTORE_0",
	    "ASTORE_1", "ASTORE_2", "ASTORE_3", "IASTORE",
	    "LASTORE", "FASTORE", "DASTORE", "AASTORE",
	    "BASTORE", "CASTORE", "SASTORE", "POP",
	    "POP2", "DUP", "DUP_X1", "DUP_X2",
	    "DUP2", "DUP2_X1", "DUP_X2", "SWAP",
	    "IADD", "LADD", "FADD", "DADD",
	    "ISUB", "LSUB", "FSUB", "DSUB",
	    "IMUL", "LMUL", "FMUL", "DMUL",
	    "IDIV", "LDIV", "FDIV", "DDIV",
	    "IREM", "LREM", "FREM", "DREM",
	    "INEG", "LNEG", "FNEG", "DNEG",
	    "ISHL", "LSHL", "ISHR", "LSHR",
	    "IUSHR","LUSHR", "IAND", "LAND",
	    "IOR", "LOR", "IXOR", "LXOR",
	    "IINC", "I2L", "I2F", "I2D",
	    "L2I", "L2F", "L2D", "F2I",
	    "F2L", "F2D", "D2I", "D2L",
	    "D2F", "I2B", "I2C", "I2S",
	    "LCMP", "FCMPL", "FCMPG", "DCMPL",
	    "DCMPG", "IFEQ", "IFNE", "IFLT",
	    "IFGE", "IFGT", "IFLE", "IF_ICMPEQ",
	    "IF_ICMPNE", "IF_ICMPLT", "IF_ICMPGE", "IF_ICMPGT",
	    "IF_ICMPLE", "IF_ACMPEQ", "IF_ACMPNE", "GOTO",
	    "JSR", "RET", "TABLESWITCH", "LOOKUPSWITCH", 
	    "IRETURN", "LRETURN", "FRETURN", "DRETURN",
	    "ARETURN", "RETURN", "GETSTATIC", "PUTSTATIC",
	    "GETFIELD", "PUTFIELD", "INVOKEVIRTUAL", "INVOKESPECIAL",
	    "INVOKESTATIC", "INVOKEINTERFACE", "INVOKEDYNAMIC", "NEW", 
	    "NEWARRAY", "ANEWARRAY", "ARRAYLENGTH", "ATHROW",
	    "CHECKCAST", "INSTANCEOF", "MONITORENTER", "MONITOREXIT", 
	    "WIDE", "MULTIANEWARRAY", "IFNULL", "IFNONNULL", 
	    "GOTO_W",  "JSR_W" 
	};

	static {
		assert opcodeNames.length == 202;
	}
}
