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

import de.topobyte.melon.io.StreamUtil;
import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.access.OsmOutputStream;
import de.topobyte.osm4j.core.model.iface.OsmBounds;
import de.topobyte.osm4j.utils.OsmIoUtils;
import de.topobyte.osm4j.utils.OsmOutputConfig;

public class AbstractEntitySplitter
{

	protected OsmIterator iterator;
	private Path pathNodes;
	private Path pathWays;
	private Path pathRelations;

	protected boolean passNodes = false;
	protected boolean passWays = false;
	protected boolean passRelations = false;

	private OutputStream osNodes = null;
	private OutputStream osWays = null;
	private OutputStream osRelations = null;

	protected OsmOutputStream oosNodes = null;
	protected OsmOutputStream oosWays = null;
	protected OsmOutputStream oosRelations = null;

	private OsmOutputConfig outputConfig;

	public AbstractEntitySplitter(OsmIterator iterator, Path pathNodes,
			Path pathWays, Path pathRelations, OsmOutputConfig outputConfig)
	{
		this.iterator = iterator;
		this.pathNodes = pathNodes;
		this.pathWays = pathWays;
		this.pathRelations = pathRelations;
		this.outputConfig = outputConfig;

		passNodes = pathNodes != null;
		passWays = pathWays != null;
		passRelations = pathRelations != null;
	}

	protected void init() throws IOException
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

	protected void passBounds() throws IOException
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
	}

	protected void finish() throws IOException
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
