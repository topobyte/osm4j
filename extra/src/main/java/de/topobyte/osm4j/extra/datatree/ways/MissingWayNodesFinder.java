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

import gnu.trove.iterator.TLongIterator;
import gnu.trove.iterator.TLongObjectIterator;
import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import de.topobyte.osm4j.core.access.OsmIdIterator;
import de.topobyte.osm4j.core.dataset.InMemoryMapDataSet;
import de.topobyte.osm4j.core.dataset.MapDataSetLoader;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.IdContainer;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.extra.datatree.DataTree;
import de.topobyte.osm4j.extra.datatree.DataTreeFiles;
import de.topobyte.osm4j.extra.datatree.DataTreeOpener;
import de.topobyte.osm4j.extra.datatree.Node;
import de.topobyte.osm4j.extra.idlist.IdListOutputStream;
import de.topobyte.osm4j.utils.FileFormat;
import de.topobyte.osm4j.utils.OsmIoUtils;
import de.topobyte.osm4j.utils.StreamUtil;

public class MissingWayNodesFinder
{

	private Path pathNodeTree;
	private Path pathWayTree;
	private Path pathOutputTree;

	private String fileNamesNodes;
	private String fileNamesWays;
	private String fileNamesOutput;

	private FileFormat inputFormatNodes;
	private FileFormat inputFormatWays;

	public MissingWayNodesFinder(Path pathNodeTree, Path pathWayTree,
			Path pathOutputTree, String fileNamesNodes, String fileNamesWays,
			String fileNamesOutput, FileFormat inputFormatNodes,
			FileFormat inputFormatWays)
	{
		this.pathNodeTree = pathNodeTree;
		this.pathWayTree = pathWayTree;
		this.pathOutputTree = pathOutputTree;
		this.fileNamesNodes = fileNamesNodes;
		this.fileNamesWays = fileNamesWays;
		this.fileNamesOutput = fileNamesOutput;
		this.inputFormatNodes = inputFormatNodes;
		this.inputFormatWays = inputFormatWays;
	}

	private List<Node> leafs;

	private long counter = 0;
	private long found = 0;
	private long notFound = 0;

	private long start = System.currentTimeMillis();

	private NumberFormat format = NumberFormat.getNumberInstance(Locale.US);

	public void execute() throws IOException
	{
		DataTree tree = DataTreeOpener.open(pathNodeTree.toFile());

		DataTreeFiles filesNodes = new DataTreeFiles(pathNodeTree,
				fileNamesNodes);
		DataTreeFiles filesWays = new DataTreeFiles(pathWayTree, fileNamesWays);
		DataTreeFiles filesOutput = new DataTreeFiles(pathOutputTree,
				fileNamesOutput);

		leafs = tree.getLeafs();

		int i = 0;
		for (Node leaf : leafs) {
			System.out.println(String.format("Processing leaf %d/%d", ++i,
					leafs.size()));

			File fileNodes = filesNodes.getFile(leaf);
			File fileWays = filesWays.getFile(leaf);
			File fileOutput = filesOutput.getFile(leaf);

			InputStream inputNodes = StreamUtil.bufferedInputStream(fileNodes);
			InputStream inputWays = StreamUtil.bufferedInputStream(fileWays);

			long nodesSize = fileNodes.length();
			System.out.println(String.format(
					"Loading nodes file of size: %.3fMB",
					nodesSize / 1024. / 1024.));

			OsmIdIterator idIterator = OsmIoUtils.setupOsmIdIterator(
					inputNodes, inputFormatNodes);
			TLongSet nodeIds = read(idIterator);

			long waysSize = fileWays.length();
			System.out.println(String.format(
					"Loading ways file of size: %.3fMB",
					waysSize / 1024. / 1024.));

			InMemoryMapDataSet dataWays = MapDataSetLoader.read(OsmIoUtils
					.setupOsmIterator(inputWays, inputFormatWays, false),
					false, false, false);

			inputNodes.close();
			inputWays.close();

			System.out.println("Number of ways: " + dataWays.getWays().size());

			TLongSet missingIds = new TLongHashSet();

			TLongObjectIterator<OsmWay> ways = dataWays.getWays().iterator();
			while (ways.hasNext()) {
				ways.advance();
				OsmWay way = ways.value();
				build(way, nodeIds, missingIds);
			}

			System.out.println("Sorting id list of size: " + missingIds.size());

			TLongList missingIdList = new TLongArrayList(missingIds);
			missingIdList.sort();

			System.out.println("Writing missing ids");
			OutputStream bos = StreamUtil.bufferedOutputStream(fileOutput);
			IdListOutputStream idOutput = new IdListOutputStream(bos);
			TLongIterator iterator = missingIdList.iterator();
			while (iterator.hasNext()) {
				idOutput.write(iterator.next());
			}
			idOutput.close();

			stats(i);
		}
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
			throws IOException
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

	private void stats(int leafsDone)
	{
		double ratio = notFound / (double) (found + notFound);
		System.out.println(String.format(
				"ways: %s, found ids: %s, missing ids: %s, ratio: %f",
				format.format(counter), format.format(found),
				format.format(notFound), ratio));

		long now = System.currentTimeMillis();
		long past = now - start;
		long estimate = Math.round((past / (double) leafsDone) * leafs.size());
		System.out.println(String.format("Past: %.2f", past / 1000 / 60.));
		System.out.println(String.format("Estimate: %.2f",
				estimate / 1000 / 60.));
	}

}
