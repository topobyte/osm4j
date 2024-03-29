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

package de.topobyte.osm4j.extra.datatree.ways;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.slimjars.dist.gnu.trove.iterator.TLongIterator;
import com.slimjars.dist.gnu.trove.iterator.TLongObjectIterator;
import com.slimjars.dist.gnu.trove.list.TLongList;
import com.slimjars.dist.gnu.trove.list.array.TLongArrayList;
import com.slimjars.dist.gnu.trove.set.TLongSet;
import com.slimjars.dist.gnu.trove.set.hash.TLongHashSet;

import de.topobyte.melon.io.StreamUtil;
import de.topobyte.osm4j.core.access.OsmIdIterator;
import de.topobyte.osm4j.core.access.OsmIdIteratorInput;
import de.topobyte.osm4j.core.access.OsmIteratorInput;
import de.topobyte.osm4j.core.dataset.InMemoryMapDataSet;
import de.topobyte.osm4j.core.dataset.MapDataSetLoader;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.IdContainer;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.extra.idlist.IdListOutputStream;
import de.topobyte.osm4j.extra.threading.Task;
import de.topobyte.osm4j.utils.OsmFile;
import de.topobyte.osm4j.utils.OsmFileInput;

public class MissingWayNodesFinderTask implements Task
{

	final static Logger logger = LoggerFactory
			.getLogger(MissingWayNodesFinderTask.class);

	private long counter = 0;
	private long found = 0;
	private long notFound = 0;

	private OsmFile fileNodes;
	private OsmFile fileWays;
	private Path fileOutput;
	private boolean verbose;

	public MissingWayNodesFinderTask(OsmFile inputNodes, OsmFile inputWays,
			Path fileOutput, boolean verbose)
	{
		this.fileNodes = inputNodes;
		this.fileWays = inputWays;
		this.fileOutput = fileOutput;
		this.verbose = verbose;
	}

	public long getCounter()
	{
		return counter;
	}

	public long getFound()
	{
		return found;
	}

	public long getNotFound()
	{
		return notFound;
	}

	@Override
	public void execute() throws IOException
	{
		if (verbose) {
			long nodesSize = fileNodes.getPath().toFile().length();
			logger.info(String.format("Loading nodes file of size: %.3fMB",
					nodesSize / 1024. / 1024.));
		}

		OsmIdIteratorInput nodeInput = new OsmFileInput(fileNodes)
				.createIdIterator();
		TLongSet nodeIds = read(nodeInput.getIterator());

		if (verbose) {
			long waysSize = fileWays.getPath().toFile().length();
			logger.info(String.format("Loading ways file of size: %.3fMB",
					waysSize / 1024. / 1024.));
		}

		OsmIteratorInput wayInput = new OsmFileInput(fileWays)
				.createIterator(false, false);
		InMemoryMapDataSet dataWays = MapDataSetLoader.read(wayInput, true,
				true, true);

		nodeInput.close();
		wayInput.close();

		if (verbose) {
			logger.info("Number of ways: " + dataWays.getWays().size());
		}

		TLongSet missingIds = new TLongHashSet();

		TLongObjectIterator<OsmWay> ways = dataWays.getWays().iterator();
		while (ways.hasNext()) {
			ways.advance();
			OsmWay way = ways.value();
			build(way, nodeIds, missingIds);
		}

		if (verbose) {
			logger.info("Sorting id list of size: " + missingIds.size());
		}

		TLongList missingIdList = new TLongArrayList(missingIds);
		missingIdList.sort();

		if (verbose) {
			logger.info("Writing missing ids");
		}
		OutputStream bos = StreamUtil.bufferedOutputStream(fileOutput);
		IdListOutputStream idOutput = new IdListOutputStream(bos);
		TLongIterator iterator = missingIdList.iterator();
		while (iterator.hasNext()) {
			idOutput.write(iterator.next());
		}
		idOutput.close();
	}

	private TLongSet read(OsmIdIterator idIterator)
	{
		TLongSet ids = new TLongHashSet();
		while (idIterator.hasNext()) {
			IdContainer container = idIterator.next();
			if (container.getType() != EntityType.Node) {
				break;
			}
			ids.add(container.getId());
		}
		return ids;
	}

	private void build(OsmWay way, TLongSet nodeIds, TLongSet missing)
	{
		for (int i = 0; i < way.getNumberOfNodes(); i++) {
			long nodeId = way.getNodeId(i);
			if (nodeIds.contains(nodeId)) {
				found++;
			} else {
				notFound++;
				missing.add(nodeId);
			}
		}
		counter++;
	}

}
