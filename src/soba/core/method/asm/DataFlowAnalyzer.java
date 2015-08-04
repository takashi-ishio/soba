package soba.core.method.asm;

import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.Value;

import soba.util.IntPairList;
import soba.util.IntPairSet;
import soba.util.IntPairUtil;

public class DataFlowAnalyzer extends Analyzer<Value> {

	private MethodNode method;
    private IntPairSet controlFlow = new IntPairSet();
    private IntPairSet exceptionalFlow = new IntPairSet();
    private DataFlowInterpreter interpreter;

	public DataFlowAnalyzer(DataFlowInterpreter interpreter) {
		super(interpreter);
		this.interpreter = interpreter;
	}
	
	@Override
	public Frame<Value>[] analyze(String owner, MethodNode m) throws AnalyzerException {
		this.method = m;
		return super.analyze(owner, m);
	}
	
	@Override
	protected void newControlFlowEdge(int insn, int successor) {
		controlFlow.add(insn, successor);
		super.newControlFlowEdge(insn, successor);
	}
	
	@Override
	protected boolean newControlFlowExceptionEdge(int insn, int successor) {
		exceptionalFlow.add(insn, successor);
		return super.newControlFlowExceptionEdge(insn, successor);
	}
	
	public MethodNode getAnalyzedMethod() {
		return method;
	}
	
	public IntPairList getNormalControlFlow() {
		return IntPairUtil.createList(controlFlow);
	}
	
	public IntPairList getConservativeControlFlow() {
		return IntPairUtil.createList(controlFlow, exceptionalFlow);
	}
	
	public int getOperandCount(int instructionIndex) {
		return interpreter.getOperandCount(instructionIndex);
	}
	
}
