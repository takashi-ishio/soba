package soba.util.graph;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import gnu.trove.list.array.TIntArrayList;

import org.junit.BeforeClass;
import org.junit.Test;

import soba.util.UtilForAssertThat;

public class DirectedGraphTest {
	
	private static DirectedGraph graph;
	
	@BeforeClass
	public static void buildGraph() {
		graph = GraphTestBase.buildGraph();
	}
	
	
	@Test
	public void testDepthFirstSearch() throws Exception {
		final TIntArrayList visited = new TIntArrayList();
		DepthFirstSearch.search(graph, 0, new IDepthFirstVisitor() {
			@Override
			public void onStart(int startVertexId) {
			}
			@Override
			public boolean onVisit(int vertexId) {
				visited.add(vertexId);
				return true;
			}
			@Override
			public void onLeave(int vertexId) {
			}
			@Override
			public void onFinished(boolean[] visited) {
				assertThat(visited.length, is(14));
				for (int i=0; i<13; ++i) {
					assertThat(visited[i], is(true));
				}
				assertThat(visited[13], is(false));
			}
			@Override
			public void onVisitAgain(int vertexId) {
				assertThat(visited.contains(vertexId), is(true));
			}
		});

		assertThat(visited.size(), is(13));
	}	
	
	@Test
	public void testEdgeCount() {
		assertThat(graph.getEdgeCount(), is(16));
	}
	
	@Test
	public void testReverseGraph() {
		DirectedGraph r = graph.getReverseGraph();
		assertThat(r.getVertexCount(), is(14));
		assertThat(r.getEdgeCount(), is(16));
		
		// check all edges
		Integer[] edgesFrom0 = UtilForAssertThat.asIntegerArray(r.getEdges(0));
		assertThat(edgesFrom0, is(arrayContainingInAnyOrder(2)));

		Integer[] edgesFrom1 = UtilForAssertThat.asIntegerArray(r.getEdges(1));
		assertThat(edgesFrom1, is(arrayContainingInAnyOrder(0)));

		Integer[] edgesFrom2 = UtilForAssertThat.asIntegerArray(r.getEdges(2));
		assertThat(edgesFrom2, is(arrayContainingInAnyOrder(1)));

		Integer[] edgesFrom3 = UtilForAssertThat.asIntegerArray(r.getEdges(3));
		assertThat(edgesFrom3, is(arrayContainingInAnyOrder(1)));

		Integer[] edgesFrom4 = UtilForAssertThat.asIntegerArray(r.getEdges(4));
		assertThat(edgesFrom4, is(arrayContainingInAnyOrder(3)));
		
		Integer[] edgesFrom5 = UtilForAssertThat.asIntegerArray(r.getEdges(5));
		assertThat(edgesFrom5, is(arrayContainingInAnyOrder(3, 8)));
		
		Integer[] edgesFrom6 = UtilForAssertThat.asIntegerArray(r.getEdges(6));
		assertThat(edgesFrom6, is(arrayContainingInAnyOrder(5)));

		Integer[] edgesFrom7 = UtilForAssertThat.asIntegerArray(r.getEdges(7));
		assertThat(edgesFrom7, is(arrayContainingInAnyOrder(6)));
		
		Integer[] edgesFrom8 = UtilForAssertThat.asIntegerArray(r.getEdges(8));
		assertThat(edgesFrom8, is(arrayContainingInAnyOrder(6, 7)));

		Integer[] edgesFrom9 = UtilForAssertThat.asIntegerArray(r.getEdges(9));
		assertThat(edgesFrom9, is(arrayContainingInAnyOrder(7)));
		
		Integer[] edgesFrom10 = UtilForAssertThat.asIntegerArray(r.getEdges(10));
		assertThat(edgesFrom10, is(arrayContainingInAnyOrder(7)));

		Integer[] edgesFrom11 = UtilForAssertThat.asIntegerArray(r.getEdges(11));
		assertThat(edgesFrom11, is(arrayContainingInAnyOrder(9, 10)));

		Integer[] edgesFrom12 = UtilForAssertThat.asIntegerArray(r.getEdges(12));
		assertThat(edgesFrom12, is(arrayContainingInAnyOrder(11)));

		Integer[] edgesFrom13 = UtilForAssertThat.asIntegerArray(r.getEdges(13));
		assertThat(edgesFrom13, is(emptyArray()));
	}
	
	@Test
	public void testUndirectedGraph() {
		DirectedGraph g = graph.getUndirectedGraph();
		Integer[] edgesFrom1 = UtilForAssertThat.asIntegerArray(g.getEdges(1));
		assertThat(edgesFrom1, is(arrayContainingInAnyOrder(0, 2, 3)));
		
		Integer[] edgesFrom10 = UtilForAssertThat.asIntegerArray(g.getEdges(10));
		assertThat(edgesFrom10, is(arrayContainingInAnyOrder(7, 11)));
		
		Integer[] edgesFrom12 = UtilForAssertThat.asIntegerArray(g.getEdges(12));
		assertThat(edgesFrom12, is(arrayContainingInAnyOrder(11)));
		
		Integer[] edgesFrom13 = UtilForAssertThat.asIntegerArray(g.getEdges(13));
		assertThat(edgesFrom13, is(emptyArray()));
		
		assertThat(g.getEdgeCount(), is(32));
	}
}
