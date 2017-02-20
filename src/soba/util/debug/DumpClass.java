package soba.util.debug;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.analysis.Frame;

import soba.core.ClassInfo;
import soba.core.JavaProgram;
import soba.core.MethodInfo;
import soba.core.method.DataDependence;
import soba.core.method.DataFlowEdge;
import soba.core.method.OpcodeString;
import soba.core.method.asm.FastSourceValue;
import soba.util.IntPairProc;
import soba.util.Timer;
import soba.util.files.Directory;
import soba.util.files.IClassList;
import soba.util.files.SingleFile;
import soba.util.files.ZipFile;
import soba.util.graph.IDirectedGraph;

public class DumpClass {

	static Timer timer;
	static int classCount = 0;
	static int methodCount = 0;
	static int failedMethodCount = 0;
	static long instructionCount = 0;
	static long dataflowEdgeCount = 0;
	static boolean enableOutput = true;
	static boolean dumpStackframe = false;
	static boolean dumpParamName = false;
	static boolean dumpTryBlock = false;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		timer = new Timer();
		List<IClassList> files = new ArrayList<IClassList>();
		
		if (args.length == 0) {
			Directory bin = new Directory(new File("bin"));
			Directory lib = new Directory(new File("lib"));
			files.add(bin);
			files.add(lib);
		} else {
			for (String arg: args) {
				if (arg.equals("--disable-output")) {
					enableOutput = false;
				} else if (arg.equals("--output-frame")) {
					dumpStackframe = true;
				} else if (arg.equals("--output-param")) {
					dumpParamName = true;
				} else if (arg.equals("--output-try")) {
					dumpTryBlock = true;
				} else {
					File f = new File(arg);
					if (f.isDirectory()) {
						Directory dir = new Directory(f);
						dir.enableRecursiveZipSearch();
						files.add(dir);
					} else if (ZipFile.isZipFile(f)) {
						ZipFile zip = new ZipFile(f);
						zip.enableRecursiveSearch();
						files.add(zip);
					} else {
						SingleFile file = new SingleFile(f);
						files.add(file);
					}
				}
			}
		}
		
		JavaProgram program = new JavaProgram((IClassList[]) files.toArray(new IClassList[files.size()]));
		for (ClassInfo c: program.getClasses()) {
			processClass(c);
		}
		
