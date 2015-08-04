package soba.core.vta;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TIntObjectProcedure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.Opcodes;

import soba.core.ClassHierarchy;
import soba.core.ClassInfo;
import soba.core.FieldInfo;
import soba.core.IDynamicBindingResolver;
import soba.core.JavaProgram;
import soba.core.MethodInfo;
import soba.core.method.CallSite;
import soba.core.method.DataDependence;
import soba.core.signature.TypeConstants;
import soba.core.signature.TypeResolver;
import soba.util.IntPairList;
import soba.util.graph.DirectedAcyclicGraph;
import soba.util.graph.DirectedGraph;

public class VTAResolver implements IDynamicBindingResolver {
	
	public static int VERTEX_ERROR = 0;
	private static final String ARRAY_SUFFIX = "[]";

	private Map<FieldInfo, FieldVertex> fieldVertex;
	private Map<MethodInfo, CallSiteVertices[]> callsiteMap; // methodInfo * instructionIndex -> callsite
	private Map<MethodInfo, NewVertices> newVerticesMap;
	private Map<MethodInfo, MethodVertices> localVerticesMap;
	private TIntObjectHashMap<String> catchVariableVertices;
	
	private ArrayList<String> declaredTypeNames; // vertex ID -> type name (constraint) of the vertex.
	
	private ClassHierarchy hierarchy;
	private IAnalysisTarget target;
	
	private IntPairList edges;
	
	private TypeSet[] reachingTypes;

	private TypeSetManager typeSetManager;
	
	/**
	 * Creates a new <code>VTAResolver</code> instance.
	 * All methods and fields in the program are analyzed.
	 * @param program
	 */
	public VTAResolver(final JavaProgram program) {
		this(program, new IAnalysisTarget() {
			@Override
			public boolean isTargetMethod(MethodInfo m) {
				return true;
			}
			
			@Override
			public boolean isTargetField(FieldInfo f) {
				return true;
			}
			
			@Override
			public boolean isExcludedType(String className) {
				return false;
			}
			
			@Override
			public boolean assumeExternalCallers(MethodInfo m) {
				return false;
			}
		});
	}
	
