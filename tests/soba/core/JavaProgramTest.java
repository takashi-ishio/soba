package soba.core;

import java.io.File;

import soba.core.JavaProgram;
import soba.util.files.Directory;
import soba.util.files.IClassList;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import org.junit.BeforeClass;
import org.junit.Test;

public class JavaProgramTest implements ExampleProgram {

	private static JavaProgram program;
	
	public static JavaProgram readExampleProgram() {
		Directory dir = new Directory(new File("bin/soba/testdata/"));
		JavaProgram program = new JavaProgram(new IClassList[] {dir});
		return program;
	}
	
	@BeforeClass
	public static void setUpBeforeClass() {
		program = readExampleProgram();
	}

	@Test
	public void testJavaProgram01() {
		assertThat(program.getClasses(), is(notNullValue()));
		assertThat(program.getClasses(), hasSize(22));

		assertThat(program.getFiltered(), is(empty()));
		assertThat(program.getDuplicated(), is(empty()));
		assertThat(program.getErrors(), is(empty()));

		assertThat(program.getClassHierarchy(), is(notNullValue()));

		assertThat(program.getClassInfo(CLASS_C), is(notNullValue()));
		assertThat(program.getClassInfo(CLASS_D), is(notNullValue()));
		assertThat(program.getClassInfo(CLASS_E), is(notNullValue()));
		assertThat(program.getClassInfo(CLASS_F), is(notNullValue()));
		assertThat(program.getClassInfo(CLASS_G), is(notNullValue()));
		assertThat(program.getClassInfo(CLASS_H), is(notNullValue()));
		assertThat(program.getClassInfo(CLASS_I), is(notNullValue()));
		assertThat(program.getClassInfo(CLASS_J), is(notNullValue()));
		assertThat(program.getClassInfo(CLASS_K), is(notNullValue()));
		assertThat(program.getClassInfo("NotExistClass"), is(nullValue()));
	}

}
