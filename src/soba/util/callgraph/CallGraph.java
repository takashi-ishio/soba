package soba.util.callgraph;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

import java.util.ArrayList;
import java.util.List;

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

	private ObjectIdMap<MethodInfo> methodToId;
	private DirectedGraph callGraph;
	private DirectedGraph reverseCallGraph;
	
	public CallGraph(JavaProgram program) {
		this(program, program.getClassHierarchy());
	}
	
	public CallGraph(JavaProgram program, IDynamicBindingResolver resolver) {
		methodToId = new ObjectIdMap<>();
		for (ClassInfo c: program.getClasses()) {
			for (MethodInfo m: c.getMethods()) {
				methodToId.add(m);
			}
		}
		
		IntPairList edges = new IntPairList();
		for (ClassInfo c: program.getClasses()) {
			for (MethodInfo m: c.getMethods()) {
				for (CallSite cs: m.getCallSites()) {
					MethodInfo[] callees = resolver.resolveCall(cs);
					for (MethodInfo callee: callees) {
						// m may call callee
						edges.add(methodToId.getId(m), methodToId.getId(callee));
					}
				}
			}
		}
		callGraph = new DirectedGraph(methodToId.size(), edges);
		reverseCallGraph = callGraph.getReverseGraph();
	}
	
	public int size() {
		return methodToId.size();
	}
	
	public List<MethodInfo> getMethods() {
		List<MethodInfo> methodList = new ArrayList<>();
		for (int i = 0; i < methodToId.size(); i++) {
			methodList.add(methodToId.getItem(i));
		}
		return methodList;
	}
	
	public List<MethodInfo> getCallees(MethodInfo caller) {
		int callerId = methodToId.getId(caller);
		List<MethodInfo> callees = new ArrayList<>();
		for (int calleeId: callGraph.getEdges(callerId)) {
			callees.add(methodToId.getItem(calleeId));
		}
		return callees;
	}

	public List<MethodInfo> getAllCallees(MethodInfo caller) {
		SimpleVisitor visitor = new SimpleVisitor();
		DepthFirstSearch.search(callGraph, methodToId.getId(caller), visitor);
		List<MethodInfo> callees = new ArrayList<>();
		for (int calleeId: visitor.getVisitedIdsWithoutOwn()) {
			callees.add(methodToId.getItem(calleeId));
		}
		return callees;
	}
	
	public List<MethodInfo> getCallers(MethodInfo callee) {
		int calleeId = methodToId.getId(callee);
		List<MethodInfo> callers = new ArrayList<>();
		for (int callerId: reverseCallGraph.getEdges(calleeId)) {
			callers.add(methodToId.getItem(callerId));
		}
		return callers;
	}
	
	public List<MethodInfo> getAllCallers(MethodInfo callee) {
		SimpleVisitor visitor = new SimpleVisitor();
		DepthFirstSearch.search(reverseCallGraph, methodToId.getId(callee), visitor);
		List<MethodInfo> callers = new ArrayList<>();
		for (int callerId: visitor.getVisitedIdsWithoutOwn()) {
			callers.add(methodToId.getItem(callerId));
		}
		return callers;
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
