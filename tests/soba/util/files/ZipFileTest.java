package soba.util.files;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assume.*;

import java.io.File;

import org.junit.Test;

import soba.util.TestUtil;

public class ZipFileTest {

	@Test
	public void testIsZipFile() {
		File file = TestUtil.getTestFile("soba/util/files/ZipFileTest.class");
		assumeThat(file.exists(), is(true));
		assertThat(ZipFile.isZipFile(file), is(false));
	}

	@Test
	public void testIsClassFile() {
		File classFile = TestUtil.getTestFile("soba/util/files/ZipFileTest.class");
		assumeThat(classFile.exists(), is(true));
		assertThat(ZipFile.isClassFile(classFile), is(true));
	}

}
