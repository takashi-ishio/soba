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
import soba.core.vta.MethodVertices;
import soba.core.vta.VTAResolver;

public class MethodVerticesTest {

	private static JavaProgram program;
	private static MethodInfo m;
	private static MethodVertices v;
	private static final int startID = 1;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		program = JavaProgramTest.readExampleProgram();
		ClassInfo c = program.getClassInfo("soba/testdata/ObjectTransferCode");
		m = c.findMethod("newObject", "(I)[[I");
		v = new MethodVertices(m, m.getDataDependence().getLocalVariables(), startID);
	}

	@Test
	public void testGetLocalVertex() {
		int storeCount = 0;
		int loadCount = 0;
		InsnList instructions = m.getMethodNode().instructions;
		for (int i = 0; i < instructions.size(); i++) {
			if (instructions.get(i).getOpcode() == Opcodes.ASTORE) {
				if (storeCount <= 2) {
					assertThat(v.getLocalVertex(i), is(startID + storeCount + 1));
				} else if (storeCount == 3) {
					assertThat(v.getLocalVertex(i), is(startID + 6));
				} else {
					assertThat(v.getLocalVertex(i), is(startID + storeCount));
				}
				storeCount++;
			} else if (instructions.get(i).getOpcode() == Opcodes.ALOAD) {
				if (loadCount == 0) {
					assertThat(v.getLocalVertex(i), is(startID + 1));
				} else if (loadCount == 1 || loadCount == 3) {
					assertThat(v.getLocalVertex(i), is(startID));
				} else if (loadCount == 2) {
					assertThat(v.getLocalVertex(i), is(startID + 2));
				} else {
					assertThat(v.getLocalVertex(i), is(startID + loadCount - 1));
				}
				loadCount++;
			} else {
				assertThat(v.getLocalVertex(i), is(VTAResolver.VERTEX_ERROR));
			}
		}
	}

	@Test
	public void testGetReturnVertex() {
		assertThat(v.getReturnVertex(), is(startID + 7));
	}

	@Test
	public void testGetFormalVertex() {
		assertThat(v.getFormalVertex(0), is(startID));
	}

	@Test
	public void testHasFormalVertex() {
		assertThat(v.hasFormalVertex(0), is(true));
		assertThat(v.hasFormalVertex(1), is(false));
	}

	@Test
	public void testGetVertexCount() {
		assertThat(v.getVertexCount(), is(8));
	}

	@Test
	public void testGetTypeName() {
		assertThat(v.getTypeName(0), is("soba/testdata/ObjectTransferCode"));
		assertThat(v.getTypeName(1), is("soba/testdata/ObjectTransferCode"));
		assertThat(v.getTypeName(2), is("soba/testdata/ObjectTransferCode"));
		assertThat(v.getTypeName(3), is("soba/testdata/ObjectTransferCode"));
		assertThat(v.getTypeName(4), is("soba/testdata/ObjectTransferCode[]"));
		assertThat(v.getTypeName(5), is("int[][]"));
		assertThat(v.getTypeName(6), is("java/lang/Object"));
		assertThat(v.getTypeName(7), is("int[][]"));
}

}
