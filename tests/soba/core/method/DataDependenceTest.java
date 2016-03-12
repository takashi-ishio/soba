package soba.core.method;

import java.util.List;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import org.junit.BeforeClass;
import org.junit.Test;

import soba.core.JavaProgram;
import soba.core.JavaProgramTest;
import soba.core.MethodInfo;
import soba.core.method.DataFlowEdge;
import soba.util.UtilForAssertThat;
import soba.util.graph.DirectedGraph;

public class DataDependenceTest {

	private static JavaProgram program;
	
	@BeforeClass
	public static void setUpBeforeClass() {
		program = JavaProgramTest.readExampleProgram();
	}
	
	@Test
	public void testDataDependence01() {
		MethodInfo m = program.getClassInfo("soba/testdata/DefUseTestData").findMethod("overwriteParam", "(II)V");
		DataDependence dd = m.getDataDependence();
		assertThat(dd, is(notNullValue()));

		List<DataFlowEdge> edges = dd.getEdges();
		assertThat(edges, hasSize(7));
		assertThat(containsEdge(edges, -1, 2), is(true));
		assertThat(containsEdge(edges, 2, 3), is(true));
		assertThat(containsEdge(edges, 4, 5), is(true));
		assertThat(containsEdge(edges, -1, 10), is(true));
		assertThat(containsEdge(edges, 5, 10), is(true));
		assertThat(containsEdge(edges, 9, 11), is(true));
		assertThat(containsEdge(edges, 10, 11), is(true));
		
		List<DataFlowEdge> edgesSourceOrder = dd.getEdgesInSourceOrder();
		assertThat(edgesSourceOrder, hasSize(7));
		assertThat(containsEdgeAt(edgesSourceOrder, -1, 2, 0), is(true));
		assertThat(containsEdgeAt(edgesSourceOrder, -1, 10, 1), is(true));
		assertThat(containsEdgeAt(edgesSourceOrder, 2, 3, 2), is(true));
		assertThat(containsEdgeAt(edgesSourceOrder, 4, 5, 3), is(true));
		assertThat(containsEdgeAt(edgesSourceOrder, 5, 10, 4), is(true));
		assertThat(containsEdgeAt(edgesSourceOrder, 9, 11, 5), is(true));
		assertThat(containsEdgeAt(edgesSourceOrder, 10, 11, 6), is(true));
		
		DirectedGraph dependence = dd.getDependenceGraph();
		assertThat(dependence.getVertexCount(), is(m.getInstructionCount()));
		assertThat(dependence.getEdgeCount(), is(5));
		Integer[] edgesFrom0 = UtilForAssertThat.asIntegerArray(dependence.getEdges(0));
		Integer[] edgesFrom1 = UtilForAssertThat.asIntegerArray(dependence.getEdges(1));
		Integer[] edgesFrom2 = UtilForAssertThat.asIntegerArray(dependence.getEdges(2));
		Integer[] edgesFrom3 = UtilForAssertThat.asIntegerArray(dependence.getEdges(3));
		Integer[] edgesFrom4 = UtilForAssertThat.asIntegerArray(dependence.getEdges(4));
		Integer[] edgesFrom5 = UtilForAssertThat.asIntegerArray(dependence.getEdges(5));
		Integer[] edgesFrom6 = UtilForAssertThat.asIntegerArray(dependence.getEdges(6));
		Integer[] edgesFrom7 = UtilForAssertThat.asIntegerArray(dependence.getEdges(7));
		Integer[] edgesFrom8 = UtilForAssertThat.asIntegerArray(dependence.getEdges(8));
		Integer[] edgesFrom9 = UtilForAssertThat.asIntegerArray(dependence.getEdges(9));
		Integer[] edgesFrom10 = UtilForAssertThat.asIntegerArray(dependence.getEdges(10));
		Integer[] edgesFrom11 = UtilForAssertThat.asIntegerArray(dependence.getEdges(11));
		Integer[] edgesFrom12 = UtilForAssertThat.asIntegerArray(dependence.getEdges(12));
		Integer[] edgesFrom13 = UtilForAssertThat.asIntegerArray(dependence.getEdges(13));
		Integer[] edgesFrom14 = UtilForAssertThat.asIntegerArray(dependence.getEdges(14));
		Integer[] edgesFrom15 = UtilForAssertThat.asIntegerArray(dependence.getEdges(15));
		assertThat(edgesFrom2, is(arrayContainingInAnyOrder(3)));
		assertThat(edgesFrom4, is(arrayContainingInAnyOrder(5)));
		assertThat(edgesFrom5, is(arrayContainingInAnyOrder(10)));
		assertThat(edgesFrom9, is(arrayContainingInAnyOrder(11)));
		assertThat(edgesFrom10, is(arrayContainingInAnyOrder(11)));
		assertThat(edgesFrom0, is(emptyArray()));
		assertThat(edgesFrom1, is(emptyArray()));
		assertThat(edgesFrom3, is(emptyArray()));
		assertThat(edgesFrom6, is(emptyArray()));
		assertThat(edgesFrom7, is(emptyArray()));
		assertThat(edgesFrom8, is(emptyArray()));
		assertThat(edgesFrom11, is(emptyArray()));
		assertThat(edgesFrom12, is(emptyArray()));
		assertThat(edgesFrom13, is(emptyArray()));
		assertThat(edgesFrom14, is(emptyArray()));
		assertThat(edgesFrom15, is(emptyArray()));

		List<DataFlowEdge> incomingEdges2 = dd.getIncomingEdges(2);
		assertThat(incomingEdges2, hasSize(1));
		assertThat(containsEdge(incomingEdges2, -1, 2), is(true));
		assertThat(dd.getVariableName(incomingEdges2.get(0)), is("x"));
		assertThat(dd.getVariableDescriptor(incomingEdges2.get(0)), is("I"));
		List<DataFlowEdge> incomingEdges11 = dd.getIncomingEdges(11);
		assertThat(incomingEdges11, hasSize(2));
		assertThat(containsEdge(incomingEdges11, 9, 11), is(true));
		assertThat(containsEdge(incomingEdges11, 10, 11), is(true));
		
		DataFlowEdge incomingEdge11At0 = dd.getIncomingEdge(11, 0);
		assertThat(incomingEdge11At0.getSourceInstruction(), is(9));
		DataFlowEdge incommingEdge11At1 = dd.getIncomingEdge(11, 1);
		assertThat(incommingEdge11At1.getSourceInstruction(), is(10));
		
		List<DataFlowEdge> incomingEdges11At0 = dd.getIncomingEdges(11, 0);
		assertThat(incomingEdges11At0, hasSize(1));
		assertThat(containsEdge(incomingEdges11At0, 9, 11), is(true));
		List<DataFlowEdge> incomingEdges11At1 = dd.getIncomingEdges(11, 1);
		assertThat(incomingEdges11At1, hasSize(1));
		assertThat(containsEdge(incomingEdges11At1, 10, 11), is(true));
		
		int[][] definition10 = dd.getDataDefinition(10);
		assertThat(definition10.length, is(1));
		Integer[] local = UtilForAssertThat.asIntegerArray(definition10[0]);
		assertThat(local, is(arrayContainingInAnyOrder(-1, 5)));
		int[][] definition11 = dd.getDataDefinition(11);
		assertThat(definition11.length, is(2));
		Integer[] operand0 = UtilForAssertThat.asIntegerArray(definition11[0]);
		Integer[] operand1 = UtilForAssertThat.asIntegerArray(definition11[1]);
		assertThat(operand0, is(arrayContainingInAnyOrder(9)));
		assertThat(operand1, is(arrayContainingInAnyOrder(10)));
		
		assertThat(dd.getOperandCount(2), is(0));
		assertThat(dd.getOperandCount(3), is(1));
		assertThat(dd.getOperandCount(11), is(2));
	}
	