	/**
	 * Creates a new <code>VTAResolver</code> instance.
	 * @param program
	 * @param selector specifies the analysis target in the program.
	 */
	public VTAResolver(final JavaProgram program, final IAnalysisTarget selector) {
		target = selector;
		edges = new IntPairList(65536);
		hierarchy = program.getClassHierarchy();
		
		callsiteMap = new HashMap<MethodInfo, CallSiteVertices[]>();
		newVerticesMap = new HashMap<MethodInfo, NewVertices>();
		fieldVertex = new HashMap<FieldInfo, FieldVertex>();
		localVerticesMap = new HashMap<MethodInfo, MethodVertices>();
		catchVariableVertices = new TIntObjectHashMap<String>();
		declaredTypeNames = new ArrayList<String>(65536);
		List<CallSiteVertices> callsitesWithoutCallees = new ArrayList<CallSiteVertices>(); 
		
		// Create vertices for inter-procedural connection
		fieldVertex = new HashMap<FieldInfo, FieldVertex>();
		int vID = VERTEX_ERROR+1;
		declaredTypeNames.add(TypeSet.DEFAULT_UNKNOWN_TYPE);
		for (ClassInfo c: program.getClasses()) {
			for (int mIndex = 0; mIndex < c.getMethodCount(); mIndex++) {
				MethodInfo m = c.getMethod(mIndex);
				if (m.hasMethodBody() && (selector == null || selector.isTargetMethod(m))) {
					// Create vertices for local variables (including formal parameters)
//					MethodBody body = m.getMethodBody();
					DataDependence dataflow = m.getDataDependence();
					MethodVertices localVertices = new MethodVertices(m, dataflow.getLocalVariables(), vID);
					this.localVerticesMap.put(m, localVertices);
					vID += localVertices.getVertexCount();
					for (int i=0; i<localVertices.getVertexCount(); ++i) {
						declaredTypeNames.add(localVertices.getTypeName(i));
					}
				}
			}
			for (int fIndex = 0; fIndex < c.getFieldCount(); fIndex++) {
				FieldInfo f = c.getField(fIndex);
				if (!TypeConstants.isPrimitiveTypeName(f.getFieldTypeName())) {
					FieldVertex fv = new FieldVertex(f, vID);
					fieldVertex.put(f, fv);
					vID++;
					declaredTypeNames.add(fv.getTypeName());
				}
			}
		}
		
		// Build a type propagation graph
		for (ClassInfo c: program.getClasses()) {
			for (int mIndex = 0; mIndex < c.getMethodCount(); mIndex++) {
				MethodInfo m = c.getMethod(mIndex);
				if (m.hasMethodBody() && (selector == null || selector.isTargetMethod(m))) {
//					MethodBody body = m.getMethodBody();
					DataDependence dataflow = m.getDataDependence();
					
					MethodNode mnode = m.getMethodNode();

					// Create vertices for "new" instructions 
					NewVertices newVertices = new NewVertices(mnode.instructions, vID);
					vID += newVertices.getVertexCount();
					this.newVerticesMap.put(m, newVertices);
					for (int i=0; i<newVertices.getVertexCount(); ++i) {
						declaredTypeNames.add(newVertices.getTypeName(i));
						assert newVertices.getTypeName(i) != null;
					}

					// Create vertices for method invocations.
					// Connect inter-procedural edges.
					CallSiteVertices[] callsites = new CallSiteVertices[m.getInstructionCount()];
					callsiteMap.put(m, callsites);

					for (CallSite callsite: m.getCallSites()) {
						MethodInfo[] methods = hierarchy.resolveCall(callsite);
						CallSiteVertices actuals = new CallSiteVertices(callsite, vID);
						callsites[callsite.getInstructionIndex()] = actuals;
						vID += actuals.getVertexCount();
						for (int i=0; i<actuals.getVertexCount(); ++i) {
							declaredTypeNames.add(actuals.getTypeName(i));
						}

						boolean methodNotIncluded = false;
						if (methods.length > 0) {
							for (MethodInfo called: methods) {
								
								MethodVertices formals = localVerticesMap.get(called);
								if (formals != null) {
									for (int i=0; i<actuals.getParamCount(); ++i) {
										if (actuals.isObjectParam(i)) {
											assert actuals.getParamVertexId(i) != VERTEX_ERROR;
											assert formals.getFormalVertex(i) != VERTEX_ERROR;
											
											addEdge(actuals.getParamVertexId(i), formals.getFormalVertex(i));
										}
									}
									if (actuals.hasReturnValue()) {
										assert formals.getReturnVertex() != VERTEX_ERROR;
										assert actuals.getReturnValueVertex() != VERTEX_ERROR;
										addEdge(formals.getReturnVertex(), actuals.getReturnValueVertex());
									}
								} else {
									// The method may be out of target.
									methodNotIncluded = true;
								}
							}
						}
						if ((methodNotIncluded || methods.length == 0) && actuals.hasReturnValue()) callsitesWithoutCallees.add(actuals);

					}

					
					// Process instructions in a method
					for (int i=0; i<m.getInstructionCount(); ++i) {
						AbstractInsnNode instruction = mnode.instructions.get(i);
						analyzeInstruction(i, instruction, m, dataflow);
					}
				}
			}
		}

		// Construct a graph object
		DirectedGraph graph = new DirectedGraph(vID, edges);
		DirectedAcyclicGraph typePropagationDAG = new DirectedAcyclicGraph(graph);

		this.typeSetManager = new TypeSetManager();
		assignTypes(typePropagationDAG, callsitesWithoutCallees, selector);
		propagateTypes(typePropagationDAG);
	}
	
	
	/**
	 * This method resolves dynamic binding for INVOKEVIRTUAL/INVOKEINTERFACE calls.
	 * It should be noted that this binding does not correctly return binding information
	 * for INVOKESPECIAL and INVOKESTATIC.
	 * @param cs specifies a method call.
	 * @return an array of <code>IMethodInfo</code> objects that are invoked by 
	 * the specified invocation instruction.
	 * The array is a subset of CHA result (returned by <code>ClassHierarchy</code>).
	 * The array excludes unreachable types according to VTA analysis
	 * and a type filter represented by IAnalysisTarget.isExcludedType().
	 */
	@Override
	public MethodInfo[] resolveCall(CallSite cs) {
		int instruction = cs.getInstructionIndex();
		CallSiteVertices[] callsites = callsiteMap.get(cs.getOwnerMethod());
		if (callsites != null) {
			CallSiteVertices params = callsites[instruction];
			if (params != null) {
				HashSet<MethodInfo> called = new HashSet<>();
				int v = params.getParamVertexId(0);
				TypeSet types = reachingTypes[v];
				String methodName = params.getCallSite().getMethodName();
				String methodDesc = params.getCallSite().getDescriptor();
				if (types != null) {
					ArrayList<String> declaredType = new ArrayList<String>();
					declaredType.add(declaredTypeNames.get(v));
					Collection<String> declaredSubtypes = hierarchy.getAllSubtypes(declaredType);
					
					for (int i=0; i<types.getTypeCount(); ++i) {
						String className = types.getType(i);
						if (declaredSubtypes.contains(className)) {
							MethodInfo m = hierarchy.resolveSpecialCall(className, methodName, methodDesc);
							if (m != null && !target.isExcludedType(m.getClassName())) {
								called.add(m);
							}
						}
					}
					
					ArrayList<String> approxTypes = new ArrayList<String>();
					for (int i=0; i<types.getApproximatedTypeCount(); ++i) {
						approxTypes.add(types.getApproximatedType(i));
					}
					Collection<String> subtypes = hierarchy.getAllSubtypes(approxTypes);
					for (String className: subtypes) {
						if (declaredSubtypes.contains(className)) {
							MethodInfo m = hierarchy.resolveSpecialCall(className, methodName, methodDesc);
							if (m != null && !target.isExcludedType(m.getClassName())) {
								called.add(m);
							}
						}
					}
					MethodInfo[] methods = called.toArray(new MethodInfo[0]);
					Arrays.sort(methods, new Comparator<MethodInfo>() {
						@Override
						public int compare(MethodInfo o1, MethodInfo o2) {
							int idx = o1.getClassName().compareTo(o2.getClassName());
							if (idx != 0) return idx;
							
							idx = o1.getMethodName().compareTo(o2.getMethodName());
							if (idx != 0) return idx;

							idx = o1.getDescriptor().compareTo(o2.getDescriptor());
							if (idx != 0) return idx;
							
							return o1.hashCode() - o2.hashCode();
						}
					});
					return methods;
				} else {
					// types == null if the invocation is not processed -- this condition is never satisfied.
					return new MethodInfo[0];
				}
			} else {
				// params == null if the specified instruction is not an invocation.
				return new MethodInfo[0];
			}
		} else {
			// callsites == null if the caller is not included in the analysis.
			return new MethodInfo[0];
		}
	}
	
