package soba.util.files;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assume.*;

import java.io.File;

import org.junit.Test;

public class ZipFileTest {

	@Test
	public void testIsZipFile() {
		File jarFile = new File("lib/asm-debug-all-5.0.3.jar");
		assumeThat(jarFile.exists(), is(true));
		assertThat(ZipFile.isZipFile(jarFile), is(true));
		
		File sourceFile = new File("tests/soba/util/files/ZipFileTest.java");
		assumeThat(sourceFile.exists(), is(true));
		assertThat(ZipFile.isZipFile(sourceFile), is(false));
	}

	@Test
	public void testIsClassFile() {
		File classFile = new File("bin/soba/util/files/ZipFileTest.class");
		assumeThat(classFile.exists(), is(true));
		assertThat(ZipFile.isClassFile(classFile), is(true));
		
		File sourceFile = new File("tests/soba/util/files/ZipFileTest.java");
		assumeThat(sourceFile.exists(), is(true));
		assertThat(ZipFile.isClassFile(sourceFile), is(false));
	}

}
