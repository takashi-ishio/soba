package soba.core.method;

import java.util.Comparator;

import soba.core.method.asm.FastSourceInterpreter;

/**
 * This class represents a data flow edge.
 */
public class DataFlowEdge {

	/**
	 * A field specifies an instruction.
	 */
	private int from;
	private int to;
	private int operandIndex;
	private int operandCount;
	private int variableIndex;
	private boolean isLocal;
	
	/**
	 * Creates a new <code>DataFlowEdge</code> instance.
	 * @param from is index value of a source instruction.
	 * @param to is index value of a destination instruction.
	 * @param operandIndex is a operand index value in the instruction.
	 * @param operandCount is the number of a operand.
	 * @param variableIndex is a index value of a local variable table or operand stack.
	 * @param isLocal is true if the instructions access a local variable.
	 */
	public DataFlowEdge(int from, int to, int operandIndex, int operandCount, int variableIndex, boolean isLocal) {
		assert operandIndex < operandCount;
		
		this.from = from;
		this.to = to;
		this.operandIndex = operandIndex;
		this.operandCount = operandCount;
		this.variableIndex = variableIndex;
		this.isLocal = isLocal;
	}
	
	/**
	 * @return the index value of an instruction which produces data.
	 * This method returns -1 if the definition instruction cannot be resolved.  
	 * For example, a catch clause starts with ASTORE instruction whose data source cannot be resolved. 
	 * If isParameter() is true, it is a parameter from outside of the method.
	 */
	public int getSourceInstruction() {
		return from;
	}
	
	/**
	 * @return the index value of an instruction which consumes data.
	 */
	public int getDestinationInstruction() {
		return to;
	}
	
	/**
	 * @return the index value in the operand stack.
	 */
	public int getDestinationOperandIndex() {
		return operandIndex;
	}
	
	/**
	 * @return the number of the operand in the destination instruction.
	 */
	public int getDestinationOperandCount() {
		return operandCount;
	}
	
	/**
	 * @return a variable index pointing to an entry in 
	 * a local variable table or a operand stack.
	 */
	public int getVariableIndex() {
		return variableIndex;
	}
	
	/**
	 * @return true if the source instruction is a formal parameter.
	 */
	public boolean isParameter() {
		return isLocal && (from == FastSourceInterpreter.METHOD_ENTRY);
	}
	
	/**
	 * @return true if the edge represents a data-flow through a local variable.
	 * The method returns false for a data-flow edge for an operand stack.
	 */
	public boolean isLocal() {
		return isLocal;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(64);
		if (isParameter()) {
			builder.append("PARAM");
		} else {
			builder.append(from);
		}
		builder.append(" -> ");
		builder.append(to);
		if (operandCount > 1) {
			builder.append(" [");
			builder.append(operandIndex + 1);
			builder.append("/");
			builder.append(operandCount);
			builder.append("]");
		}
		if (isLocal) {
			builder.append(" (LOCAL:");
		} else {
			builder.append(" (STACK:");
		}
		builder.append(variableIndex);
		builder.append(")");
		return builder.toString();
	}
	
	private static int compareVariable(DataFlowEdge o1, DataFlowEdge o2) {
		if (o1.operandIndex == o2.operandIndex) {
			if (o1.variableIndex == o2.variableIndex) {
				if (o1.isLocal == o2.isLocal) {
					return 0;
				} else {
					if (o1.isLocal) return 1;
					else return -1;
				}
			} else {
				return o1.variableIndex - o2.variableIndex;
			}
		} else {
			return o1.operandIndex - o2.operandIndex;
		}
	}
	
	public static class SourceComparator implements Comparator<DataFlowEdge> {
		
		@Override
		public int compare(DataFlowEdge o1, DataFlowEdge o2) {
			if (o1.from == o2.from) {
				if (o1.to == o2.to) {
					return compareVariable(o1, o2);
				} else {
					return o1.to - o2.to;
				}
			} else {
				return o1.from - o2.from;
			}
		}
	}
	
	public static class DestinationComparator implements Comparator<DataFlowEdge> {
		
		@Override
		public int compare(DataFlowEdge o1, DataFlowEdge o2) {
			if (o1.to == o2.to) {
				if (o1.from == o2.from) {
					return compareVariable(o1, o2);
				} else {
					return o1.from - o2.from;
				}
			} else {
				return o1.to - o2.to;
			}
		}
	}
}
