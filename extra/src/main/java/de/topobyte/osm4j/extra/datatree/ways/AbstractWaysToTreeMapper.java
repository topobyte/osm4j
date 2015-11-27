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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import de.topobyte.largescalefileio.ClosingFileInputStreamFactory;
import de.topobyte.largescalefileio.SimpleClosingFileInputStreamFactory;
import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.dataset.sort.IdComparator;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.extra.datatree.DataTree;
import de.topobyte.osm4j.extra.datatree.DataTreeOpener;
import de.topobyte.osm4j.extra.datatree.Node;
import de.topobyte.osm4j.extra.progress.NodeProgress;
import de.topobyte.osm4j.extra.ways.WayNodeIdComparator;
import de.topobyte.osm4j.utils.FileFormat;
import de.topobyte.osm4j.utils.OsmIoUtils;
import de.topobyte.osm4j.utils.merge.sorted.SortedMergeIterator;

public abstract class AbstractWaysToTreeMapper implements WaysToTreeMapper
{

	private OsmIterator nodeIterator;

	protected Path pathTree;

	private Path pathWays;
	private FileFormat inputFormatWays;
	private boolean readMetadata;

	public AbstractWaysToTreeMapper(OsmIterator nodeIterator, Path pathTree,
			Path pathWays, FileFormat inputFormatWays, boolean readMetadata)
	{
		this.nodeIterator = nodeIterator;
		this.pathTree = pathTree;
		this.pathWays = pathWays;
		this.inputFormatWays = inputFormatWays;
		this.readMetadata = readMetadata;
	}

	@Override
	public void execute() throws IOException
	{
		prepare();
		map();
		finish();
	}

	protected DataTree tree;
	private SortedMergeIterator wayIterator;

	private List<InputStream> wayInputStreams = new ArrayList<>();

	public void prepare() throws IOException
	{
		tree = DataTreeOpener.open(pathTree.toFile());

		// Way inputs
		ClosingFileInputStreamFactory factoryIn = new SimpleClosingFileInputStreamFactory();

		List<OsmIterator> wayIterators = new ArrayList<>();
		File[] wayFiles = pathWays.toFile().listFiles();
		for (File file : wayFiles) {
			InputStream inputWays = factoryIn.create(file);
			inputWays = new BufferedInputStream(inputWays);
			wayInputStreams.add(inputWays);
			OsmIterator osmIterator = OsmIoUtils.setupOsmIterator(inputWays,
					inputFormatWays, readMetadata);
			wayIterators.add(osmIterator);
		}

		wayIterator = new SortedMergeIterator(wayIterators, new IdComparator(),
				new WayNodeIdComparator(), new IdComparator());
	}

	private NodeProgress progress = new NodeProgress();
	private OsmWay way = null;
	private long next = -1;

	private boolean advanceWay()
	{
		while (wayIterator.hasNext()) {
			EntityContainer c = wayIterator.next();
			if (c.getType() != EntityType.Way) {
				continue;
			}

			way = (OsmWay) c.getEntity();
			next = way.getNodeId(0);
			return true;
		}
		way = null;
		next = -1;
		return false;
	}

	public void map() throws IOException
	{
		progress.printTimed(1000);

		advanceWay();

		while (nodeIterator.hasNext()) {
			EntityContainer container = nodeIterator.next();
			if (container.getType() != EntityType.Node) {
				break;
			}
			OsmNode node = (OsmNode) container.getEntity();
			long id = node.getId();
			progress.increment();

			if (next > id) {
				// We don't need this node
				continue;
			} else if (next == id) {
				// We need this node
				query(node);
				// Could be that more outputs are waiting for this node
				while (advanceWay() && next == id) {
					query(node);
				}
			} else {
				// Some node that we are waiting for is not available on the
				// node input source
				while (advanceWay() && next < id) {
					query(node);
				}
			}
			if (way == null) {
				break;
			}
		}

		progress.stop();
	}

	private void query(OsmNode node) throws IOException
	{
		List<Node> leafs = tree.query(node.getLongitude(), node.getLatitude());
		for (Node leaf : leafs) {
			process(way, leaf);
		}
	}

	protected void finish() throws IOException
	{
		for (InputStream input : wayInputStreams) {
			input.close();
		}
	}

	protected abstract void process(OsmWay way, Node leaf) throws IOException;

}
