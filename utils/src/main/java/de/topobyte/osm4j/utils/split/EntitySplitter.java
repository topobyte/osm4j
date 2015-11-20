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

package de.topobyte.osm4j.utils.split;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.access.OsmOutputStream;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.OsmBounds;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.utils.OsmIoUtils;
import de.topobyte.osm4j.utils.OsmOutputConfig;
import de.topobyte.osm4j.utils.StreamUtil;

public class EntitySplitter
{

	private OsmIterator iterator;
	private Path pathNodes;
	private Path pathWays;
	private Path pathRelations;

	private boolean passNodes = false;
	private boolean passWays = false;
	private boolean passRelations = false;

	private OutputStream osNodes = null;
	private OutputStream osWays = null;
	private OutputStream osRelations = null;

	private OsmOutputStream oosNodes = null;
	private OsmOutputStream oosWays = null;
	private OsmOutputStream oosRelations = null;

	private OsmOutputConfig outputConfig;

	public EntitySplitter(OsmIterator iterator, Path pathNodes, Path pathWays,
			Path pathRelations)
	{
		this.iterator = iterator;
		this.pathNodes = pathNodes;
		this.pathWays = pathWays;
		this.pathRelations = pathRelations;

		passNodes = pathNodes != null;
		passWays = pathWays != null;
		passRelations = pathRelations != null;
	}

	public void execute() throws IOException
	{
		init();
		run();
		finish();
	}

	private void init() throws IOException
	{
		if (passNodes) {
			osNodes = StreamUtil.bufferedOutputStream(pathNodes);
			oosNodes = OsmIoUtils.setupOsmOutput(osNodes, outputConfig);
		}
		if (passWays) {
			osWays = StreamUtil.bufferedOutputStream(pathWays);
			oosWays = OsmIoUtils.setupOsmOutput(osWays, outputConfig);
		}
		if (passRelations) {
			osRelations = StreamUtil.bufferedOutputStream(pathRelations);
			oosRelations = OsmIoUtils.setupOsmOutput(osRelations, outputConfig);
		}
	}

	private void run() throws IOException
	{
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

	private void finish() throws IOException
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
	}

}