	/**
	 * Assign types for each vertex.
	 * @param typePropagationDAG
	 */
	private void assignTypes(final DirectedAcyclicGraph typePropagationDAG, List<CallSiteVertices> callsitesWithoutCallees, IAnalysisTarget selector) {
		reachingTypes = new TypeSet[typePropagationDAG.getVertexCount()];
		for (NewVertices vertices: newVerticesMap.values()) {
			for (int i=0; i<vertices.getVertexCount(); ++i) {
				String typeName = extractBaseType(vertices.getTypeName(i));
				int v = vertices.getVertex(i);
				assignSpecificType(typePropagationDAG, v, typeName);
			}
		}
		for (CallSiteVertices vertices: callsitesWithoutCallees) {
			assert vertices.hasReturnValue();
			int v = vertices.getReturnValueVertex();
			assignApproximatedType(typePropagationDAG, v, extractBaseType(vertices.getReturnValueTypeName()));
		}
		// Assign approximated types for parameters from outside
		for (MethodInfo m: localVerticesMap.keySet()) {
			if (selector != null && selector.assumeExternalCallers(m)) {
				MethodVertices methodVertices = localVerticesMap.get(m); 
				for (int i=0; i<m.getParamCount(); ++i) {
					if (methodVertices.hasFormalVertex(i)) {
						int v = methodVertices.getFormalVertex(i);
						assignApproximatedType(typePropagationDAG, v, extractBaseType(m.getParamType(i)));
					}
				}
			}
		}
		// Assign approximated types for exception types in catch blocks
		catchVariableVertices.forEachEntry(new TIntObjectProcedure<String>() {
			@Override
			public boolean execute(int v, String typeName) {
				assignApproximatedType(typePropagationDAG, v, typeName);
				return true;
			}
		});
		// Assign approximated types to fields which are not included in analysis target.
		if (selector != null) {
			for (FieldVertex fv: fieldVertex.values()) {
				int vertexId = fv.getId();
				FieldInfo f = fv.getFieldInfo();
				if (!selector.isTargetField(f)) {
					assignApproximatedType(typePropagationDAG, vertexId, extractBaseType(fv.getTypeName()));
				}
			}
		}
	}
	
