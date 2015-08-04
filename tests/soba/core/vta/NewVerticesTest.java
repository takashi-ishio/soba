package soba.core.vta;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import org.junit.BeforeClass;
import org.junit.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnList;

import soba.core.ClassInfo;
import soba.core.JavaProgram;
import soba.core.JavaProgramTest;
import soba.core.MethodInfo;
import soba.core.vta.NewVertices;
import soba.core.vta.VTAResolver;

public class NewVerticesTest {

	private static JavaProgram program;
	private static MethodInfo m;
	private static NewVertices v;
	private static final int startID = 1;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		program = JavaProgramTest.readExampleProgram();
		ClassInfo c = program.getClassInfo("soba/testdata/ObjectTransferCode");
		m = c.findMethod("newObject", "(I)[[I");
		v = new NewVertices(m.getMethodNode().instructions, startID);
	}

	@Test
	public void testGetNewInstructionVertex() {
		int count = startID;
		InsnList instructions = m.getMethodNode().instructions;
		for (int i = 0; i < instructions.size(); i++) {
			switch (instructions.get(i).getOpcode()) {
			case Opcodes.NEW:
			case Opcodes.ANEWARRAY:
			case Opcodes.MULTIANEWARRAY:
				assertThat(v.getNewInstructionVertex(i), is(count));
				count++;
				break;
			default:
				assertThat(v.getNewInstructionVertex(i), is(VTAResolver.VERTEX_ERROR));
			}
		}
	}

	@Test
	public void testGetVertex() {
		assertThat(v.getVertex(0), is(startID));
		assertThat(v.getVertex(1), is(startID + 1));
		assertThat(v.getVertex(2), is(startID + 2));
	}

	@Test
	public void testGetTypeName() {
		assertThat(v.getTypeName(0), is("soba/testdata/ObjectTransferCode"));
		assertThat(v.getTypeName(1), is("soba/testdata/ObjectTransferCode[]"));
		assertThat(v.getTypeName(2), is("int[][]"));
	}

	@Test
	public void testGetVertexCount() {
		assertThat(v.getVertexCount(), is(3));
	}

}
