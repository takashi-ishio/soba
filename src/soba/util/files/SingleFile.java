package soba.util.files;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class SingleFile implements IClassList {
	
	private File file;
	private String label;

	public SingleFile(File f) {
		assert f.isFile(): f.getAbsolutePath() + " is not a file.";
		this.file = f;
	}

	public void setLabel(String l) {
		this.label = l;
	}
	
	@Override
	public String getLabel() {
		return label;
	}
	
	
	@Override
	public void process(IClassListCallback c) {
		String filename = file.getAbsolutePath();
		if (c.isTarget(filename)) {
			try {
				FileInputStream binaryStream = new FileInputStream(file);
				c.process(filename, binaryStream);
				binaryStream.close();
			} catch (IOException e) {
				c.reportError(filename, e);
			}
		}
	}
}
