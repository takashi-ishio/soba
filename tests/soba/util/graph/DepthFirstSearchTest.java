package soba.util.graph;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import org.junit.Before;
import org.junit.Test;

public class DepthFirstSearchTest {

	private DirectedGraph graph;
	
	/**
	 * Check the sequence of visited vertices.
	 */
	private class VisitList implements IDepthFirstVisitor {

		int[] visitList;
		int[] leaveList;
		boolean[] expectedVisited;
		int visitIndex = 0;
		int leaveIndex = 0;
		
		public VisitList(int[] visitList, int[] leaveList) {
			this.visitList = visitList;
			this.leaveList = leaveList;
			this.expectedVisited = new boolean[graph.getVertexCount()];
			for (int v: visitList) {
				expectedVisited[v] = true;
			}
			for (int v: leaveList) {
//				Assert.assertTrue(expectedVisited[v]);
				assertThat(expectedVisited[v], is(true));
			}
		}
		
		@Override
		public void onStart(int startVertexId) {
		}
		
		@Override
		public boolean onVisit(int vertexId) {
			assertThat(vertexId, is(visitList[visitIndex]));
			visitIndex++;
			return continueVisit(vertexId);
		}
		
		protected boolean continueVisit(int vertexId) {
			return true;
		}
		
		@Override
		public void onLeave(int vertexId) {
			assertThat(vertexId, is(leaveList[leaveIndex]));
			leaveIndex++;
		}
		@Override
		public void onFinished(boolean[] visited) {
			for (int i=0; i<visited.length; ++i) {
				assertThat(visited[i], is(expectedVisited[i]));
			}
		}
		
		@Override
		public void onVisitAgain(int vertexId) {
			// The vertices must be visited
			boolean visited = false;
			for (int i=0; i<visitIndex; ++i) {
				if (visitList[i] == vertexId) {
					visited = true;
					break;
				}
			}
			assertThat(visited, is(true));
		}
		
	}
	
	
	/**
	 digraph {
	    0 -> 1;  1 -> 2;   2 -> 0;  1 -> 3;  3 -> 4;  3 -> 5;
	    5 -> 6;  6 -> 7;   6 -> 8;  7 -> 8;  8 -> 5;  7 -> 9;
	    7 -> 10;  9 -> 11;  10 -> 11;  11 -> 12;  13;
	 }
	 */
	@Before
	public void buildGraph() throws Exception {
		graph = GraphTestBase.buildGraph();
	}
	
	@Test
	public void testDFS() throws Exception {
		DepthFirstSearch.search(graph, 7, new VisitList(new int[]{7, 8, 5, 6, 9, 11, 12, 10}, new int[]{6, 5, 8, 12, 11, 9, 10, 7}));
		DepthFirstSearch.search(graph, 7, new VisitList(new int[] {7}, new int[] {7}) {
			protected boolean continueVisit(int vertexId) {
				return false;
			};
		});
		DepthFirstSearch.search(graph, 0, new VisitList(new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, new int[] {2, 4, 8, 9, 10, 7, 6, 5,  3, 1, 0}) {
			protected boolean continueVisit(int vertexId) {
				return vertexId <= 7;
			};
		});
	}
	
}
