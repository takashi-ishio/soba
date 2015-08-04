package soba.core.method;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.hash.TIntHashSet;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import soba.core.signature.TypeResolver;
import soba.util.IntPairProc;
import soba.util.IntPairSet;


/**
 * An instance of LocalVariables maintains a list of 
 * local variable entries.
 * Each entry corresponds to a set of def-use chains 
 * that share the same instructions.
 * 
 * A variable may be represented by two or more entries
 * if the variable is re-used for several "independent"
 * def-use chains.
 */
public class LocalVariables {
	
	private ArrayList<Entry> entries;
	private MethodNode m;

	/**
	 * Creates a new <code>LocalVariables</code> instance.
	 * @param dataDependence is a <code>DataDependence</code> object.
	 * @param node
	 */
	public LocalVariables(DataDependence dataDependence, MethodNode node) {
		this.m = node;
		
		// Create entries based on data-flow edges.
		// Each entry is a set of data-flow edges that have some common def/use instructions.
		ArrayList<Entry> entries = new ArrayList<Entry>();
		for (DataFlowEdge edge: dataDependence.getEdges()) {
			if (edge.isLocal()) {
				int[] index = findEntries(entries, edge);
				if (index[0] != -1) {
					Entry e1 = entries.get(index[0]);
					if (index[1] == -1) {
						e1.add(edge);
						checkObjectFlag(e1, dataDependence, edge);
					} else if (index[1] != index[0]) {
						Entry e2 = entries.remove(index[1]);
						e1.merge(e2);
					} else { 
						// index[0] == index[1]; both instructions are already involved in a single entry.  
						checkObjectFlag(e1, dataDependence, edge);
					}
				} else {
					// Both instructions are not included in an entry.
					// Create a new entry for the edge.
					Entry e = new Entry(edge); 
					entries.add(e);
					checkObjectFlag(e, dataDependence, edge);
				}
				
			}
		}
		
		// Associate local variable nodes to entries.
		List<?> variables = node.localVariables;
		for (int i=0; i<variables.size(); ++i) {
			LocalVariableNode var = (LocalVariableNode)variables.get(i);
			for (Entry e: entries) {			
				if (e.isDataflowOf(var)) {
					e.addLocalVariableNode(var);
				}
			}
		}

		this.entries = entries;
		
		// Add entries for STORE instructions without LOAD instructions.
		for (int i=0; i<node.instructions.size(); ++i) {
			AbstractInsnNode instruction = node.instructions.get(i);
			if (OpcodeString.isStoreOperation(instruction) || OpcodeString.isLoadOperation(instruction)) {
				int entry = findEntryForInstruction(i);
				if (entry == -1) {
					Entry e = new Entry(i, (VarInsnNode)instruction);
					this.entries.add(e);
				}
			}
		}
	}
	
	private void checkObjectFlag(Entry e, DataDependence dataflow, DataFlowEdge edge) {
		AbstractInsnNode varNode = dataflow.getInstruction(edge.getDestinationInstruction());
		if (varNode.getOpcode() == Opcodes.ALOAD) {
			e.setObjectTypeEntry(true);
		}
		if (edge.getSourceInstruction() >= 0) {
			varNode = dataflow.getInstruction(edge.getSourceInstruction());
			if (varNode.getOpcode() == Opcodes.ASTORE) {
				e.setObjectTypeEntry(true);
			} else if (edge.getDestinationOperandIndex() == 0 && 
						(varNode.getOpcode() == Opcodes.AALOAD ||
						varNode.getOpcode() == Opcodes.AASTORE)) {
				e.setArrayTypeEntry(true);
			}
		}
	}
	
	/**
	 * @return the number of the variable entries.
	 * It may be larger than the number of variables in the method. 
	 */
	public int getVariableEntryCount() {
		return entries.size();
	}
	
	/**
	 * @param entryIndex specifies a local variable.
	 * @return a variable name.
	 */
	public String getVariableName(int entryIndex) {
		return entries.get(entryIndex).getVariableName();
	}
	
	/**
	 * @param entryIndex specifies a local variable.
	 * @return a descriptor of the specified variable.
	 */
	public String getDescriptor(int entryIndex) {
		return entries.get(entryIndex).getDesc();
	}
	
