package soba.core;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import soba.core.ClassInfo;
import soba.core.FieldInfo;
import soba.core.JavaProgram;

public class FieldInfoTest {

	private static FieldInfo fi;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		JavaProgram program = JavaProgramTest.readExampleProgram();
		ClassInfo c = program.getClassInfo(ExampleProgram.CLASS_C);
		fi = c.getField(0);
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
	public void testGetPackageName() {
		assertThat(fi.getPackageName(), is("soba/testdata/inheritance1"));
	}

	@Test
	public void testGetClassName() {
		assertThat(fi.getClassName(), is("soba/testdata/inheritance1/C"));
	}

	@Test
	public void testGetFieldName() {
		assertThat(fi.getFieldName(), is("x"));
	}

	@Test
	public void testGetDescriptor() {
		assertThat(fi.getDescriptor(), is("I"));
	}

	@Test
	public void testGetFieldTypeName() {
		assertThat(fi.getFieldTypeName(), is("int"));
	}

}
