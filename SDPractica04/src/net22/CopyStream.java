/*
 * Urko Nalda Gil
 * 16626492-E
 */

package net22;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class CopyStream {
	public static void copyStream(InputStream is, OutputStream os) throws IOException {

		byte buffer[] = new byte[1024];
		int line = is.read(buffer);

		while(line != -1) {
			os.write(buffer, 0, line);
			line = is.read(buffer);
		}
	}
}


