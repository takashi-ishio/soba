package soba.util.callgraph;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import soba.core.ClassInfo;
import soba.core.IDynamicBindingResolver;
import soba.core.JavaProgram;
import soba.core.MethodInfo;
import soba.core.method.CallSite;
import soba.util.IntPairList;
import soba.util.ObjectIdMap;
import soba.util.graph.DepthFirstSearch;
import soba.util.graph.DirectedGraph;
import soba.util.graph.IDepthFirstVisitor;

public class CallGraph {

	private ObjectIdMap<MethodInfo> methods;
	private DirectedGraph callGraph;
	private DirectedGraph reverseCallGraph;
	
	public CallGraph(JavaProgram program) {
		this(program, program.getClassHierarchy());
	}
	
	public CallGraph(JavaProgram program, IDynamicBindingResolver resolver) {
		methods = new ObjectIdMap<>();
		for (ClassInfo c: program.getClasses()) {
			for (MethodInfo m: c.getMethods()) {
				methods.add(m);
			}
		}
		
		IntPairList edges = new IntPairList();
		for (ClassInfo c: program.getClasses()) {
			for (MethodInfo m: c.getMethods()) {
				for (CallSite cs: m.getCallSites()) {
					MethodInfo[] callees = resolver.resolveCall(cs);
					for (MethodInfo callee: callees) {
						// m may call callee
						edges.add(methods.getId(m), methods.getId(callee));
					}
				}
			}
		}
		callGraph = new DirectedGraph(methods.size(), edges);
		reverseCallGraph = callGraph.getReverseGraph();
	}
	
	public List<MethodInfo> getCallees(MethodInfo caller) {
		return Arrays.stream(callGraph.getEdges(methods.getId(caller)))
				.mapToObj(methods::getItem)
				.collect(Collectors.toList());
	}

	public List<MethodInfo> getAllCallees(MethodInfo caller) {
		SimpleVisitor visitor = new SimpleVisitor();
		DepthFirstSearch.search(callGraph, methods.getId(caller), visitor);
		return Arrays.stream(visitor.getVisitedIdsWithoutOwn())
				.mapToObj(methods::getItem)
				.collect(Collectors.toList());
	}
	
	public List<MethodInfo> getCallers(MethodInfo callee) {
		return Arrays.stream(reverseCallGraph.getEdges(methods.getId(callee)))
				.mapToObj(methods::getItem)
				.collect(Collectors.toList());
	}
	
	public List<MethodInfo> getAllCallers(MethodInfo callee) {
		SimpleVisitor visitor = new SimpleVisitor();
		DepthFirstSearch.search(reverseCallGraph, methods.getId(callee), visitor);
		return Arrays.stream(visitor.getVisitedIdsWithoutOwn())
				.mapToObj(methods::getItem)
				.collect(Collectors.toList());
	}
	
	private class SimpleVisitor implements IDepthFirstVisitor {
		
		private TIntList visited;
		
		private SimpleVisitor() {
			visited = new TIntArrayList();
		}
		
		private int[] getVisitedIdsWithoutOwn() {
			return visited.toArray(1, visited.size()-1);
		}
		
		@Override
		public void onStart(int startVertexId) {
		}

		@Override
		public boolean onVisit(int vertexId) {
			if (visited.contains(vertexId)) {
				return false;
			}
			visited.add(vertexId);
			return true;
		}

		@Override
		public void onVisitAgain(int vertexId) {
		}

		@Override
		public void onLeave(int vertexId) {
		}

		@Override
		public void onFinished(boolean[] visited) {
		}
		
	}
}
