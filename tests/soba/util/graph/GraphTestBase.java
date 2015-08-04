package soba.util.graph;


import soba.util.IntPairList;

public class GraphTestBase {

	public static DirectedGraph buildGraph() { 
		IntPairList edges = new IntPairList();
		edges.add(0, 1); // cycle 0->1->2
		edges.add(1, 2);
		edges.add(2, 0);
		edges.add(1, 3);
		
		edges.add(3, 4);
		edges.add(3, 5);
		
		edges.add(5, 6); // two cycles: 5->6->8->5
		edges.add(6, 7); // and 5->6->7->8->5
		edges.add(6, 8);
		edges.add(7, 8);
		edges.add(8, 5);

		edges.add(7, 9);
		edges.add(7, 10);
		edges.add(9, 11);
		edges.add(10, 11);
		edges.add(11, 12); // 13 is not connected to any other vertices
		
		return new DirectedGraph(14, edges);
	}
	
}
