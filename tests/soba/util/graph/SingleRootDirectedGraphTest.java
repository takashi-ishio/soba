package soba.util.graph;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import org.junit.Before;
import org.junit.Test;

import soba.util.IntPairProc;


public class SingleRootDirectedGraphTest {


	private DirectedGraph graph;
	
	@Before
	public void buildGraph() throws Exception {
		graph = GraphTestBase.buildGraph();
	}

	@Test
	public void testSingleRootDirectedGraph() throws Exception {
		SingleRootDirectedGraph base = new SingleRootDirectedGraph(graph);
		assertThat(base.getVertexCount(), is(15));
		assertThat(base.getRootId(), is(14));
		assertThat(base.getEdges(14).length, is(2));
		assertThat(base.getEdges(14)[0], is(0));
		assertThat(base.getEdges(14)[1], is(13));
	}
	
	@Test
	public void testEdges() throws Exception {
		final SingleRootDirectedGraph base = new SingleRootDirectedGraph(graph);
		base.forEachEdge(new IntPairProc() {
			
			int index = 0;
			int[][] expected = new int[][] {
				{0, 1}, {1, 2}, {1, 3}, {2, 0},
				{3, 4}, {3, 5}, {5, 6}, {6, 7},
				{6, 8}, {7, 8}, {7, 9}, {7, 10},
				{8, 5}, {9, 11}, {10, 11}, {11, 12},
				{14, 0}, {14, 13}
			};
			@Override
			public boolean execute(int elem1, int elem2) {
				assertThat(elem1, is(expected[index][0]));
				assertThat(elem2, is(expected[index][1]));
				index++;
				return true;
			}
		});
		base.forEachEdge(new IntPairProc() {
			private boolean first = true;
			@Override
			public boolean execute(int elem1, int elem2) {
				if (first) {
					first = false;
					return false;
				} else {
					fail();
					return false;
				}
			}
		});
		base.forEachEdge(new IntPairProc() {
			private boolean firstFromRoot = true;
			@Override
			public boolean execute(int elem1, int elem2) {
				if (base.getRootId() == elem1) {
					if (firstFromRoot) {
						firstFromRoot = false;
						return false;
					} else {
						fail();
						return false;
					}
				} else {
					return true;
				}
			}
		});
	}
}
