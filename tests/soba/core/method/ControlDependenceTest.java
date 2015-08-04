package soba.core.method;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import soba.core.method.ControlDependence;
import soba.util.IntPairList;
import soba.util.UtilForAssertThat;
import soba.util.graph.DirectedGraph;

public class ControlDependenceTest {

	public static DirectedGraph buildControlFlowGraph() { 
		IntPairList edges = new IntPairList();
		edges.add(0, 1); // cycle 0->1->2
		edges.add(1, 2);
		edges.add(2, 0);
		edges.add(1, 3);
		
		edges.add(3, 4);
		edges.add(3, 5);
		
		edges.add(5, 6); // another cycle: 5->6->8->5
		edges.add(6, 8);
		edges.add(8, 5);

		edges.add(6, 7); 
		edges.add(7, 9);
		edges.add(7, 10);
		edges.add(9, 11);
		edges.add(10, 11);
		edges.add(11, 12); // 13 is not connected to any other vertices
		
		return new DirectedGraph(14, edges);
	}


	@Test
	public void testControlDependence() {
		DirectedGraph g = buildControlFlowGraph();
		DirectedGraph cd = ControlDependence.getDependence(14, g);
		
		// Not a branch vertex
		Integer[] edgesFrom0 = UtilForAssertThat.asIntegerArray(cd.getEdges(0));
		Integer[] edgesFrom2 = UtilForAssertThat.asIntegerArray(cd.getEdges(2));
		Integer[] edgesFrom4 = UtilForAssertThat.asIntegerArray(cd.getEdges(4));
		Integer[] edgesFrom5 = UtilForAssertThat.asIntegerArray(cd.getEdges(5));
		Integer[] edgesFrom8 = UtilForAssertThat.asIntegerArray(cd.getEdges(8));
		Integer[] edgesFrom9 = UtilForAssertThat.asIntegerArray(cd.getEdges(9));
		Integer[] edgesFrom10 = UtilForAssertThat.asIntegerArray(cd.getEdges(10));
		Integer[] edgesFrom11 = UtilForAssertThat.asIntegerArray(cd.getEdges(11));
		Integer[] edgesFrom12 = UtilForAssertThat.asIntegerArray(cd.getEdges(12));
		Integer[] edgesFrom13 = UtilForAssertThat.asIntegerArray(cd.getEdges(13));
		assertThat(edgesFrom0, is(emptyArray()));
		assertThat(edgesFrom2, is(emptyArray()));
		assertThat(edgesFrom4, is(emptyArray()));
		assertThat(edgesFrom5, is(emptyArray()));
		assertThat(edgesFrom8, is(emptyArray()));
		assertThat(edgesFrom9, is(emptyArray()));
		assertThat(edgesFrom10, is(emptyArray()));
		assertThat(edgesFrom11, is(emptyArray()));
		assertThat(edgesFrom12, is(emptyArray()));
		assertThat(edgesFrom13, is(emptyArray()));
		
		// Conditional branches
		Integer[] edgesFrom1 = UtilForAssertThat.asIntegerArray(cd.getEdges(1));
		Integer[] edgesFrom3 = UtilForAssertThat.asIntegerArray(cd.getEdges(3));
		Integer[] edgesFrom6 = UtilForAssertThat.asIntegerArray(cd.getEdges(6));
		Integer[] edgesFrom7 = UtilForAssertThat.asIntegerArray(cd.getEdges(7));
		assertThat(edgesFrom1, is(arrayContainingInAnyOrder(0, 2)));
		assertThat(edgesFrom3, is(arrayContainingInAnyOrder(4, 6, 7, 11, 12)));
		assertThat(edgesFrom6, is(arrayContainingInAnyOrder(5, 8)));
		assertThat(edgesFrom7, is(arrayContainingInAnyOrder(9, 10)));
	}
	
}