	/**
	 * @param entryIndex specifies a local variable.
	 * @return the type name.  The value becomes null 
	 * if the type name is not included in the analyzed classfile.
	 */
	public String getVariableType(int entryIndex) {
		return entries.get(entryIndex).getTypeName();
	}
	
	/**
	 * @param entryIndex specifies a local variable.
	 * @return the index value in the local variable table.
	 */
	public int getVariableIndex(int entryIndex) {
		return entries.get(entryIndex).getVariableIndex();
	}
	
	/**
	 * @param entryIndex specifies a local variable.
	 * @return true if the variable is an object type.
	 */
	public boolean isObjectVariable(int entryIndex) {
		return entries.get(entryIndex).isObjectType();
	}
	
	/**
	 * @param entryIndex specifies a local variable.
	 * @return true if the variable is an array.
	 */
	public boolean isArrayVariable(int entryIndex) {
		return entries.get(entryIndex).isArrayType();
	}
	
	/**
	 * @param entryIndex specifies a local variable.
	 * @return true if there is no instruction which uses the variable value.
	 */
	public boolean hasNoDataDependence(int entryIndex) {
		return entries.get(entryIndex).isAlone();
	}
	
	/**
	 * @param entryIndex specifies a variable entry.
	 * @return true if the entry corresponds to a method parameter.
	 * Please note that this method returns false
	 * for a parameter whose value must be overwritten 
	 * by another instruction.
	 */
	public boolean isParameter(int entryIndex) {
		return entries.get(entryIndex).isParameter();
	}
	
	/**
	 * @param instructionIndex specifies an instruction.
	 * @return the index value in the variable entries.
	 * The variable is accessed by the specified instruction.
	 */
	public int findEntryForInstruction(int instructionIndex) {
		for (int i=0; i<entries.size(); ++i) {
			Entry e = entries.get(i);
			if (e.containsSource(instructionIndex) ||
				e.containsDestination(instructionIndex)) {
				return i;
			}
		}
		return -1;
	}
	
	
	/**
	 * Returns a pair of indices that indicate a pair of Entries
	 * containing the specified values.
	 * The return_value[0] specifies a set containing value1,
	 * while the return_value[1] specifies a set containing value2.
	 * If no set contains value1, 
	 * return_value is a pair (a set containing value2, -1).
	 * If no values are found in the sets, (-1, -1) is returned.
	 * @return 
	 */
	private static int[] findEntries(ArrayList<Entry> entries, DataFlowEdge edge) {
		int[] ret = new int[] {-1, -1};
		for (int i=0; i<entries.size(); ++i) {
			Entry e = entries.get(i);
			if (e.isConnected(edge)) {
				if (ret[0] == -1) {
					ret[0] = i;
				} else if (ret[1] == -1) {
					ret[1] = i;
					return ret;
				}
			}
		}
		return ret;
	}
	
	private class Entry {
		
		private TIntHashSet defs;
		private TIntHashSet refs;
		private IntPairSet refWithOperands;
		private int variableIndex;
		private boolean isObjectType;
		private boolean isArrayType;
		private String typeName;
		private String typeNameWithGenerics;
		private String variableName;
		private String desc;
		private ArrayList<LocalVariableNode> variables;
		private boolean isParam;
		private boolean isAlone; // true if there is a store instruction without a load instruction.
		
		private Entry(DataFlowEdge e) {
			this.variableIndex = e.getVariableIndex();
			this.variables = new ArrayList<LocalVariableNode>();
			
			defs = new TIntHashSet();
			defs.add(e.getSourceInstruction());
			refs = new TIntHashSet();
			refs.add(e.getDestinationInstruction());
			refWithOperands = new IntPairSet();
			refWithOperands.add(e.getDestinationInstruction(), e.getDestinationOperandIndex());
			isParam = e.isParameter();
			isAlone = false;
			// isObjectType and isArrayType are set by an external method.
		}

		private Entry(int instructionIndex, VarInsnNode var) {
			assert OpcodeString.isStoreOperation(var) || OpcodeString.isAfterJSR(var) : "A STORE instruction may exist without LOAD instructions. But there are no LOAD instructions without STORE.";
			this.variableIndex = var.var;
			this.variables = new ArrayList<LocalVariableNode>(1);
			defs = new TIntHashSet(2);
			defs.add(instructionIndex);
			refs = new TIntHashSet();
			refWithOperands = new IntPairSet();
			isParam = false;
			isObjectType = var.getOpcode() == Opcodes.ASTORE;
			isAlone = true;
		}
		

