package soba.testdata;

import java.io.File;

public class TestUtil {

	public static File getTestFile(String relativePath) {
		return new File("target/test-classes/", relativePath);
	}
}
