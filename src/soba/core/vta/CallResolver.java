package soba.core.vta;

import soba.core.ClassHierarchy;
import soba.core.IDynamicBindingResolver;
import soba.core.JavaProgram;
import soba.core.MethodInfo;
import soba.core.method.CallSite;

/**
 * This class wraps VTA resolver and CHA resolver.  
 * While VTAResolver resolves only INVOKEVIRTUAL and INVOKEINTERFACE, 
 * this wrapper object can easily resolve all invocations using both algorithms.
 */
public abstract class CallResolver implements IDynamicBindingResolver {

	public static CallResolver getCHA(JavaProgram program) {
		return new CHA(program.getClassHierarchy());
	}

	public static CallResolver getCHA(ClassHierarchy hierarchy) {
		return new CHA(hierarchy);
	}
	
	public static CallResolver getVTA(JavaProgram program, IAnalysisTarget selector) {
		VTAResolver vta = new VTAResolver(program, selector);
		return new VTA(program.getClassHierarchy(), vta);
	}
	
	public static CallResolver getVTA(JavaProgram program) {
		VTAResolver vta = new VTAResolver(program);
		return new VTA(program.getClassHierarchy(), vta);
	}
	
	/**
	 * @param c specifies a call site.  This instance must be extracted from MethodBody object.
	 * @return
	 */
	public abstract MethodInfo[] resolveCall(CallSite cs);
		
	private static class CHA extends CallResolver {
		
		private ClassHierarchy ch;
		
		public CHA(ClassHierarchy ch) {
			this.ch = ch;
		}
		
		public MethodInfo[] resolveCall(CallSite c) {
			return ch.resolveCall(c);
		}
	}
	
	private static class VTA extends CallResolver {
		
		private ClassHierarchy ch;
		private VTAResolver vta;
		
		public VTA(ClassHierarchy ch, VTAResolver vta) {
			this.ch = ch;
			this.vta = vta;
		}
		
		public MethodInfo[] resolveCall(CallSite c) {
			if (c.isStaticOrSpecial()) {
				return ch.resolveCall(c);
			} else {
				return vta.resolveCall(c);
			}
		}
	}
}
