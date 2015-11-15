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

package de.topobyte.osm4j.extra.nodearray;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.utils.AbstractExecutable;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

public class NodeArrayShow extends AbstractExecutable
{

	private static final String OPTION_INPUT = "input";
	private static final String OPTION_TYPE = "type";
	private static final String OPTION_ID = "id";

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
		return NodeArrayShow.class.getSimpleName() + " [options]";
	}

	public static void main(String[] args) throws IOException
	{
		NodeArrayShow task = new NodeArrayShow();

		task.setup(args);

		task.init();

		task.execute();
	}

	private String inputPath;
	private NodeArrayType type;
	private long id;

	private File file;
	private NodeArray array;

	public NodeArrayShow()
	{
		// @formatter:off
		OptionHelper.add(options, OPTION_INPUT, true, true, "input file");
		OptionHelper.add(options, OPTION_TYPE, true, true, POSSIBLE_TYPES);
		OptionHelper.add(options, OPTION_ID, true, true, "a node id");
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

		String argId = line.getOptionValue(OPTION_ID);
		id = Long.parseLong(argId);
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
		System.out.println("Queried id: " + id);
		if (!array.contains(id)) {
			System.out.println("Not found");
		} else {
			OsmNode node = array.get(id);
			System.out.println("lon: " + node.getLongitude());
			System.out.println("lat: " + node.getLatitude());
		}

		array.close();
	}

}
