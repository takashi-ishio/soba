package soba.core.method;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.analysis.Frame;

import soba.core.method.asm.DataFlowAnalyzer;
import soba.core.method.asm.FastSourceInterpreter;
import soba.core.method.asm.FastSourceValue;
import soba.util.IntPairList;
import soba.util.ObjectIdMap;
import soba.util.graph.DirectedGraph;

/**
 * This class has data dependence information in a single method.
 */
public class DataDependence {

	private ObjectIdMap<AbstractInsnNode> instructions;
	private DataFlowAnalyzer analyzer;
	private LocalVariables locals;
	
	private List<DataFlowEdge> dataFlowEdges;
	private List<DataFlowEdge> dataFlowEdgesSourceOrder;
	
	/**
	 * Creates a new <code>DataDependence</code> instance.
	 * @param instructions are instructions in the method.
	 * @param analyzer
	 */
	public DataDependence(ObjectIdMap<AbstractInsnNode> instructions, DataFlowAnalyzer analyzer) {
		this.instructions = instructions;
		this.analyzer = analyzer;
		computeEdges();
	}
	
	/**
	 * Returns a graph representing data-dependencies in a single method
	 * Note: This graph does not contain data dependence edges from formal parameters of the method.
	 */
	public DirectedGraph getDependenceGraph() {
		IntPairList edges = new IntPairList(dataFlowEdges.size());
		for (DataFlowEdge e: dataFlowEdges) {
			if (e.getSourceInstruction() != FastSourceInterpreter.METHOD_ENTRY) {
				edges.add(e.getSourceInstruction(), e.getDestinationInstruction());
			}
		}
		return new DirectedGraph(instructions.size(), edges);
	}
	
	/**
	 * @return a list of data flow edges.
	 * The edges are sorted by their destination instructions.
	 */
	public List<DataFlowEdge> getEdges() {
		return dataFlowEdges;
	}
	
	/**
	 * @return a list of data flow edges.
	 * The edges are sorted by their source instructions.
	 */
	public List<DataFlowEdge> getEdgesInSourceOrder() {
		return dataFlowEdgesSourceOrder;
	}
	
	/**
	 * @return a <code>LocalVariables</code> object.
	 */
	public LocalVariables getLocalVariables() {
		if (locals == null) {
			locals = new LocalVariables(this, analyzer.getAnalyzedMethod());
		}
		return locals;
	}
	
	public String getVariableName(DataFlowEdge e) {
		LocalVariables locals = getLocalVariables();
		if (e.isLocal()) {
			if (e.getSourceInstruction() != FastSourceInterpreter.METHOD_ENTRY) {
				int sourceIndex = locals.findEntryForInstruction(e.getSourceInstruction());
				String sourceName = locals.getVariableName(sourceIndex);
				if (sourceName != null) {
					return sourceName;
				}
			}
			int destinationIndex = locals.findEntryForInstruction(e.getDestinationInstruction());
			String destinationName = locals.getVariableName(destinationIndex);
			if (destinationName == null) {
				return e.getVariableIndex() + "_unavailable";
			}
			return destinationName;
		} else {
			return null;
		}
	}
	
	public String getVariableDescriptor(DataFlowEdge e) {
		LocalVariables locals = getLocalVariables();
		if (e.isLocal()) {
			if (e.getSourceInstruction() != FastSourceInterpreter.METHOD_ENTRY) {
				int sourceIndex = locals.findEntryForInstruction(e.getSourceInstruction());
				String sourceDescriptor = locals.getDescriptor(sourceIndex);
				if (sourceDescriptor != null) {
					return sourceDescriptor;
				}
			}
			int destinationIndex = locals.findEntryForInstruction(e.getDestinationInstruction());
			String destinationDescriptor = locals.getDescriptor(destinationIndex);
			if (destinationDescriptor == null) {
				return "null";
			}
			return destinationDescriptor;
		} else {
			return null;
		}
	}
	
//	/**
//	 * Return a local variable corresponding to a data-flow edge.
//	 * @param e specifies a data-flow edge.
//	 * The edge must be returned by the same DataFlowInfo object.
//	 * @return a local variable information.
//	 * If the edge is caused by an operand stack, null is returned.
//	 */
//	public ILocalVariableInfo getLocalVariable(DataFlowEdge e) {
//		if (e.isLocal()) {
//			ILocalVariableInfo v1;
//			if (e.getSourceInstruction() == FastSourceInterpreter.METHOD_ENTRY) {
//				locals.getVariableName(e.getVariableIndex());
//				v1 = localVariables.getParam(e.getVariableIndex());
//			} else {
//				v1  = localVariables.getAccessedVariable(e.getSourceInstruction());
//			}
//			if (v1 != null && !v1.isAnonymous()) {
//				return v1;
//			}
//			ILocalVariableInfo v2 = localVariables.getAccessedVariable(e.getDestinationInstruction());
//			if (v2 != null && !v2.isAnonymous() || v1 == null) {
//				return v2;
//			}
//			// v1.isAnonymous AND (v2 == null OR v2.isAnonymous)
//			return v1;
//		} else {
//			return null;
//		}
//	}
	
