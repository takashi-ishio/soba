package soba.core;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import org.junit.BeforeClass;
import org.junit.Test;

import soba.core.ClassInfo;
import soba.core.JavaProgram;
import soba.core.MethodInfo;
import soba.util.UtilForAssertThat;
import soba.util.graph.DirectedGraph;

public class MethodInfoTest implements ExampleProgram {

	private static JavaProgram program; 
	
	@BeforeClass
	public static void readExampleProgram() {
		program = JavaProgramTest.readExampleProgram();
	}
	
	@Test
	public void testMethodInfo01() {
		ClassInfo c = program.getClassInfo(CLASS_D);
		MethodInfo m = c.findMethod("example", "(IJDLjava/lang/String;)I");
		
		assertThat(m.getPackageName(), is("soba/testdata/inheritance1"));
		assertThat(m.getClassName(), is(CLASS_D));
		assertThat(m.getMethodName(), is("example"));
		assertThat(m.getDescriptor(), is("(IJDLjava/lang/String;)I"));
		assertThat(m.getGenericsSignature(), is(nullValue()));
		assertThat(m.hasMethodBody(), is(true));
		assertThat(m.isLibrary(), is(false));
		assertThat(m.isPrivate(), is(false));
		assertThat(m.isPublic(), is(true));
		assertThat(m.isProtected(), is(false));
		assertThat(m.isStatic(), is(false));
		assertThat(m.isSynthetic(), is(false));
		assertThat(m.isOverridable(), is(true));
		assertThat(m.isPackagePrivate(), is(false));
		assertThat(m.getInstructionCount(), is(5));
		assertThat(m.getParamCount(), is(5));
		assertThat(m.getReturnType(), is("int"));
		assertThat(m.getReceiverObjectParamIndex(), is(0));
		
		assertThat(m.getParamName(0), is("this"));
		assertThat(m.getParamName(1), is("i"));
		assertThat(m.getParamName(2), is("l"));
		assertThat(m.getParamName(3), is("d"));
		assertThat(m.getParamName(4), is("s"));
		assertThat(m.getParamName(5), is(nullValue()));
		assertThat(m.getParamType(0), is(CLASS_D));
		assertThat(m.getParamType(1), is("int"));
		assertThat(m.getParamType(2), is("long"));
		assertThat(m.getParamType(3), is("double"));
		assertThat(m.getParamType(4), is("java/lang/String"));
		assertThat(m.getVariableTableIndexOfParamAt(0), is(0));
		assertThat(m.getVariableTableIndexOfParamAt(1), is(1));
		assertThat(m.getVariableTableIndexOfParamAt(2), is(2));
		assertThat(m.getVariableTableIndexOfParamAt(3), is(4));
		assertThat(m.getVariableTableIndexOfParamAt(4), is(6));
		assertThat(m.isParameterOrderingNumber(0), is(true));
		assertThat(m.getParameterOrderingNumber(0), is(0));

		assertThat(m.getMaxLine(), is(49));
		assertThat(m.getMinLine(), is(49));
		assertThat(UtilForAssertThat.asIntegerArray(m.getLineNumbers()), is(arrayContaining(49)));
		assertThat(m.getLine(2), is(49));
		assertThat(UtilForAssertThat.asIntegerArray(m.getInstructions(49)), is(arrayContainingInAnyOrder(1, 2, 3, 4)));
		
		assertThat(m.getCallSites(), is(empty()));
		assertThat(m.getCallSite(2), is(nullValue()));
		
		assertThat(m.getFieldAccesses(), is(empty()));
		
		assertThat(UtilForAssertThat.asIntegerArray(m.getReturnInstructions()), is(arrayContainingInAnyOrder(3)));
		
		assertThat(m.getDataDependence(), is(notNullValue()));
		assertThat(m.getControlDependence(), is(notNullValue()));
		assertThat(m.getConservativeControlFlow(), is(notNullValue()));
		DirectedGraph cfg = m.getControlFlow();
		assertThat(cfg.getVertexCount(), is(5));
		assertThat(cfg.getEdgeCount(), is(3));
		Integer[] edges0 = UtilForAssertThat.asIntegerArray(cfg.getEdges(0));
		Integer[] edges1 = UtilForAssertThat.asIntegerArray(cfg.getEdges(1));
		Integer[] edges2 = UtilForAssertThat.asIntegerArray(cfg.getEdges(2));
		Integer[] edges3 = UtilForAssertThat.asIntegerArray(cfg.getEdges(3));
		Integer[] edges4 = UtilForAssertThat.asIntegerArray(cfg.getEdges(4));
		assertThat(edges0, is(arrayContainingInAnyOrder(1)));
		assertThat(edges1, is(arrayContainingInAnyOrder(2)));
		assertThat(edges2, is(arrayContainingInAnyOrder(3)));
		assertThat(edges3, is(emptyArray()));
		assertThat(edges4, is(emptyArray()));
		
		assertThat(m.getMethodKey(), is(CLASS_D + "#example#(IJDLjava/lang/String;)I"));
		assertThat(m.toLongString(), is(CLASS_D + ".example(" + CLASS_D + ":this, int:i, long:l, double:d, java/lang/String:s): int"));
		assertThat(m.getInstructionString(0), is("0: (L00000)"));
		assertThat(m.getInstructionString(1), is("1: (line=49)"));
		assertThat(m.getInstructionString(2), is("2: ILOAD 1 (i)"));
		assertThat(m.getInstructionString(3), is("3: IRETURN"));
		assertThat(m.getInstructionString(4), is("4: (L00004)"));
	}
	
