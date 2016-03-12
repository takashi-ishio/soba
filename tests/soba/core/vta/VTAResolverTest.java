package soba.core.vta;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.BeforeClass;
import org.junit.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;

import soba.core.ClassInfo;
import soba.core.ExampleProgram;
import soba.core.FieldInfo;
import soba.core.JavaProgram;
import soba.core.JavaProgramTest;
import soba.core.MethodInfo;
import soba.core.vta.IAnalysisTarget;
import soba.core.vta.TypeSet;
import soba.core.vta.VTAResolver;

public class VTAResolverTest implements ExampleProgram {

	private static JavaProgram program;
	private static VTAResolver resolver;
	
	@BeforeClass
	public static void setupResolver() {
		program = JavaProgramTest.readExampleProgram();
		resolver = new VTAResolver(program, new IAnalysisTarget(){
			@Override
			public boolean assumeExternalCallers(MethodInfo m) {
				return false;
			}
			@Override
			public boolean isExcludedType(String className) {
				return false;
			}
			@Override
			public boolean isTargetMethod(MethodInfo m) {
				return m.getClassName().startsWith("soba/testdata");
			}
			@Override
			public boolean isTargetField(FieldInfo f) {
				return true;
			}
		});
	}
	
	private void checkClasses(MethodInfo[] resolved, String... classNames) {
		List<String> classes = new ArrayList<>();
		for (MethodInfo m: resolved) {
			classes.add(m.getClassName());
		}
		assertThat(classes, containsInAnyOrder(classNames));
	}
	
	@Test
	public void testResolveCall01() {
		ClassInfo c = program.getClassInfo(CLASS_E);
		MethodInfo m = c.findMethod("testDynamicBinding1", "()V");
		InsnList instructions = m.getMethodNode().instructions;
		int counter = 0;
		for (int i=0; i<instructions.size(); ++i) {
			if (instructions.get(i).getOpcode() == Opcodes.INVOKEVIRTUAL) {
				if (counter == 0 || counter == 1) {
					MethodInfo[] methods = resolver.resolveCall(m.getCallSite(i));
					checkClasses(methods, CLASS_D, CLASS_G);
				}
				counter++;
			}
		}
		assertThat(counter, is(2));
		
	}

	@Test
	public void testResolveCall02() {
		ClassInfo c = program.getClassInfo("soba/testdata/inheritance2/E");
		MethodInfo m = c.findMethod("testDynamicBinding2", "()V");
		InsnList instructions = m.getMethodNode().instructions;
		int counter = 0;
		for (int i=0; i<instructions.size(); ++i) {
			if (instructions.get(i).getOpcode() == Opcodes.INVOKEVIRTUAL) {
				if (counter == 0) {
					MethodInfo[] methods = resolver.resolveCall(m.getCallSite(i));
					checkClasses(methods, CLASS_D);
				} else if (counter == 1) {
					MethodInfo[] methods = resolver.resolveCall(m.getCallSite(i));
					checkClasses(methods, CLASS_G);
				}
				counter++;
			}
		}
		assertThat(counter, is(2));
		
	}

	/**
	 * Inherited but not overridden methods
	 */
	@Test
	public void testResolvedCall03() {
		ClassInfo c = program.getClassInfo(CLASS_E);
		MethodInfo m = c.findMethod("testDynamicBinding3", "()V");
		InsnList instructions = m.getMethodNode().instructions;
		int counter = 0;
		for (int i=0; i<instructions.size(); ++i) {
			if (instructions.get(i).getOpcode() == Opcodes.INVOKEVIRTUAL) {
				if (counter == 0) {
					MethodInfo[] methods = resolver.resolveCall(m.getCallSite(i));
					checkClasses(methods, CLASS_C);
				} else if (counter == 1) {
					MethodInfo[] methods = resolver.resolveCall(m.getCallSite(i));
					checkClasses(methods, CLASS_C);
				}
				counter++;
			}
		}
	}
	
	/**
	 * Declared but not implemented method
	 */
	@Test
	public void testResolvedCall04() {
		ClassInfo c = program.getClassInfo(CLASS_E);
		MethodInfo m = c.findMethod("testDynamicBinding6", "(Lsoba/testdata/inheritance2/L;)V");
		InsnList instructions = m.getMethodNode().instructions;
		int counter = 0;
		for (int i=0; i<instructions.size(); ++i) {
			if (instructions.get(i).getOpcode() == Opcodes.INVOKEVIRTUAL) {
				if (counter == 0) {
					MethodInfo[] methods = resolver.resolveCall(m.getCallSite(i));
					assertThat(methods, is(emptyArray()));
				} else if (counter == 1) {
					MethodInfo[] methods = resolver.resolveCall(m.getCallSite(i));
					checkClasses(methods, CLASS_C, CLASS_D, CLASS_G);
				}
				counter++;
			}
		}
	}
	