	/**
	 * Assign a specific type to a vertex.
	 * @param typePropagationDAG specifies a DAG.
	 * @param v specifies a vertex.
	 * @param typeName specifies a type name.
	 */
	private void assignSpecificType(DirectedAcyclicGraph typePropagationDAG, int v, String typeName) {
		v = typePropagationDAG.getRepresentativeNode(v);
		if (reachingTypes[v] == null) {
			reachingTypes[v] = new TypeSet(typeSetManager, typeName);
		} else {
			reachingTypes[v] = reachingTypes[v].addType(typeName);
		}
	}
	
	/**
	 * Assign an approximated type to a vertex.
	 * @param typePropagationDAG specifies a DAG.
	 * @param v specifies a vertex.
	 * @param typeName specifies a type name.
	 */
	private void assignApproximatedType(DirectedAcyclicGraph typePropagationDAG, int v, String typeName) {
		v = typePropagationDAG.getRepresentativeNode(v);
		if (reachingTypes[v] == null) {
			reachingTypes[v] = TypeSet.createApproximation(typeSetManager, typeName);
		} else {
			reachingTypes[v] = reachingTypes[v].addApproximatedType(typeName);
		}
	}
	

	
	private void propagateTypes(final DirectedAcyclicGraph typePropagationDAG) {
		final DirectedAcyclicGraph reverse = typePropagationDAG.getReverseGraph(); 

		TopologicalOrderSearch.searchFromRoot(typePropagationDAG, new ITopologicalVisitor() {
			
			@Override
			public boolean onVisit(int vertexId) {
				// Don't propagate types through ERROR vertex.
				if (vertexId == VERTEX_ERROR) {
					reachingTypes[vertexId] = new TypeSet(typeSetManager);
					return true;
				}
				

				int[] incoming = reverse.getEdges(vertexId);
				if (incoming.length == 1) {
					if (reachingTypes[vertexId] == null) {
						reachingTypes[vertexId] = reachingTypes[incoming[0]];
					} else {
						ArrayList<TypeSet> types = new ArrayList<TypeSet>();
						types.add(reachingTypes[vertexId]);
						types.add(reachingTypes[incoming[0]]);
						reachingTypes[vertexId] = new TypeSet(typeSetManager, types);
					}
				} else if (incoming.length > 1) {
					// Merge reaching types.
					ArrayList<TypeSet> types = new ArrayList<TypeSet>();
					if (reachingTypes[vertexId] != null) {
						types.add(reachingTypes[vertexId]);
					}
					for (int i=0; i<incoming.length; ++i) {
						assert reachingTypes[incoming[i]] != null;
						types.add(reachingTypes[incoming[i]]);
					}
					reachingTypes[vertexId] = new TypeSet(typeSetManager, types);
				} else {
					assert incoming.length == 0: "Unreachable vertices";
					// Assign an empty set for unreachable vertices.
					if (reachingTypes[vertexId] == null) {
						reachingTypes[vertexId] = new TypeSet(typeSetManager);
					}
				}
				
				return true;
			}
			
			@Override
			public void onFinished() {
				// Vertices in the same SCC share the same TypeSet.
				for (int i=0; i<reachingTypes.length; ++i) {
					int v = typePropagationDAG.getRepresentativeNode(i);
					if (v != i) {
						reachingTypes[i] = reachingTypes[v];
					}
				}
			}
		});
	}
	
