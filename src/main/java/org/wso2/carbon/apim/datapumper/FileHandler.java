package org.wso2.carbon.apim.datapumper;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileHandler implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final ThreadLocal<FileHandler> threadLocal = new ThreadLocal<FileHandler>() {
		protected FileHandler initialValue() {
			return new FileHandler();
		}
	};

	private FileHandler() {

	}

	public static FileHandler getInstance() {
		return threadLocal.get();
	}

	public static InputStream getResourceAsStream(final String fileName) {
		InputStream is = null;
		try {
			is = new FileInputStream(fileName);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return is;
	}

	public static String readInputStream(final InputStream is) throws Exception {

		if (is == null) {
			return null;
		}

		StringBuffer buf = new StringBuffer();
		Reader reader = null;
		try {
			reader = new InputStreamReader(is, "UTF-8");
			char[] arr = new char[100 * 1024]; // 8K at a time

			int numChars;

			while ((numChars = reader.read(arr, 0, arr.length)) > 0) {
				buf.append(arr, 0, numChars);
			}
		} finally {
			try {
				close(reader);
				close(is);
			} catch (Exception ex) {

			}
		}

		return buf.toString();

	}

	public static void writingToFile(final String fileLocation,
			final String output) throws IOException {

		BufferedWriter bw = null;
		FileWriter fw = null;
		try {
			File file = new File(fileLocation);

			System.out.println("output : " + output);

			// if file doesnt exists, then create it
			if (file.exists()) {
				file.delete();
			}
			file.createNewFile();

			fw = new FileWriter(file.getAbsoluteFile());
			bw = new BufferedWriter(fw);
			bw.write(output);
		} finally {
			close(bw);
		}

	}

	public static Properties loadResourceProperties(final String propertyFile)
			throws IOException {
		Properties prop = new Properties();
		InputStream is = null;
		try {
			is = ClassLoader.getSystemResourceAsStream(propertyFile);
			prop.load(is);
		} finally {
			close(is);
		}

		System.out.println(prop.toString());
		return prop;

	}

	public static void close(Closeable closer) {
		try {
			closer.close();
		} catch (Exception ex) {

		}
	}
	
	public static String readZipFile(final String filePath,
			final String expectedFileName) throws IOException {
		// create a buffer to improve copy performance later.
		byte[] buffer = new byte[1000 * 8];	

		// open the zip file stream
		InputStream theFile = new FileInputStream(filePath);
		ZipInputStream zip = new ZipInputStream(theFile);
		ByteArrayOutputStream streamBuilder = new ByteArrayOutputStream();
		try {

			// now iterate through each item in the stream. The get next
			// entry call will return a ZipEntry for each file in the
			// stream
			ZipEntry entry;
			do {
				entry = zip.getNextEntry();
			} while (entry != null && !expectedFileName.equals(entry.getName()));

			
			int bytesRead;

			try {
				while ((bytesRead = zip.read(buffer)) != -1) {
					streamBuilder.write(buffer, 0, bytesRead);
				}
			} catch (IOException ex) {
				throw ex;
			}

			zip.closeEntry();
			

		} finally {
			if (zip != null)
				zip.close();
		}
		
		return streamBuilder.toString();
	}

}