	/**
	 * Resolves library method calls.
	 */
	@Test
	public void testResolveCall05() {
		ClassInfo c = program.getClassInfo(CLASS_E);
		MethodInfo m = c.findMethod("testDynamicBinding7", "(Z)V");
		InsnList instructions = m.getMethodNode().instructions;
		for (int i = 0; i < instructions.size(); i++) {
			if (instructions.get(i).getOpcode() == Opcodes.INVOKEVIRTUAL) {
				MethodInfo[] methods = resolver.resolveCall(m.getCallSite(i));
				checkClasses(methods, "java/util/ArrayList", "java/util/LinkedList");
			}
		}
	}
	
	private void checkLoopBinding(MethodInfo m) {
		InsnList instructions = m.getMethodNode().instructions;
		for (int i=0; i<instructions.size(); ++i) {
			if (instructions.get(i).getOpcode() == Opcodes.INVOKEVIRTUAL) {
				MethodInsnNode call = (MethodInsnNode)instructions.get(i);
				if (call.name.equals("x")) {
					MethodInfo[] methods = resolver.resolveCall(m.getCallSite(i));
					checkClasses(methods, CLASS_D, CLASS_G);
				}
			}
		}
	}
	
	private void checkParamBinding(MethodInfo m) {
		TypeSet typeset = resolver.getMethodParamType(m, 1);
		assertThat(typeset.getTypeCount(), is(2));
	}
	
	@Test
	public void testLoopBinding() {
		ClassInfo c = program.getClassInfo(CLASS_E);
		checkLoopBinding(c.findMethod("testDynamicBinding4", "(Lsoba/testdata/inheritance1/C;)V"));
		checkLoopBinding(c.findMethod("testDynamicBinding5", "(Lsoba/testdata/inheritance1/C;)V"));
	}
	
	@Test
	public void testMethodParamType() {
		ClassInfo c = program.getClassInfo(CLASS_E);
		checkParamBinding(c.findMethod("testDynamicBinding4", "(Lsoba/testdata/inheritance1/C;)V"));
		checkParamBinding(c.findMethod("testDynamicBinding5", "(Lsoba/testdata/inheritance1/C;)V"));
	}

	@Test
	public void testgetReceiverTypeAtCallsite() {
		ClassInfo c = program.getClassInfo(CLASS_C);
		MethodInfo m = c.findMethod("main", "([Ljava/lang/String;)V");
		InsnList instructions = m.getMethodNode().instructions;
		int count = 0;
		for (int i = 0; i < instructions.size(); i++) {
			if (instructions.get(i).getOpcode() == Opcodes.INVOKEVIRTUAL) {
				if (count == 0) {
					TypeSet typeSet = resolver.getReceiverTypeAtCallsite(m, i);
					assertThat(typeSet.getTypeCount(), is(0));
				} else {
					TypeSet typeSet = resolver.getReceiverTypeAtCallsite(m, i);
					assertThat(typeSet.getTypeCount(), is(1));
				}
				count++;
			}
		}
	}
	
	@Test
	public void testReflection() {
		ClassInfo c = program.getClassInfo("soba/testdata/ReflectionCode");
		MethodInfo m = c.findMethod("newInstanceUser", "()V");
		InsnList instructions = m.getMethodNode().instructions;
		int counter = 0;
		for (int i=0; i<instructions.size(); ++i) {
			if (instructions.get(i).getOpcode() == Opcodes.INVOKEVIRTUAL) {
				MethodInsnNode call = (MethodInsnNode)instructions.get(i);
				if (call.name.equals("toString")) {
					MethodInfo[] methods = resolver.resolveCall(m.getCallSite(i));
					counter++;
					if (counter == 1) {
						checkClasses(methods, CLASS_C, CLASS_D);
					} else {
						checkClasses(methods, CLASS_D);
					}
				}
			}
		}
		assertThat(counter, is(2));
	}
	
	@Test
	public void testReflection2() {
		ClassInfo c = program.getClassInfo("soba/testdata/ReflectionCode");
		MethodInfo m = c.findMethod("newInstanceUser2", "()V");
		InsnList instructions = m.getMethodNode().instructions;
		int counter = 0;
		for (int i=0; i<instructions.size(); ++i) {
			if (instructions.get(i).getOpcode() == Opcodes.INVOKEVIRTUAL) {
				MethodInsnNode call = (MethodInsnNode)instructions.get(i);
				if (call.name.equals("toString")) {
					MethodInfo[] methods = resolver.resolveCall(m.getCallSite(i));
					assertThat(methods.length, is(greaterThan(1))); // at least C and D implements toString.
					counter++;
				}
			}
		}
		assertThat(counter, is(1));
	}
}