	@Test
	public void testDataDependence02() {
		MethodInfo m = program.getClassInfo("soba/testdata/DefUseTestData").findMethod("localDataDependence", "()V");
		DataDependence dd = m.getDataDependence();
		assertThat(dd, is(notNullValue()));

		List<DataFlowEdge> edges = dd.getEdges();
		assertThat(edges, hasSize(17));
		assertThat(containsEdge(edges, 2, 3), is(true));
		assertThat(containsEdge(edges, 3, 6), is(true));
		assertThat(containsEdge(edges, 6, 7), is(true));
		assertThat(containsEdge(edges, 10, 11), is(true));
		assertThat(containsEdge(edges, 18, 19), is(true));
		assertThat(containsEdge(edges, 19, 23), is(true));
		assertThat(containsEdge(edges, 22, 24), is(true));
		assertThat(containsEdge(edges, 23, 24), is(true));
		assertThat(containsEdge(edges, 27, 28), is(true));
		assertThat(containsEdge(edges, 11, 33), is(true));
		assertThat(containsEdge(edges, 28, 33), is(true));
		assertThat(containsEdge(edges, 32, 34), is(true));
		assertThat(containsEdge(edges, 11, 38), is(true));
		assertThat(containsEdge(edges, 28, 38), is(true));
		assertThat(containsEdge(edges, 37, 39), is(true));
		assertThat(containsEdge(edges, 38, 39), is(true));
	}
	
	@Test
	public void testDataDependence03() {
		MethodInfo m = program.getClassInfo("soba/testdata/DefUseTestData").findMethod("tryFinallyDependence", "()I");
		DataDependence dd = m.getDataDependence();
		assertThat(dd, is(notNullValue()));
	}
	
	private boolean containsEdge(List<DataFlowEdge> edges, int from, int to) {
		for (DataFlowEdge e: edges) {
			if (e.getSourceInstruction() == from 
					&& e.getDestinationInstruction() == to) {
				return true;
			}
		}
		return false;
	}
	
	private boolean containsEdgeAt(List<DataFlowEdge> edges, int from, int to, int pos) {
		return edges.size() > pos && 
			    edges.get(pos).getSourceInstruction() == from &&
			    edges.get(pos).getDestinationInstruction() == to;
	}

}
