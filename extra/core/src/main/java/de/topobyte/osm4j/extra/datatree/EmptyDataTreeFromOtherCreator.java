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

import com.vividsolutions.jts.geom.Envelope;

import de.topobyte.adt.geo.BBox;

public class EmptyDataTreeFromOtherCreator
{

	private File dirInputTree;
	private File dirOutputTree;

	public EmptyDataTreeFromOtherCreator(File dirInputTree, File dirOutputTree)
	{
		this.dirInputTree = dirInputTree;
		this.dirOutputTree = dirOutputTree;
	}

	public void execute() throws IOException
	{
		System.out.println("Opening data tree: " + dirInputTree);

		DataTree tree = DataTreeOpener.open(dirInputTree);

		System.out.println("Creating new data tree: " + dirOutputTree);

		dirOutputTree.mkdirs();

		if (!dirOutputTree.isDirectory()) {
			System.out.println("Unable to create output directory");
			System.exit(1);
		}

		if (dirOutputTree.listFiles().length != 0) {
			System.out.println("Output directory not empty");
			System.exit(1);
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
