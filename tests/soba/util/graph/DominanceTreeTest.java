package soba.util.graph;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import org.junit.BeforeClass;
import org.junit.Test;


public class DominanceTreeTest {

	private static DirectedGraph graph;
	
	@BeforeClass
	public static void buildGraph() throws Exception {
		graph = GraphTestBase.buildGraph();
	}

	@Test
	public void testDominanceTree() {
		SingleRootDirectedGraph g = new SingleRootDirectedGraph(graph);
		DominanceTree tree = new DominanceTree(g);
		
		assertThat(tree.isRoot(g.getRootId()), is(true));
		assertThat(tree.isRoot(0), is(false));
		assertThat(tree.isRoot(13), is(false));
		assertThat(tree.getDominator(0), is(14));
		assertThat(tree.getDominator(1), is(0));
		assertThat(tree.getDominator(2), is(1));
		assertThat(tree.getDominator(3), is(1));
		assertThat(tree.getDominator(4), is(3));
		assertThat(tree.getDominator(5), is(3));
		assertThat(tree.getDominator(6), is(5));
		assertThat(tree.getDominator(7), is(6));
		assertThat(tree.getDominator(8), is(6));
		assertThat(tree.getDominator(9), is(7));
		assertThat(tree.getDominator(10), is(7));
		assertThat(tree.getDominator(11), is(7));
		assertThat(tree.getDominator(12), is(11));
		assertThat(tree.getDominator(13), is(14));
		
		assertThat(tree.nearestCommonAncestor(1, 13), is(14));
		assertThat(tree.nearestCommonAncestor(5, 12), is(5));
		assertThat(tree.nearestCommonAncestor(4, 12), is(3));
		assertThat(tree.nearestCommonAncestor(12, 4), is(3));
		assertThat(tree.nearestCommonAncestor(8, 6), is(6));
	}
}
