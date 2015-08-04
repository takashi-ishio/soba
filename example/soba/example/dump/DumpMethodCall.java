package soba.example.dump;

import soba.core.ClassHierarchy;
import soba.core.ClassInfo;
import soba.core.JavaProgram;
import soba.core.MethodInfo;
import soba.core.method.CallSite;
import soba.util.files.ClasspathUtil;

public class DumpMethodCall {

	public static void main(String[] args) {
		long count = 0;
		long t = System.currentTimeMillis();
		int classCount = 0;
		int methodCount = 0;
		JavaProgram program = new JavaProgram(ClasspathUtil.getClassList(args));
		ClassHierarchy ch = program.getClassHierarchy();
		
		for (ClassInfo c: program.getClasses()) {
			classCount++;
			for (MethodInfo m: c.getMethods()) {
				methodCount++;
				System.out.println(m.toLongString());
				for (CallSite cs: m.getCallSites()) {
					MethodInfo[] callees = ch.resolveCall(cs);
					if (callees.length > 0) {
						for (MethodInfo callee: callees) {
							//System.out.println("  [inside] " + callee.toLongString());
							count++;
						}
					} else {
						//System.out.println("  [outside] " + cs.toString());
						count++;
					}
				}
			}
		}
		System.out.println(classCount + " classes");
		System.out.println(methodCount + " methods");
		System.out.println(count + " method calls");
		System.out.println((System.currentTimeMillis() -t ) + "ms");
	}

}
