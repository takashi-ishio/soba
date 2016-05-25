package soba.util.files;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUtil {

	/**
	 * Read all the data in the stream and creates a byte array. 
	 * The method does not close the stream.
	 */
	public static byte[] readFully(InputStream stream) throws IOException {
		byte[] buf = new byte[4096];
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		int n;
		while ((n = stream.read(buf, 0, buf.length)) > 0) {
			buffer.write(buf, 0, n);
		}
		return buffer.toByteArray();
	}

}
