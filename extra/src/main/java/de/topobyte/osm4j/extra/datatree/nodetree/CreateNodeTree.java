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

package de.topobyte.osm4j.extra.datatree.nodetree;

import java.io.IOException;
import java.nio.file.Paths;

import de.topobyte.osm4j.core.access.OsmIterator;

public class CreateNodeTree extends CreateNodeTreeBase
{

	@Override
	protected String getHelpMessage()
	{
		return CreateNodeTree.class.getSimpleName() + " [options]";
	}

	public static void main(String[] args) throws IOException
	{
		CreateNodeTree task = new CreateNodeTree();

		task.setup(args);

		task.init();

		task.execute();

		task.finish();
	}

	private void execute() throws IOException
	{
		OsmIterator iterator = createIterator();

		NodeTreeCreator creator = new NodeTreeCreator(iterator,
				Paths.get(pathOutput), fileNames, outputFormat, pbfConfig,
				tboConfig, writeMetadata);

		creator.openExistingTree();

		creator.execute();
	}

}
