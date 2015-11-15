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

package de.topobyte.osm4j.utils.executables;

import java.io.IOException;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.utils.AbstractTaskSingleInputStreamSingleOutput;
import de.topobyte.osm4j.utils.sort.MemorySort;

public class OsmSort extends AbstractTaskSingleInputStreamSingleOutput
{

	@Override
	protected String getHelpMessage()
	{
		return OsmSort.class.getSimpleName() + " [options]";
	}

	public static void main(String[] args) throws IOException
	{
		OsmSort convert = new OsmSort();

		convert.setup(args);

		convert.init();

		convert.run();

		convert.finish();
	}

	@Override
	protected void setup(String[] args)
	{
		super.setup(args);
	}

	private void run() throws IOException
	{
		OsmIterator iterator = createIterator();
		MemorySort sort = new MemorySort(osmOutputStream, iterator);
		sort.run();
	}

}
