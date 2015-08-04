package soba.core.method.asm;

import java.util.List;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.analysis.Value;

import soba.util.ObjectIdMap;

public class DataFlowInterpreter extends FastSourceInterpreter {
	
	private ObjectIdMap<AbstractInsnNode> instructions;
	private int[] operands;
	
	public DataFlowInterpreter(ObjectIdMap<AbstractInsnNode> instructions) {
		super(instructions);
		this.instructions = instructions;
		this.operands = new int[instructions.size()];
	}
	
	public int getInstructionCount() {
		return instructions.size();
	}
	
	public int getOperandCount(int opIndex) { 
		return operands[opIndex];
	}

	@Override
	public Value unaryOperation(AbstractInsnNode insn, Value value) {
		if (insn.getOpcode() == IINC) {
			// IINC does not use Operand Stack.
			operands[ instructions.getId(insn) ] = 0;
			return super.unaryOperation(insn, value);
		} else {
			operands[ instructions.getId(insn) ] = 1;
			return super.unaryOperation(insn, value);
		}
	}
	
	@Override
	public void returnOperation(AbstractInsnNode insn, Value value,
			Value expected) {
		operands[ instructions.getId(insn) ] = 1;
		super.returnOperation(insn, value, expected);
	}
	
	@Override
	public Value ternaryOperation(AbstractInsnNode insn, Value value1,
			Value value2, Value value3) {
		operands[ instructions.getId(insn) ] = 3;
		return super.ternaryOperation(insn, value1, value2, value3);
	}
	
	@Override
	public Value binaryOperation(AbstractInsnNode insn, Value value1,
			Value value2) {
		operands[ instructions.getId(insn) ] = 2;
		return super.binaryOperation(insn, value1, value2);
	}
	
	@Override
	public Value copyOperation(AbstractInsnNode insn, Value value) {
		int operandCount = 0;
		switch (insn.getOpcode()) {
		case ILOAD:
		case LLOAD:
		case FLOAD:
		case DLOAD:
		case ALOAD:
			operandCount = 0;
			break;
		case ISTORE:
		case LSTORE:
		case FSTORE:
		case DSTORE:
		case ASTORE:
			operandCount = 1;
			break;

		case DUP:
		case DUP_X1:
		case DUP_X2:
		case DUP2:
		case DUP2_X1:
		case DUP2_X2:
		case SWAP:
			// DUP and SWAP do not modify the value.
			// Propagate the defined value.
			return value;
			
		}
		operands[ instructions.getId(insn) ] = operandCount;
		return super.copyOperation(insn, value);
	}
	
	@Override
	public Value newValue(Type type) {
		return super.newValue(type);
	}
	
	@Override
	public Value newOperation(AbstractInsnNode insn) {
		// no arguments
		return super.newOperation(insn);
	}
	
	@Override 
	public Value naryOperation(AbstractInsnNode insn, List<? extends Value> values) {
		operands[ instructions.getId(insn) ] = values.size();
		return super.naryOperation(insn, values);
	}
	
}
