// Copyright 2020 Sebastian Kuerten
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

package de.topobyte.osm4j.diskstorage;

import java.io.IOException;

/**
 * An interface for classes that are able to provide a Block of nodes.
 * 
 * @param <T>
 *            the type of blocks.
 * 
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
public interface BlockProvider<T>
{

	/**
	 * Get the block that will be found at position <code>pos</code>
	 * 
	 * @param pos
	 *            the position of the block
	 * @return the block to retrieve
	 * @throws IOException
	 *             on failure.
	 */
	public T getBlock(long pos) throws IOException;

}
