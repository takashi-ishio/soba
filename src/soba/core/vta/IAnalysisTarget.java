package soba.core.vta;

import soba.core.FieldInfo;
import soba.core.MethodInfo;

public interface IAnalysisTarget {

	public boolean isTargetMethod(MethodInfo m);
	public boolean isTargetField(FieldInfo f);
	public boolean assumeExternalCallers(MethodInfo m);

	public boolean isExcludedType(String className);
	
}
