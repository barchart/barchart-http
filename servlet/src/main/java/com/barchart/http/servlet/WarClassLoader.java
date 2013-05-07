package com.barchart.http.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Loads classes from war files. See
 * http://www2.java.net/blog/2008/07/25/how-load-classes-jar-or-zip
 */
public final class WarClassLoader extends ClassLoader {
	private final ZipFile file;

	public WarClassLoader(String filename) throws IOException {
		this.file = new ZipFile(filename);
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		ZipEntry entry =
				this.file.getEntry("WEB-INF/classes/" + name.replace('.', '/')
						+ ".class");
		if (entry == null) {
			throw new ClassNotFoundException(name);
		}
		try {
			byte[] array = new byte[1024];
			InputStream in = this.file.getInputStream(entry);
			ByteArrayOutputStream out = new ByteArrayOutputStream(array.length);
			int length = in.read(array);
			while (length > 0) {
				out.write(array, 0, length);
				length = in.read(array);
			}
			return defineClass(name, out.toByteArray(), 0, out.size());
		} catch (IOException exception) {
			throw new ClassNotFoundException(name, exception);
		}
	}
}