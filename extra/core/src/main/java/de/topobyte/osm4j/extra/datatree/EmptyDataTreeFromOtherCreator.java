// Copyright 2016 Sebastian Kuerten
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

package de.topobyte.osm4j.extra.datatree;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.locationtech.jts.geom.Envelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.topobyte.adt.geo.BBox;
import de.topobyte.osm4j.extra.OutputUtil;

public class EmptyDataTreeFromOtherCreator
{

	final static Logger logger = LoggerFactory
			.getLogger(EmptyDataTreeFromOtherCreator.class);

	private Path dirInputTree;
	private Path dirOutputTree;

	public EmptyDataTreeFromOtherCreator(Path dirInputTree, Path dirOutputTree)
	{
		this.dirInputTree = dirInputTree;
		this.dirOutputTree = dirOutputTree;
	}

	public void execute() throws IOException
	{
		logger.info("Opening data tree: " + dirInputTree);

		DataTree tree = DataTreeOpener.open(dirInputTree);

		logger.info("Creating new data tree: " + dirOutputTree);

		OutputUtil.ensureOutputDirectory(dirOutputTree);

		Envelope envelope = tree.getRoot().getEnvelope();
		BBox bbox = new BBox(envelope);
		DataTreeUtil.writeTreeInfo(dirOutputTree, bbox);

		for (Node leaf : tree.getLeafs()) {
			String subdirName = Long.toHexString(leaf.getPath());
			Path subdir = dirOutputTree.resolve(subdirName);
			Files.createDirectories(subdir);
		}
	}

}
