package de.topobyte.osm4j.xml.test;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

public class Util
{

	public static String read(String filename) throws IOException
	{
		ClassLoader classloader = Thread.currentThread()
				.getContextClassLoader();

		InputStream input = classloader.getResourceAsStream(filename);
		String text = IOUtils.toString(input);
		input.close();

		return text;
	}

	public static InputStream stream(String filename)
	{
		ClassLoader classloader = Thread.currentThread()
				.getContextClassLoader();

		return classloader.getResourceAsStream(filename);
	}

}
