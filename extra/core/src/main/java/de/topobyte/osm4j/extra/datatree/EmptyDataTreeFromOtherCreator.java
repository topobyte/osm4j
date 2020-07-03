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

import java.io.File;
import java.io.IOException;

import org.locationtech.jts.geom.Envelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.topobyte.adt.geo.BBox;

public class EmptyDataTreeFromOtherCreator
{

	final static Logger logger = LoggerFactory
			.getLogger(EmptyDataTreeFromOtherCreator.class);

	private File dirInputTree;
	private File dirOutputTree;

	public EmptyDataTreeFromOtherCreator(File dirInputTree, File dirOutputTree)
	{
		this.dirInputTree = dirInputTree;
		this.dirOutputTree = dirOutputTree;
	}

	public void execute() throws IOException
	{
		logger.info("Opening data tree: " + dirInputTree);

		DataTree tree = DataTreeOpener.open(dirInputTree);

		logger.info("Creating new data tree: " + dirOutputTree);

		dirOutputTree.mkdirs();

		if (!dirOutputTree.isDirectory()) {
			String error = "Unable to create output directory";
			logger.error(error);
			throw new IOException(error);
		}

		if (dirOutputTree.listFiles().length != 0) {
			String error = "Output directory not empty";
			logger.error(error);
			throw new IOException(error);
		}

		Envelope envelope = tree.getRoot().getEnvelope();
		BBox bbox = new BBox(envelope);
		DataTreeUtil.writeTreeInfo(dirOutputTree, bbox);

		for (Node leaf : tree.getLeafs()) {
			String subdirName = Long.toHexString(leaf.getPath());
			File subdir = new File(dirOutputTree, subdirName);
			subdir.mkdir();
		}
	}

}