	private void analyzeInstruction(int index, AbstractInsnNode instruction, MethodInfo m, DataDependence dataflow) {
		
		switch (instruction.getOpcode()) { 
		case Opcodes.ARETURN:
		{
			int targetVertexId = localVerticesMap.get(m).getReturnVertex();
			int[][] operandSources = dataflow.getDataDefinition(index);
			assert operandSources.length == 1: "ARETURN takes a single parameter.";
			int[] sources = operandSources[0];
			for (int sourceInstructionIndex: sources) {
				for (int sourceId: getSourceVertices(sourceInstructionIndex, m, dataflow)) {
					addEdge(sourceId, targetVertexId);
				}
			}
			break;
		}
		case Opcodes.PUTFIELD:
		case Opcodes.PUTSTATIC:
		{
			FieldVertex v = getFieldVertexId((FieldInsnNode)instruction);
			if (v == null) return;
			int targetVertexId = v.getId();
			int[][] operandSources = dataflow.getDataDefinition(index);
			int[] sources;
			if (instruction.getOpcode() == Opcodes.PUTFIELD) {
				assert operandSources.length == 2: "PUTFIELD takes two parameters (object and value)";
				sources = operandSources[1];
			} else {
				assert operandSources.length == 1: "PUTFIELD takes a parameter (value)";
				sources = operandSources[0];
			}
			for (int sourceInstructionIndex: sources) {
				for (int sourceId: getSourceVertices(sourceInstructionIndex, m, dataflow)) {
					addEdge(sourceId, targetVertexId);
				}
			}
			break;
		}
		case Opcodes.ASTORE:
		{
			int[][] operandSources = dataflow.getDataDefinition(index);
			int[] sources = operandSources[0]; 
			assert operandSources.length == 1: "ASTORE takes a single parameter (value)";
			if (sources.length == 1 && sources[0] == -1) {
				// ASTORE at the beginning of CATCH block has a direct data flow (without ALOAD instruction).
				List<?> blocks = m.getMethodNode().tryCatchBlocks;
				for (int i=0; i<blocks.size(); ++i) {
					TryCatchBlockNode node = (TryCatchBlockNode)blocks.get(i);
					AbstractInsnNode handler = node.handler;
					while (handler != null) {
						if (handler == instruction) {
							// instruction is to store an exception object to a local variable.
							int vertexId = getLocalVariableVertex(m, index);
							String type = node.type;
							if (type == null) type = TypeSet.DEFAULT_UNKNOWN_TYPE;
							catchVariableVertices.put(vertexId, type);
							return;
						} else {
							if (handler.getType() == AbstractInsnNode.LABEL || 
								handler.getType() == AbstractInsnNode.FRAME ||
								handler.getType() == AbstractInsnNode.LINE) {
								handler = handler.getNext();
							} else {
								break; // different try-catch or finally block
							}
						}
					}
				}
				assert false: "A separated ASTORE outside of try-catch blocks.";
			} else {
				int targetVertexId = getLocalVariableVertex(m, index);
				if (targetVertexId != VERTEX_ERROR) { 
					for (int sourceInstructionIndex: sources) {
						for (int sourceId: getSourceVertices(sourceInstructionIndex, m, dataflow)) {
							addEdge(sourceId, targetVertexId);
						}
					}
				} else {
					assert false: "ASTORE must have its corresponding ALOAD.";
				}
			}
			break;
		}
		case Opcodes.INVOKEVIRTUAL:
		case Opcodes.INVOKESPECIAL:
		case Opcodes.INVOKESTATIC:
		case Opcodes.INVOKEDYNAMIC:
		case Opcodes.INVOKEINTERFACE:
		{
			// Create edges for invocations.
			// This process is separated from creation of vertices since
			// vertices of return values must be generated.
			CallSiteVertices actuals = callsiteMap.get(m)[index];
			if (actuals != null) {
				int[][] operandSources = dataflow.getDataDefinition(index);
				assert (operandSources.length == actuals.getParamCount()): "The number of operands must be the same as the number of actual vertices.";
				// In general, the number of operands is the same as actual parameters.
				for (int i=0; i<actuals.getParamCount(); ++i) {
					if (actuals.isObjectParam(i)) {
						int actualId = actuals.getParamVertexId(i);
						for (int sourceInstruction: operandSources[i]) {
							for (int sourceVertexId: getSourceVertices(sourceInstruction, m, dataflow)) {
								addEdge(sourceVertexId, actualId);
							}
						}
					}
				}
			}
			break;
		}
		case Opcodes.AASTORE:
		{
			int[][] operandSources = dataflow.getDataDefinition(index);
			int[] arraySources = operandSources[0];
			int[] valueSources = operandSources[2];
			assert operandSources.length == 3: "AASTORE takes three parameters: object, index and value.";
			for (int valueSourceInstructionIndex: valueSources) {
				for (int sourceId: getSourceVertices(valueSourceInstructionIndex, m, dataflow)) {
					for (int arraySourceInstructionIndex: arraySources) {
						for (int arrayId: getSourceVertices(arraySourceInstructionIndex, m, dataflow)) {
							addEdge(sourceId, arrayId);
						}
					}
				}
			}
			break;
		}
		}
	}
	
