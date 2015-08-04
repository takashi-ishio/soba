package soba.util.files;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assume.*;

import org.junit.Test;
import java.io.File;

public class DirectoryTest {
	
	@Test
	public void testListSubdirectories() {
		File f = new File("src");
		assumeThat(f.isDirectory(), is(true));
		
		Directory[] dir = Directory.listSubdirectories(f, 0);
		assertThat(dir.length, is(1));
		assertThat(dir[0].getDirectory(), is(f));

		File soba = new File(f, "soba");
		Directory[] subdirs = Directory.listSubdirectories(f, 1);
		assertThat(dir.length, is(1));
		assertThat(subdirs[0].getDirectory(), is(soba));
	}

}