	/**
	 * Returns a list of data-definition vertices for each operand used by the specified instruction.
	 * @param instructionIndex specifies an instruction using an operand stack.
	 * @return a two-dimensional array.
	 * array[operandIndex] indicates a list of instructions that defined the operand.
	 * The result is consistent with a return value of getEdges().
	 */
	public int[][] getDataDefinition(int instructionIndex) {
		if (useStack(instructionIndex)) {
			int operands =  analyzer.getOperandCount(instructionIndex);
			int[][] operandDef = new int[operands][];
			for (int i=0; i<operands; ++i) {
				Frame<?> f = analyzer.getFrames()[instructionIndex];
				FastSourceValue value = (FastSourceValue)f.getStack(f.getStackSize() - operands + i);
				operandDef[i] = value.getInstructions();
			}
			return operandDef;
		} else {
			AbstractInsnNode to = instructions.getItem(instructionIndex);
			if (referLocal(instructionIndex)) {
				int localIndex = OpcodeString.getVarIndex(to);
				Frame<?> f = analyzer.getFrames()[instructionIndex];
				if (f != null) {
					FastSourceValue value = (FastSourceValue)f.getLocal(localIndex);
					int[][] localDef = new int[1][];
					localDef[0] = value.getInstructions();
					return localDef;
				} else {
					// To avoid a problem caused by certain methods including many JSRs
					return new int[0][0];
				}
			}
		}
		return new int[0][];
	}
	
	/**
	 * @param destinationInstruction is an instruction index value.
	 * @return a list of data flow edges which destination is specified.
	 */
	public List<DataFlowEdge> getIncomingEdges(final int destinationInstruction) {
		List<DataFlowEdge> edges = new ArrayList<>();
		for (DataFlowEdge e: dataFlowEdges) {
			if (e.getDestinationInstruction() == destinationInstruction) {
				edges.add(e);
			}
		}
		return edges;
	}
	
	/**
	 * @param destinationInstruction is an instruction index value.
	 * @param operandIndex is an operand index value in the instruction.
	 * @return a data flow edge which destination and operandIndex is specified.
	 * (Assumed that this incoming edge is just only one)
	 */
	public DataFlowEdge getIncomingEdge(final int destinationInstruction, final int operandIndex) {
		for (final DataFlowEdge dfe : dataFlowEdges) {
			if (dfe.getDestinationInstruction() == destinationInstruction && dfe.getDestinationOperandIndex() == operandIndex) {
				return dfe;
			}
		}
		throw new UnsupportedOperationException();
	}
	
	/**
	 * @param destinationInstruction is an instruction index value.
	 * @param operandIndex is an operand index value in the instruction.
	 * @return a list of data flow edges which destination and operandIndex is specified.
	 */
	public List<DataFlowEdge> getIncomingEdges(final int destinationInstruction, final int operandIndex) {
		List<DataFlowEdge> edges = new ArrayList<>();
		for (DataFlowEdge e: dataFlowEdges) {
			if (e.getDestinationInstruction() == destinationInstruction &&
					e.getDestinationOperandIndex() == operandIndex) {
				edges.add(e);
			}
		}
		return edges;
	}
	
	private void computeEdges() {
		List<DataFlowEdge> edges = new ArrayList<DataFlowEdge>();
		
		for (int instructionIndex=0; instructionIndex<instructions.size(); ++instructionIndex) {
			Frame<?> f = analyzer.getFrames()[instructionIndex];
			if (useStack(instructionIndex)) {
				int operands = analyzer.getOperandCount(instructionIndex);
				for (int opIndex=0; opIndex<operands; ++opIndex) {
					int stackPos = f.getStackSize() - operands + opIndex;
					FastSourceValue value = (FastSourceValue)f.getStack(stackPos);
					for (int from: value.getInstructions()) {
						edges.add(new DataFlowEdge(from, instructionIndex, opIndex, operands, stackPos, false));
					}
				}
			} else if (referLocal(instructionIndex)) {
				AbstractInsnNode to = instructions.getItem(instructionIndex);
				int localIndex = OpcodeString.getVarIndex(to);
				if (f != null) {
					FastSourceValue value = (FastSourceValue)f.getLocal(localIndex);
					for (int from: value.getInstructions()) {
						edges.add(new DataFlowEdge(from, instructionIndex, 0, 1, localIndex, true));
					}
				} else {
					// A frame object is missing for several instructions in certain methods including many JSRs.
					// We skip the data-flow edges for the "unknown" sources.
					// edges.add(new DataFlowEdge(65536, instructionIndex, 0, 1, localIndex, true));
				}
			}
		}
		List<DataFlowEdge> sourceOrder = new ArrayList<DataFlowEdge>(edges.size());
		sourceOrder.addAll(edges);
		Collections.sort(sourceOrder, new DataFlowEdge.SourceComparator());
		dataFlowEdgesSourceOrder = sourceOrder;
		dataFlowEdges = edges;
	}		

	/**
	 * @return true if the specified instruction refers to operands on a stack.
	 */
	public boolean useStack(int instructionIndex) {
		return analyzer.getOperandCount(instructionIndex) > 0;
	}

	/**
	 * @return true if the specified instruction refers to a local variable.
	 */
	private boolean referLocal(int instructionIndex) { 
		return OpcodeString.isLocalReferenceOperation(instructions.getItem(instructionIndex));
	}
	
	/**
	 * @return an instruction object.
	 */
	public AbstractInsnNode getInstruction(int index) {
		return instructions.getItem(index);
	}
	
	/**
	 * @param instructionIndex specifies an instruction.
	 * @return the number of operands used by the specified instruction.
	 * For an instruction that uses a local variable,
	 * 1 is returned.
	 */
	public int getOperandCount(int instructionIndex) {
		return analyzer.getOperandCount(instructionIndex);
	}
	
	/**
	 * Returns a state of Frame at a specified instruction.
	 * Frame represents the current state of a operand stack and a local variable table.
	 * @param instructionIndex specifies an instruction.
	 * @return Frame object.  The return value may be null if 
	 * control-flow analysis somewhat failed. 
	 * (It is rarely occurs for certain methods.)
	 */
	public Frame<?> getFrame(int instructionIndex) {
		return analyzer.getFrames()[instructionIndex];
	}
}
