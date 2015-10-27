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

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.utils.AbstractTaskSingleInputIterator;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

public class CreateNodeArray extends AbstractTaskSingleInputIterator
{

	private static final String OPTION_OUTPUT = "output";
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
		return CreateNodeArray.class.getSimpleName() + " [options]";
	}

	public static void main(String[] args) throws IOException
	{
		CreateNodeArray task = new CreateNodeArray();

		task.setup(args);

		task.init();

		task.initOutput();

		task.execute();

		task.finish();
	}

	private String outputPath;

	private NodeArrayType type;

	private NodeArrayWriter writer;

	public CreateNodeArray()
	{
		// @formatter:off
		OptionHelper.add(options, OPTION_OUTPUT, true, true, "output file");
		OptionHelper.add(options, OPTION_TYPE, true, true, POSSIBLE_TYPES);
		// @formatter:on
	}

	@Override
	protected void setup(String[] args)
	{
		super.setup(args);

		outputPath = line.getOptionValue(OPTION_OUTPUT);

		String argType = line.getOptionValue(OPTION_TYPE);
		type = typeMap.get(argType);
		if (type == null) {
			System.out.println("Please specify a valid type argument: "
					+ POSSIBLE_TYPES);
			System.exit(1);
		}
	}

	private void initOutput() throws FileNotFoundException
	{
		OutputStream fos = new FileOutputStream(outputPath);
		OutputStream bos = new BufferedOutputStream(fos);
		DataOutputStream out = new DataOutputStream(bos);

		switch (type) {
		default:
		case DOUBLE:
			writer = new NodeArrayWriterDouble(out);
			break;
		case FLOAT:
			writer = new NodeArrayWriterFloat(out);
			break;
		case INTEGER:
			writer = new NodeArrayWriterInteger(out);
			break;
		case SHORT:
			writer = new NodeArrayWriterShort(out);
			break;
		}
	}

	private void execute() throws IOException
	{
		while (inputIterator.hasNext()) {
			EntityContainer container = inputIterator.next();
			if (container.getType() != EntityType.Node) {
				break;
			}
			OsmNode node = (OsmNode) container.getEntity();
			writer.write(node);
		}
	}

	@Override
	public void finish() throws IOException
	{
		writer.finish();
		super.finish();
	}

}
