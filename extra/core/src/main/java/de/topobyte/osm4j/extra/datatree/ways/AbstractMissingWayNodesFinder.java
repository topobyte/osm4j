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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import de.topobyte.osm4j.extra.datatree.DataTree;
import de.topobyte.osm4j.extra.datatree.DataTreeFiles;
import de.topobyte.osm4j.extra.datatree.DataTreeOpener;
import de.topobyte.osm4j.extra.datatree.Node;
import de.topobyte.osm4j.utils.FileFormat;
import de.topobyte.osm4j.utils.OsmFile;

public abstract class AbstractMissingWayNodesFinder implements
		MissingWayNodesFinder
{

	private Path pathNodeTree;
	private Path pathWayTree;
	private Path pathOutputTree;

	private String fileNamesNodes;
	private String fileNamesWays;
	private String fileNamesOutput;

	protected FileFormat inputFormatNodes;
	protected FileFormat inputFormatWays;

	public AbstractMissingWayNodesFinder(Path pathNodeTree, Path pathWayTree,
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

	protected DataTreeFiles filesNodes;
	protected DataTreeFiles filesWays;
	protected DataTreeFiles filesOutput;
	protected List<Node> leafs;

	private int leafsDone = 0;
	private long counter = 0;
	private long found = 0;
	private long notFound = 0;

	private long start = System.currentTimeMillis();

	private NumberFormat format = NumberFormat.getNumberInstance(Locale.US);

	protected void prepare() throws IOException
	{
		DataTree tree = DataTreeOpener.open(pathNodeTree.toFile());

		filesNodes = new DataTreeFiles(pathNodeTree, fileNamesNodes);
		filesWays = new DataTreeFiles(pathWayTree, fileNamesWays);
		filesOutput = new DataTreeFiles(pathOutputTree, fileNamesOutput);

		leafs = tree.getLeafs();
	}

	protected MissingWayNodesFinderTask creatTask(Node leaf)
	{
		Path fileNodes = filesNodes.getPath(leaf);
		Path fileWays = filesWays.getPath(leaf);
		File fileOutput = filesOutput.getFile(leaf);

		MissingWayNodesFinderTask task = new MissingWayNodesFinderTask(
				new OsmFile(fileNodes, inputFormatNodes), new OsmFile(fileWays,
						inputFormatWays), fileOutput, false);

		return task;
	}

	protected void stats(MissingWayNodesFinderTask t)
	{
		leafsDone += 1;
		counter += t.getCounter();
		found += t.getFound();
		notFound += t.getNotFound();

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
