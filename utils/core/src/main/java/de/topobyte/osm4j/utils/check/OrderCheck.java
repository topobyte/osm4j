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

package de.topobyte.osm4j.utils.check;

import java.io.IOException;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.OsmEntity;

public class OrderCheck
{

	private OsmIterator iterator;

	private long nc = -1, wc = -1, rc = -1;
	private long nw = 0, ww = 0, rw = 0;
	private long nd = 0, wd = 0, rd = 0;

	public OrderCheck(OsmIterator iterator)
	{
		this.iterator = iterator;
	}

	public void run() throws IOException
	{
		while (iterator.hasNext()) {
			EntityContainer container = iterator.next();
			OsmEntity entity = container.getEntity();
			long id = entity.getId();
			switch (container.getType()) {
			case Node:
				if (id < nc) {
					nw++;
				} else if (id == nc) {
					nd++;
				}
				nc = id;
				break;
			case Way:
				if (id < wc) {
					ww++;
				} else if (id == wc) {
					wd++;
				}
				wc = id;
				break;
			case Relation:
				if (id < rc) {
					rw++;
				} else if (id == rc) {
					rd++;
				}
				rc = id;
				break;
			}
		}
	}

	public boolean isAllFine()
	{
		return !hasWrongOrder() && !hasDuplicates();
	}

	public boolean hasWrongOrder()
	{
		return nw != 0 || ww != 0 || rw != 0;
	}

	public boolean hasDuplicates()
	{
		return nd != 0 || wd != 0 || rd != 0;
	}

	public long getNodesWrong()
	{
		return nw;
	}

	public long getWaysWrong()
	{
		return ww;
	}

	public long getRelationsWrong()
	{
		return rw;
	}

	public long getNodeDuplicates()
	{
		return nd;
	}

	public long getWayDuplicates()
	{
		return wd;
	}

	public long getRelationDuplicates()
	{
		return rd;
	}

}
