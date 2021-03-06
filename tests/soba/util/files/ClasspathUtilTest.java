package soba.util.files;


import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import soba.core.ClassInfo;
import soba.util.TestUtil;

public class ClasspathUtilTest {

	@Test
	public void testEnumerateSystemClasspath() {
		List<String> systemList = ClasspathUtil.enumerateSystemClasspath();
		assertThat(systemList, is(notNullValue()));
	}
	
	@Test
	public void testMerge() { 
		IClassList[] e1 = ClasspathUtil.getClassList(new String[] { "." });
		IClassList[] e2 = ClasspathUtil.getClassList(new String[] { "..", "." });
		IClassList[] e3 = ClasspathUtil.merge(e1, e2);
		assertThat(e1[0] == e3[0], is(true));
		assertThat(e2[0] == e3[1], is(true));
		assertThat(e2[1] == e3[2], is(true));
	}
	
	@Test
	public void testGetClassList() {
		
		String path = TestUtil.getTestFile("soba/testdata").getPath();
		String[] fileArray = new String[]{path};
		IClassList[] results1 = ClasspathUtil.getClassList(fileArray);
		assertThat(results1, is(arrayWithSize(1)));
		IClassList[] results2 = ClasspathUtil.getClassList(fileArray, "");
		assertThat(results2, is(arrayWithSize(1)));
		
		List<String> fileList = new ArrayList<>();
		fileList.add(path);
		IClassList[] results3 = ClasspathUtil.getClassList(fileList);
		assertThat(results3, is(arrayWithSize(1)));
		IClassList[] results4 = ClasspathUtil.getClassList(fileList, null);
		assertThat(results4, is(arrayWithSize(1)));
		
		String[] classFile = new String[]{TestUtil.getTestFile("soba/testdata/DefUseTestData.class").getPath()};
		IClassList[] results6 = ClasspathUtil.getClassList(classFile);
		assertThat(results6, is(arrayWithSize(1)));
		
		String[] appFiles = new String[]{TestUtil.getTestFile("soba/testdata/DefUseTestData.class").getPath()};
		String[] libFiles = new String[]{TestUtil.getTestFile("soba/testdata").getPath()};
		IClassList[] results7 = ClasspathUtil.getClassList(appFiles, libFiles);
		assertThat(results7, is(arrayWithSize(2)));
		assertThat(results7[1].getLabel(), is(ClassInfo.LIBRARY_LABEL));
	}
}
