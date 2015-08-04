package soba.example.dump;

import soba.core.ClassInfo;
import soba.core.JavaProgram;
import soba.core.MethodInfo;
import soba.core.method.CallSite;
import soba.core.vta.CallResolver;
import soba.util.files.ClasspathUtil;

public class DumpMethodCallUsingVTA {

	public static void main(String[] args) {
		JavaProgram program = new JavaProgram(ClasspathUtil.getClassList(args));
		CallResolver resolver = CallResolver.getVTA(program);
		
		for (ClassInfo c: program.getClasses()) {
			for (MethodInfo m: c.getMethods()) {
				System.out.println(m.toLongString());
				for (CallSite cs: m.getCallSites()) {
					MethodInfo[] callees = resolver.resolveCall(cs);
					if (callees.length > 0) {
						for (MethodInfo callee: callees) {
							System.out.println("  [inside] " + callee.toLongString());
						}
					} else {
						System.out.println("  [outside] " + cs.toString());
					}
				}
			}
		}
	}

}
