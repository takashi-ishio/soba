package soba.core.method;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnList;

import soba.core.ClassInfo;
import soba.core.JavaProgram;
import soba.core.JavaProgramTest;
import soba.core.MethodInfo;
import soba.core.method.LocalVariables;

public class LocalVariablesTest {

	private static JavaProgram program;
	private static ClassInfo c;
	private static LocalVariables variables;
	private static InsnList instructions;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		program = JavaProgramTest.readExampleProgram();
		c = program.getClassInfo("soba/testdata/DefUseTestData");
		MethodInfo m = c.findMethod("localDataDependence", "()V");
		instructions = m.getMethodNode().instructions;
		variables = new LocalVariables(m.getDataDependence(), m.getMethodNode());
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetVariableEntryCount() {
		assertThat(variables.getVariableEntryCount(), is(3));
	}

	@Test
	public void testGetVariableName() {
		assertThat(variables.getVariableName(0), is("b"));
		assertThat(variables.getVariableName(1), is("x"));
		assertThat(variables.getVariableName(2), is("x"));
	}

	@Test
	public void testGetVariableType() {
		assertThat(variables.getVariableType(0), is("boolean"));
		assertThat(variables.getVariableType(1), is("int"));
		assertThat(variables.getVariableType(2), is("int"));
	}

	@Test
	public void testGetVariableIndex() {
		assertThat(variables.getVariableIndex(0), is(2)); // variable b
		assertThat(variables.getVariableIndex(1), is(1)); // variable x
		assertThat(variables.getVariableIndex(2), is(1)); // variable x
	}

	@Test
	public void testIsObjectVariable() {
		assertThat(variables.isObjectVariable(0), is(false));
		assertThat(variables.isObjectVariable(1), is(false));
		assertThat(variables.isObjectVariable(2), is(false));
	}

	@Test
	public void testIsArrayVariable() {
		assertThat(variables.isArrayVariable(0), is(false));
		assertThat(variables.isArrayVariable(1), is(false));
		assertThat(variables.isArrayVariable(2), is(false));
	}

	@Test
	public void testHasNoDataDependence() {
		assertThat(variables.hasNoDataDependence(0), is(false));
		assertThat(variables.hasNoDataDependence(1), is(false));
		assertThat(variables.hasNoDataDependence(2), is(false));
	}

	@Test
	public void testIsParameter() {
		assertThat(variables.isParameter(0), is(false));
		assertThat(variables.isParameter(1), is(false));
		assertThat(variables.isParameter(2), is(false));
	}

	@Test
	public void testFindEntryForInstruction() {
		int storeCount = 0;
		int loadCount = 0;
		for (int i = 0; i < instructions.size(); i++) {
			if (instructions.get(i).getOpcode() == Opcodes.ISTORE) {
				if (storeCount == 0) {
					assertThat(variables.findEntryForInstruction(i), is(0));
				} else if (storeCount == 1 || storeCount == 3) {
					assertThat(variables.findEntryForInstruction(i), is(2));
				} else {
					assertThat(variables.findEntryForInstruction(i), is(1));
				}
				storeCount++;
			} else if (instructions.get(i).getOpcode() == Opcodes.ILOAD) {
				if (loadCount == 0) {
					assertThat(variables.findEntryForInstruction(i), is(0));
				} else if (loadCount == 1) {
					assertThat(variables.findEntryForInstruction(i), is(1));
				} else {
					assertThat(variables.findEntryForInstruction(i), is(2));
				}
				loadCount++;
			} else {
				assertThat(variables.findEntryForInstruction(i), is(-1));
			}
		}
	}

}
