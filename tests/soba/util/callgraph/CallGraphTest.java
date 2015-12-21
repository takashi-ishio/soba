package soba.util.callgraph;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import soba.core.ClassInfo;
import soba.core.ExampleProgram;
import soba.core.JavaProgram;
import soba.core.JavaProgramTest;
import soba.core.MethodInfo;

public class CallGraphTest {

	private static JavaProgram program;
	private static CallGraph callGraph;
	
	private static ClassInfo classC;
	private static ClassInfo classD;
	private static ClassInfo classE;
	private static ClassInfo classG;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		program = JavaProgramTest.readExampleProgram();
		callGraph = new CallGraph(program);
		
		classC = program.getClassInfo(ExampleProgram.CLASS_C);
		classD = program.getClassInfo(ExampleProgram.CLASS_D);
		classE = program.getClassInfo(ExampleProgram.CLASS_E);
		classG = program.getClassInfo(ExampleProgram.CLASS_G);
	}

	@Test
	public void testGetCallees01() {
		MethodInfo caller = classE.findMethod("main", "([Ljava/lang/String;)V");
		
		MethodInfo callee1 = classE.findMethod("<init>", "()V");
		MethodInfo callee2 = classE.findMethod("exec", "()V");
		
		List<MethodInfo> callees = callGraph.getCallees(caller);
		assertThat(callees, containsInAnyOrder(callee1, callee2));
	}
	
	@Test
	public void testGetAllCallees01() {
		MethodInfo caller = classG.findMethod("<init>", "()V");

		MethodInfo callee1 = classC.findMethod("<init>", "(I)V");
		MethodInfo callee2 = classC.findMethod("q", "(D)V");
		
		List<MethodInfo> callees = callGraph.getAllCallees(caller);
		assertThat(callees, containsInAnyOrder(callee1, callee2));
	}

	@Test
	public void testGetCallers01() {
		MethodInfo callee = classG.findMethod("<init>", "()V");
		
		MethodInfo caller1 = classE.findMethod("testDynamicBinding1", "()V");
		MethodInfo caller2 = classE.findMethod("testDynamicBinding2", "()V");
		MethodInfo caller3 = classE.findMethod("testDynamicBinding5", "(Lsoba/testdata/inheritance1/C;)V");
		MethodInfo caller4 = classD.findMethod("testPackagePrivate3", "()V");
		
		List<MethodInfo> callers = callGraph.getCallers(callee);
		assertThat(callers, containsInAnyOrder(caller1, caller2, caller3, caller4));
	}
	
	@Test
	public void testGetAllCallers01() {
		MethodInfo callee = classC.findMethod("m", "()V");
		
		MethodInfo caller1 = classD.findMethod("m", "()V");
		MethodInfo caller2 = classE.findMethod("<init>", "()V");
		MethodInfo caller3 = classE.findMethod("main", "([Ljava/lang/String;)V");

		List<MethodInfo> callers = callGraph.getAllCallers(callee);
		assertThat(callers, containsInAnyOrder(caller1, caller2, caller3));
	}
	
}
