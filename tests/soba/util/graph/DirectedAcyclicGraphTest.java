package soba.util.graph;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import org.junit.Before;
import org.junit.Test;

import soba.util.IntPairProc;
import soba.util.UtilForAssertThat;

public class DirectedAcyclicGraphTest {

	DirectedGraph graph;
	DirectedAcyclicGraph dag;
	
	@Before
	public void buildGraph() throws Exception {
		graph = GraphTestBase.buildGraph();
		dag = new DirectedAcyclicGraph(graph);
	}
	
	@Test
	public void testAcyclicGraphNodes() {
		assertThat(dag.getVertexCount(), is(graph.getVertexCount()));
		assertThat(dag.getVertexCount(), is(graph.getVertexCount()));
		assertThat(dag.isRepresentativeNode(0), is(true));
		assertThat(dag.isRepresentativeNode(1), is(false));
		assertThat(dag.isRepresentativeNode(2), is(false));
		assertThat(dag.isRepresentativeNode(3), is(true));
		assertThat(dag.isRepresentativeNode(4), is(true));
		assertThat(dag.isRepresentativeNode(5), is(true));
		assertThat(dag.isRepresentativeNode(6), is(false));
		assertThat(dag.isRepresentativeNode(7), is(false));
		assertThat(dag.isRepresentativeNode(8), is(false));
		assertThat(dag.isRepresentativeNode(9), is(true));
		assertThat(dag.isRepresentativeNode(10), is(true));
		assertThat(dag.isRepresentativeNode(11), is(true));
		assertThat(dag.isRepresentativeNode(12), is(true));
		assertThat(dag.isRepresentativeNode(13), is(true));
	}
	
	@Test
	public void testAcyclicGraphEdges() {
		dag.forEachEdge(new IntPairProc() {

			int index = 0;
			int[][] expected = new int[][] {
				{0, 3}, {3, 4}, {3, 5}, {5, 9}, {5, 10},
				{9, 11}, {10, 11}, {11, 12} };

			@Override
			public boolean execute(int elem1, int elem2) {
				assertThat(elem1, is(expected[index][0]));
				assertThat(elem2, is(expected[index][1]));
				index++;
				return true;
			}
		});
		
		assertThat(UtilForAssertThat.asIntegerArray(dag.getEdges(13)), is(emptyArray()));
		assertThat(UtilForAssertThat.asIntegerArray(dag.getEdges(3)), is(arrayContainingInAnyOrder(4, 5)));
	}

	
}