		System.err.println("FINISHED: " +  timer.getTotaltime() + " ms");
		System.err.println("#Classes: " +  classCount);
		System.err.println("#Methods: " +  methodCount);
		System.err.println("#Instructions: " + instructionCount);
		System.err.println("#Edges: " + dataflowEdgeCount);
		System.err.println("#Failed: " +  failedMethodCount);
	}
	
	private static void processClass(ClassInfo c) {
		classCount++;
		for (MethodInfo m: c.getMethods()) {
			if (enableOutput) {
				System.out.println(constructMethodNameString(c, m.getMethodNode()));
			}
			
			MethodInfo methodInfo = m;
			if (!methodInfo.hasMethodBody()) continue;

			DataDependence info = methodInfo.getDataDependence();
			
			if (info != null) {
				methodCount++;
				instructionCount += methodInfo.getInstructionCount();
				
				if (dumpParamName) {
					for (int i=0; i<methodInfo.getParamCount(); ++i) {
						System.out.print("  Param ");
						System.out.print(i + 1);
						System.out.print(" ");
						System.out.print(methodInfo.getParamType(i));
						System.out.print(" ");
						System.out.println(methodInfo.getParamName(i));
					}
				}

				
				for (int i=0; i<methodInfo.getInstructionCount(); ++i) {
					if (enableOutput) {
						System.out.println("  " + OpcodeString.getInstructionString(m.getMethodNode(), i));	
					}
					if (dumpStackframe) dumpStackframe(info, i);
				}
				
				if (dumpTryBlock) {
					if (m.getMethodNode().tryCatchBlocks != null) {
						System.out.println("  Try-catch Table:");
						for (TryCatchBlockNode node: m.getMethodNode().tryCatchBlocks) {
							System.out.println("    start=" + OpcodeString.getLabelString(m.getMethodNode(), node.start) + ", end=" + OpcodeString.getLabelString(m.getMethodNode(), node.end) + ", handler=" + OpcodeString.getLabelString(m.getMethodNode(), node.handler) + " (" + node.type + ")");
						}
						System.out.println("  Try-catch Table End");
					}
				}
				
				List<DataFlowEdge> edges = info.getEdges();
				if (enableOutput) {
					for (DataFlowEdge e: edges) {
						System.out.print("    ");
						System.out.print(e.toString());
						System.out.print("  ");
//						ILocalVariableInfo v = info.getLocalVariable(e);
//						if (v != null) {
//							System.out.print(v.getName() + ": " + v.getDescriptor());
//						}
						String variableName = info.getVariableName(e);
						if (variableName != null) {
							System.out.print(variableName + ": " + info.getVariableDescriptor(e));
						}
						System.out.println();
					}
				}
				dataflowEdgeCount += edges.size();
				
				IDirectedGraph cflow = methodInfo.getControlFlow();
				if (enableOutput) {
					cflow.forEachEdge(new IntPairProc() {
						@Override
						public boolean execute(int elem1, int elem2) {
							System.out.println("    [CFLOW] " + elem1 + " -> " + elem2);
							return true;
						}
					});
				}
				
				IDirectedGraph cdepends = methodInfo.getControlDependence();
				if (enableOutput) {
					cdepends.forEachEdge(new IntPairProc() {
						@Override
						public boolean execute(int elem1, int elem2) {
							System.out.println("    [Control] " + elem1 + " -> " + elem2);
							return true;
						}
					});
				}
				
			} else {
				failedMethodCount++;
			}
		}
	}
	
	private static void dumpStackframe(DataDependence info, int i) {
		Frame<?> f = info.getFrame(i);
		if (f == null) {
			if (enableOutput) {
				System.out.print("    ");
				System.out.print(i);
				System.out.println(" [STACK] null");
			}
		} else {
			int size = f.getStackSize();
			for (int s=0; s<size; ++s) {
				FastSourceValue source = (FastSourceValue)f.getStack(s);
				for (int pos: source.getInstructions()) {
					if (enableOutput) {
						System.out.print("    ");
						System.out.print(i);
						System.out.print(" [STACK] <");
						System.out.print(s);
						System.out.print("/");
						System.out.print(size);
						System.out.print("> ");
						System.out.print(pos);
						System.out.print(": ");
						if (pos >= 0) {
							System.out.println(info.getInstruction(pos).toString());
						} else {
							System.out.println("method param");
						}
					}
				}
			}
			int locals = f.getLocals();
			for (int l=0; l<locals; ++l) {
				FastSourceValue source = (FastSourceValue)f.getLocal(l);
				for (int pos: source.getInstructions()) {
					if (enableOutput) {
						System.out.print("    ");
						System.out.print(i);
						System.out.print(" [LOCAL] <");
						System.out.print(l);
						System.out.print("/");
						System.out.print(locals);
						System.out.print("> ");
						System.out.print(pos);
						System.out.print(": ");
						if (pos >= 0) {
							System.out.println(info.getInstruction(pos).toString());
						} else {
							System.out.println("method param");
						}
					}
				}
			}

			int consumeStack = info.getOperandCount(i);
			int maxStack = f.getStackSize();
			for (int s=0; s<consumeStack; ++s) {
				FastSourceValue source = (FastSourceValue)f.getStack(maxStack-consumeStack+s);
				for (int pos: source.getInstructions()) {
					if (enableOutput) {
						System.out.print("    ");
						System.out.print(i);
						System.out.print(" [OPERAND] <");
						System.out.print(s);
						System.out.print("/");
						System.out.print(consumeStack);
						System.out.print("> ");
						System.out.print(pos);
						System.out.print(": ");
						if (pos >= 0) {
							System.out.println(info.getInstruction(pos).toString());
						} else {
							System.out.println("method param");
						}
					}
				}
			}
		}
		int[][] def = info.getDataDefinition(i);
		if (enableOutput) {
			for (int operandIndex=0; operandIndex<def.length; ++operandIndex) {
				System.out.print("    Operand " + Integer.toString(operandIndex) + " Data Dependence: ");
				if (def[operandIndex] != null) {
					if (def[operandIndex].length == 0) {
						System.out.print("EMPTY");
					} else {
						for (int fromIndex=0; fromIndex<def[operandIndex].length; ++fromIndex) {
							if (fromIndex>0) System.out.print(", ");
							System.out.print(def[operandIndex][fromIndex]);
						}
					}
				} else {
					System.out.print("null");
				}
				System.out.println();
			}
		}
	}		
	
	private static String constructMethodNameString(ClassInfo c, MethodNode m) {
		StringBuilder buf = new StringBuilder();
		buf.append(c.getClassName());
		buf.append("#");
		buf.append(m.name);
		buf.append("#");
		buf.append(m.desc);
		buf.append("#");
		buf.append(m.signature);
		if ((m.access & Opcodes.ACC_SYNTHETIC) != 0) {
			buf.append(" [Synthetic]");
		}
		
		return buf.toString();
	}
	
	@SuppressWarnings({ "unused" } ) 
	private static void dumpVariables(MethodNode m) {
		List<LocalVariableNode> variables = m.localVariables;
		if (variables != null) {
			System.out.println("---VARIABLES BEGIN---");
			for (LocalVariableNode v: variables) {
				System.out.println("   " + v.name + ": " + v.desc + " ");
			}
			System.out.println("---VARIABLES END---");
		}
	}
	
}