		public boolean isParameter() {
			return defs.contains(-1);
		}
		
		public boolean isAlone() {
			return isAlone;
		}
		
		private void setObjectTypeEntry(boolean value) { 
			this.isObjectType = value;
		}

		private void setArrayTypeEntry(boolean value) { 
			this.isArrayType = value;
		}

		private void add(DataFlowEdge e) {
			assert this.variableIndex == e.getVariableIndex();
			defs.add(e.getSourceInstruction());
			refs.add(e.getDestinationInstruction());
			refWithOperands.add(e.getDestinationInstruction(), e.getDestinationOperandIndex());
			isParam = isParam ||  e.isParameter();
		}
		
		private void merge(Entry another) {
			assert this.variableIndex == another.variableIndex; 
			
			defs.addAll(another.defs.toArray());
			another.refWithOperands.foreach(new IntPairProc() {
				@Override
				public boolean execute(int instructionIndex, int operand) {
					refs.add(instructionIndex);
					refWithOperands.add(instructionIndex, operand);
					return true;
				}
			});
			if (another.isObjectType) {
				this.isObjectType = another.isObjectType;
			}
		}
		
		private boolean isDataflowOf(LocalVariableNode var) {
			if (this.variableIndex == var.index) {
				
				for (TIntIterator it=defs.iterator(); it.hasNext(); ) {
					int def = it.next();
					if (0 <= def &&  def < m.instructions.size()) {
						AbstractInsnNode defNode = m.instructions.get(def);
						if (OpcodeString.isAccess(defNode, var)) {
							return true;
						}
					} else {
						if (var.start == m.instructions.getFirst()) {
							return true;
						}
					}
				}

				for (TIntIterator it=refs.iterator(); it.hasNext(); ) {
					int ref = it.next();
					AbstractInsnNode refNode = m.instructions.get(ref);
					if (OpcodeString.isAccess(refNode, var)) {
						return true;
					}
				}
				return false;
				
			} else {
				return false;
			}
			
		}
		
		private void addLocalVariableNode(LocalVariableNode node) {
			variables.add(node);
			if (variableName == null) {
				variableName = node.name;
			} else {
				assert node.name == null || variableName.equals(node.name): "Combined " + variableName.toString() + " and " + node.name;
			}
			if (typeName == null) {
				if (node.desc != null) {
					desc = node.desc;
					typeName = TypeResolver.getTypeName(node.desc);
				}
			} else {
				assert node.desc == null || typeName.equals(TypeResolver.getTypeName(node.desc));
			}
			if (typeNameWithGenerics == null) {
				if (node.signature != null) {
					typeNameWithGenerics = TypeResolver.getTypeName(node.signature);
				}
			} else {
				assert node.signature == null || typeNameWithGenerics.equals(TypeResolver.getTypeName(node.signature));
			}
		}
		
		/**
		 * @param edge
		 * @return edge is connected with some vertices in the entry.
		 */
		private boolean isConnected(DataFlowEdge edge) {
			return this.variableIndex == edge.getVariableIndex() &&
				containsSource(edge.getSourceInstruction()) ||
				containsDestinationOperand(edge.getDestinationInstruction(), edge.getDestinationOperandIndex());
		}
		
		/**
		 * @param instruction
		 * @return true if the entry contains the specified 
		 * source instruction.
		 */
		private boolean containsSource(int instruction) {
			return defs.contains(instruction);
		}

		/**
		 * @param instruction
		 * @return true if the entry contains the specified 
		 * destination instruction.
		 */
		private boolean containsDestination(int instruction) {
			return refWithOperands.containsFirst(instruction);
		}

		/**
		 * @param instruction
		 * @return true if the entry contains the specified 
		 * destination instruction.
		 */
		private boolean containsDestinationOperand(int instruction, int operandIndex) {
			return refWithOperands.contains(instruction, operandIndex);
		}

		private boolean isObjectType() {
			return isObjectType;
		}
		
		private boolean isArrayType() {
			return isArrayType;
		}
		
		private int getVariableIndex() {
			return variableIndex;
		}
		
		private String getTypeName() {
			return typeName;
		}
		
		private String getVariableName() {
			return variableName;
		}
		
		private String getDesc() {
			return desc;
		}

	}
}
