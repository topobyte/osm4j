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

package de.topobyte.osm4j.utils.executables;

import java.io.IOException;
import java.io.OutputStream;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.access.OsmOutputStream;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.OsmBounds;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.utils.AbstractTaskSingleInputStreamOutput;
import de.topobyte.osm4j.utils.OsmIoUtils;
import de.topobyte.osm4j.utils.StreamUtil;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

public class OsmEntitySplit extends AbstractTaskSingleInputStreamOutput
{

	private static final String OPTION_OUTPUT_NODES = "output_nodes";
	private static final String OPTION_OUTPUT_WAYS = "output_ways";
	private static final String OPTION_OUTPUT_RELATIONS = "output_relations";

	@Override
	protected String getHelpMessage()
	{
		return OsmEntitySplit.class.getSimpleName() + " [options]";
	}

	public static void main(String[] args) throws IOException
	{
		OsmEntitySplit task = new OsmEntitySplit();
		task.setup(args);

		task.init();

		task.run();

		task.finish();
	}

	private boolean passNodes = false;
	private boolean passWays = false;
	private boolean passRelations = false;

	private String pathNodes = null;
	private String pathWays = null;
	private String pathRelations = null;

	private OutputStream osNodes = null;
	private OutputStream osWays = null;
	private OutputStream osRelations = null;

	private OsmOutputStream oosNodes = null;
	private OsmOutputStream oosWays = null;
	private OsmOutputStream oosRelations = null;

	public OsmEntitySplit()
	{
		// @formatter:off
		OptionHelper.add(options, OPTION_OUTPUT_NODES, true, false, "the file to write nodes to");
		OptionHelper.add(options, OPTION_OUTPUT_WAYS, true, false, "the file to write ways to");
		OptionHelper.add(options, OPTION_OUTPUT_RELATIONS, true, false, "the file to write relations to");
		// @formatter:on
	}

	@Override
	public void setup(String[] args)
	{
		super.setup(args);

		if (line.hasOption(OPTION_OUTPUT_NODES)) {
			passNodes = true;
			pathNodes = line.getOptionValue(OPTION_OUTPUT_NODES);
		}
		if (line.hasOption(OPTION_OUTPUT_WAYS)) {
			passWays = true;
			pathWays = line.getOptionValue(OPTION_OUTPUT_WAYS);
		}
		if (line.hasOption(OPTION_OUTPUT_RELATIONS)) {
			passRelations = true;
			pathRelations = line.getOptionValue(OPTION_OUTPUT_RELATIONS);
		}

		if (pathNodes == null && pathWays == null && pathRelations == null) {
			System.out
					.println("You should specify an output for at least one entity");
			System.exit(1);
		}
	}

	@Override
	public void init() throws IOException
	{
		super.init();

		if (passNodes) {
			OutputStream osNodes = StreamUtil.bufferedOutputStream(pathNodes);
			oosNodes = OsmIoUtils.setupOsmOutput(osNodes, outputFormat,
					writeMetadata, pbfConfig, tboConfig);
		}
		if (passWays) {
			OutputStream osWays = StreamUtil.bufferedOutputStream(pathWays);
			oosWays = OsmIoUtils.setupOsmOutput(osWays, outputFormat,
					writeMetadata, pbfConfig, tboConfig);
		}
		if (passRelations) {
			OutputStream osRelations = StreamUtil
					.bufferedOutputStream(pathRelations);
			oosRelations = OsmIoUtils.setupOsmOutput(osRelations, outputFormat,
					writeMetadata, pbfConfig, tboConfig);
		}
	}

	public void run() throws IOException
	{
		OsmIterator iterator = createIterator();

		if (iterator.hasBounds()) {
			OsmBounds bounds = iterator.getBounds();
			if (passNodes) {
				oosNodes.write(bounds);
			}
			if (passWays) {
				oosWays.write(bounds);
			}
			if (passRelations) {
				oosRelations.write(bounds);
			}
		}

		loop: while (iterator.hasNext()) {
			EntityContainer entityContainer = iterator.next();
			switch (entityContainer.getType()) {
			case Node:
				if (passNodes) {
					oosNodes.write((OsmNode) entityContainer.getEntity());
				}
				break;
			case Way:
				if (passWays) {
					oosWays.write((OsmWay) entityContainer.getEntity());
				} else if (!passRelations) {
					break loop;
				}
				break;
			case Relation:
				if (passRelations) {
					oosRelations.write((OsmRelation) entityContainer
							.getEntity());
				} else {
					break loop;
				}
				break;
			}
		}
	}

	@Override
	public void finish() throws IOException
	{
		if (passNodes) {
			oosNodes.complete();
			osNodes.close();
		}
		if (passWays) {
			oosWays.complete();
			osWays.close();
		}
		if (passRelations) {
			oosRelations.complete();
			osRelations.close();
		}
		super.finish();
	}

}
