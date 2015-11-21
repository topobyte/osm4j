// Copyright 2015 Sebastian Kuerten
//
// This file is part of osm4j.
//
// osm4j is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// osm4j is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with osm4j. If not, see <http://www.gnu.org/licenses/>.

package de.topobyte.osm4j.extra.executables;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.topobyte.osm4j.extra.nodearray.NodeArray;
import de.topobyte.osm4j.extra.nodearray.NodeArrayDouble;
import de.topobyte.osm4j.extra.nodearray.NodeArrayFloat;
import de.topobyte.osm4j.extra.nodearray.NodeArrayInteger;
import de.topobyte.osm4j.extra.nodearray.NodeArrayShort;
import de.topobyte.osm4j.extra.nodearray.NodeArrayType;
import de.topobyte.osm4j.utils.AbstractExecutable;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

public class NodeArrayInfo extends AbstractExecutable
{

	private static final String OPTION_INPUT = "input";
	private static final String OPTION_TYPE = "type";

	private static Map<String, NodeArrayType> typeMap = new HashMap<>();

	static {
		typeMap.put("double", NodeArrayType.DOUBLE);
		typeMap.put("float", NodeArrayType.FLOAT);
		typeMap.put("int", NodeArrayType.INTEGER);
		typeMap.put("short", NodeArrayType.SHORT);
	}

	private static String POSSIBLE_TYPES = "double, float, int, short";

	@Override
	protected String getHelpMessage()
	{
		return NodeArrayInfo.class.getSimpleName() + " [options]";
	}

	public static void main(String[] args) throws IOException
	{
		NodeArrayInfo task = new NodeArrayInfo();

		task.setup(args);

		task.init();

		task.execute();
	}

	private String inputPath;
	private NodeArrayType type;

	private File file;
	private NodeArray array;

	public NodeArrayInfo()
	{
		// @formatter:off
		OptionHelper.add(options, OPTION_INPUT, true, true, "input file");
		OptionHelper.add(options, OPTION_TYPE, true, true, POSSIBLE_TYPES);
		// @formatter:on
	}

	@Override
	protected void setup(String[] args)
	{
		super.setup(args);

		inputPath = line.getOptionValue(OPTION_INPUT);

		String argType = line.getOptionValue(OPTION_TYPE);
		type = typeMap.get(argType);
		if (type == null) {
			System.out.println("Please specify a valid type argument: "
					+ POSSIBLE_TYPES);
			System.exit(1);
		}
	}

	private void init() throws IOException
	{
		file = new File(inputPath);

		switch (type) {
		default:
		case DOUBLE:
			array = new NodeArrayDouble(file);
			break;
		case FLOAT:
			array = new NodeArrayFloat(file);
			break;
		case INTEGER:
			array = new NodeArrayInteger(file);
			break;
		case SHORT:
			array = new NodeArrayShort(file);
			break;
		}
	}

	private void execute() throws IOException
	{
		long length = file.length();
		long entries = length / array.bytesPerRecord();

		System.out.println("Size: " + entries);

		array.close();
	}

}
