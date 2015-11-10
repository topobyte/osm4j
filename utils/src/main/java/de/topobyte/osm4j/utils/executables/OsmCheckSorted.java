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

import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.OsmEntity;
import de.topobyte.osm4j.utils.AbstractTaskSingleInputIterator;

public class OsmCheckSorted extends AbstractTaskSingleInputIterator
{

	@Override
	protected String getHelpMessage()
	{
		return OsmCheckSorted.class.getSimpleName() + " [options]";
	}

	public static void main(String[] args) throws IOException
	{
		OsmCheckSorted task = new OsmCheckSorted();
		task.setup(args);

		task.readMetadata = false;
		task.init();

		task.run();

		task.finish();
	}

	private long nc = -1, wc = -1, rc = -1;
	private long nw = 0, ww = 0, rw = 0;
	private long nd = 0, wd = 0, rd = 0;

	private void run() throws IOException
	{
		while (inputIterator.hasNext()) {
			EntityContainer container = inputIterator.next();
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

		if (nw == 0 && ww == 0 && rw == 0) {
			System.out.println("Order is fine");
		} else {
			if (nw != 0) {
				System.out.println("nodes in wrong order:     " + nw);
			}
			if (ww != 0) {
				System.out.println("ways in wrong order:      " + ww);
			}
			if (rw != 0) {
				System.out.println("relations in wrong order: " + rw);
			}
		}

		if (nd == 0 && wd == 0 && rd == 0) {
			System.out.println("No duplicates");
		} else {
			if (nd != 0) {
				System.out.println("node duplicates:     " + nd);
			}
			if (wd != 0) {
				System.out.println("way duplicates:      " + wd);
			}
			if (rd != 0) {
				System.out.println("relation duplicates: " + rd);
			}
		}

		finish();
	}

}