	private void addEdge(int sourceVertex, int destinationVertex) {
		edges.add(sourceVertex, destinationVertex);
		
		// If at least one of the vertices is an array type,
		// or both of the vertices are "java.lang.Object", 
		// then connect a back edge to represent an alias.
		String sourceType = declaredTypeNames.get(sourceVertex);
		String destinationType = declaredTypeNames.get(destinationVertex);
		if ((sourceType.endsWith(ARRAY_SUFFIX) || destinationType.endsWith(ARRAY_SUFFIX)) || 
				(sourceType.equals("java/lang/Object") && destinationType.equals("java/lang/Object"))) {
			edges.add(destinationVertex, sourceVertex);
		}
	}
	
	/**
	 * Return a vertex ID representing a local variable accessed by a specified instruction.
	 * @param m
	 * @param instructionIndex
	 * @return
	 */
	private int getLocalVariableVertex(MethodInfo m, int instructionIndex) {
		MethodVertices l = localVerticesMap.get(m);
		if (l != null) {
			return l.getLocalVertex(instructionIndex);
		} else {
			assert false: "MethodVertices is not registered.";
			return VERTEX_ERROR;
		}
	}
	
	/**
	 * @param instructionIndex specifies an instruction 
	 * that generates a value. 
	 * @return vertex IDs that may correspond to a value.  
	 */
	private int[] getSourceVertices(int instructionIndex, MethodInfo m, DataDependence dataflow) {
		assert instructionIndex >= 0: "Instruction must be >=0 : " + Integer.toString(instructionIndex);
		
		InsnList instructions = m.getMethodNode().instructions;
		AbstractInsnNode node = instructions.get(instructionIndex);
		switch (node.getOpcode()) {
		case Opcodes.CHECKCAST:
			{
				int[][] operands = dataflow.getDataDefinition(instructionIndex);
				assert operands.length == 1: "CHECKCAST takes a single parameter.";
				TIntArrayList defs = new TIntArrayList();
				for (int sourceInstruction: operands[0]) {
					defs.add(getSourceVertices(sourceInstruction, m, dataflow));
				}
				return defs.toArray();
			}
		case Opcodes.INVOKEINTERFACE:
		case Opcodes.INVOKESPECIAL:
		case Opcodes.INVOKESTATIC:
		case Opcodes.INVOKEVIRTUAL:
		case Opcodes.INVOKEDYNAMIC:
			CallSiteVertices[] sites = callsiteMap.get(m);
			if (sites[instructionIndex] != null) {
				return new int[] { sites[instructionIndex].getReturnValueVertex() };
			} else {
				return new int[] { VERTEX_ERROR };
			}

		case Opcodes.GETFIELD:
		case Opcodes.GETSTATIC:
			FieldInsnNode f = (FieldInsnNode)node;
			FieldVertex v = getFieldVertexId(f);
			if (v != null) {
				return new int[] { v.getId() };
			} else {
				return new int[0];
			}

		case Opcodes.AALOAD:
			{
				int[][] operands = dataflow.getDataDefinition(instructionIndex);
				assert operands.length == 2: "AALOAD takes two parameters.";
				TIntArrayList defs = new TIntArrayList();
				for (int sourceInstruction: operands[0]) { // arrayRef
					defs.add(getSourceVertices(sourceInstruction, m, dataflow));
				}
				return defs.toArray();
			}
			
		case Opcodes.ALOAD:
			return new int[] { getLocalVariableVertex(m, instructionIndex) };
			
		case Opcodes.MULTIANEWARRAY:
		case Opcodes.ANEWARRAY: 
		case Opcodes.NEW:
			return new int[] { newVerticesMap.get(m).getNewInstructionVertex(instructionIndex) };
			
		default:
			return new int[] { VERTEX_ERROR };
		}
	}
	
