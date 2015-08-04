package soba.core.vta;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import org.junit.BeforeClass;
import org.junit.Test;

import soba.core.ClassInfo;
import soba.core.JavaProgram;
import soba.core.JavaProgramTest;
import soba.core.MethodInfo;
import soba.core.method.CallSite;
import soba.core.vta.CallSiteVertices;

public class CallSiteVerticesTest {

	private static JavaProgram program;
	private static MethodInfo m;
	private static CallSiteVertices v;
	private static final int startID = 1;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		program = JavaProgramTest.readExampleProgram();
		ClassInfo c = program.getClassInfo("soba/testdata/ObjectTransferCode");
		m = c.findMethod("newObject", "(I)[[I");
		CallSite callSite = null;
		for (int i = 0; i < m.getInstructionCount(); i++) {
			CallSite cs = m.getCallSite(i);
			if (cs != null && cs.getMethodName().equals("m2")) {
				callSite = cs;
				break;
			}
		}
		v = new CallSiteVertices(callSite, startID);
	}

//	@Test
//	public void testGetCallSite() {
//		fail("Not yet implemented");
//	}

	@Test
	public void testIsObjectParam() {
		assertThat(v.isObjectParam(0), is(true));
		assertThat(v.isObjectParam(1), is(true));
	}

	@Test
	public void testGetParamVertexId() {
		assertThat(v.getParamVertexId(0), is(startID));
		assertThat(v.getParamVertexId(1), is(startID + 1));
	}

	@Test
	public void testGetParamCount() {
		assertThat(v.getParamCount(), is(2));
	}

	@Test
	public void testHasReturnValue() {
		assertThat(v.hasReturnValue(), is(true));
	}

	@Test
	public void testGetReturnValueVertex() {
		assertThat(v.getReturnValueVertex(), is(startID + 2));
	}

	@Test
	public void testGetVertexCount() {
		assertThat(v.getVertexCount(), is(3));
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
		assertThat(v.getTypeName(1), is("soba/testdata/ObjectTransferCode1"));
	}

	@Test
	public void testGetReturnValueTypeName() {
		assertThat(v.getReturnValueTypeName(), is("soba/testdata/ObjectTransferCode"));
	}

}