	@Test
	public void testMethodInfo02() {
		ClassInfo c = program.getClassInfo(CLASS_C);
		MethodInfo m = c.findMethod("main", "([Ljava/lang/String;)V");
		
		assertThat(m.getPackageName(), is("soba/testdata/inheritance1"));
		assertThat(m.getClassName(), is(CLASS_C));
		assertThat(m.getMethodName(), is("main"));
		assertThat(m.getDescriptor(), is("([Ljava/lang/String;)V"));
		assertThat(m.getGenericsSignature(), is(nullValue()));
		assertThat(m.hasMethodBody(), is(true));
		assertThat(m.isLibrary(), is(false));
		assertThat(m.isPrivate(), is(false));
		assertThat(m.isPublic(), is(true));
		assertThat(m.isProtected(), is(false));
		assertThat(m.isStatic(), is(true));
		assertThat(m.isSynthetic(), is(false));
		assertThat(m.isOverridable(), is(false));
		assertThat(m.isPackagePrivate(), is(false));
		assertThat(m.getInstructionCount(), is(27));
		assertThat(m.getParamCount(), is(1));
		assertThat(m.getReturnType(), is("void"));
		
		assertThat(m.getParamName(0), is("args"));
		assertThat(m.getParamType(0), is("java/lang/String[]"));
		assertThat(m.getVariableTableIndexOfParamAt(0), is(0));
		assertThat(m.isParameterOrderingNumber(0), is(true));
		assertThat(m.getParameterOrderingNumber(0), is(0));

		assertThat(m.getMaxLine(), is(15));
		assertThat(m.getMinLine(), is(10));
		assertThat(UtilForAssertThat.asIntegerArray(m.getLineNumbers()), is(arrayContaining(10, 11, 12, 13, 14, 15)));
		assertThat(m.getLine(9), is(11));
		assertThat(UtilForAssertThat.asIntegerArray(m.getInstructions(11)), is(arrayContainingInAnyOrder(6, 7, 8, 9, 10, 11)));
		
		assertThat(m.getCallSites(), hasSize(4));
		assertThat(m.getCallSite(9), is(notNullValue()));
		
		assertThat(m.getFieldAccesses(), hasSize(1));
		
		assertThat(UtilForAssertThat.asIntegerArray(m.getReturnInstructions()), is(arrayContainingInAnyOrder(25)));
		
		assertThat(m.getDataDependence(), is(notNullValue()));
		assertThat(m.getControlDependence(), is(notNullValue()));
		assertThat(m.getConservativeControlFlow(), is(notNullValue()));
		assertThat(m.getControlFlow(), is(notNullValue()));
		
		assertThat(m.getMethodKey(), is(CLASS_C + "#main#([Ljava/lang/String;)V"));
		assertThat(m.toLongString(), is(CLASS_C + ".main(java/lang/String[]:args): void"));
	}
	
}