	private FieldVertex getFieldVertexId(FieldInsnNode node) {
		String className = node.owner;
		String fieldName = node.name;
		String desc = node.desc;
		String owner;
		if (node.getOpcode() == Opcodes.PUTSTATIC || 
			node.getOpcode() == Opcodes.GETSTATIC) {
			owner = hierarchy.resolveStaticFieldOwner(className, fieldName, desc);
		} else {
			assert node.getOpcode() == Opcodes.PUTFIELD || 
			        node.getOpcode() == Opcodes.GETFIELD;
			owner = hierarchy.resolveInstanceFieldOwner(className, fieldName, desc);
		}
		ClassInfo c = hierarchy.getClassInfo(owner);
		if (c != null) {
			FieldInfo f = c.findField(fieldName, desc);
			if (f != null) {
				FieldVertex fv = fieldVertex.get(f);
				if (fv != null) {
					return fv;
				} else {
					assert TypeConstants.isPrimitiveTypeName(f.getFieldTypeName());
					return null;
				}
			}
		}
		return null;
	}
	
	/**
	 * Return a set of types that may be invoked by a method call.
	 * @param m specifies a caller method.
	 * @param instruction specifies an INVOKE instruction in the caller method.
	 * @return a TypeSet including types that may be assigned to a receiver object.
	 */
	public TypeSet getReceiverTypeAtCallsite(MethodInfo m, int instruction) {
		CallSiteVertices[] callsites = callsiteMap.get(m);
		if (callsites != null) {
			CallSiteVertices params = callsites[instruction];
			if (params != null) {
				if (!params.getCallSite().isStaticMethod()) {
					int v = params.getParamVertexId(0);
					return reachingTypes[v];
				} else {
					return null;
				}
			} else {
				// params == null if no method implementations are included in the analysis.
				return null;
			}
		} else {
			return null;
		}
	}
	
	public TypeSet getMethodParamType(MethodInfo m, int paramIndex) {
		MethodVertices vertices = localVerticesMap.get(m);
		if (vertices.hasFormalVertex(paramIndex)) {
			return reachingTypes[vertices.getFormalVertex(paramIndex)];
		} else {
			return null;
		}
	}
		
	private static class FieldVertex {
		private int vertexID;
		private FieldInfo fieldInfo;
		public FieldVertex(FieldInfo f, int vID) {
			this.vertexID = vID;
			this.fieldInfo= f;
		}
		
		public int getId() {
			return vertexID;
		}
		
		public String getTypeName() {
			return TypeResolver.getTypeName(fieldInfo.getDescriptor());
		}
		
		public FieldInfo getFieldInfo() {
			return fieldInfo;
		}
	}
	
	/**
	 * @param typename represents a type. 
	 * The value may be an array type.
	 * @return If typename is a regular type, 
	 * the typename is returned.
	 * If typename is an array type, 
	 * the return value is a base type of the array.  
	 */
	private String extractBaseType(String typename) {
		assert typename != null;
		while (typename.endsWith("[]")) {
			typename = typename.substring(0, typename.length() - 2);
		}
		assert typename != null;
		return typename;
	}
	
}
