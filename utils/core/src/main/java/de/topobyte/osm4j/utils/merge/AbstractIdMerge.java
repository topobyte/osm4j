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

package de.topobyte.osm4j.utils.merge;

import java.util.Collection;

import org.locationtech.jts.geom.Envelope;

import de.topobyte.osm4j.core.access.OsmIdIterator;
import de.topobyte.osm4j.core.model.iface.OsmBounds;
import de.topobyte.osm4j.core.model.impl.Bounds;

public class AbstractIdMerge
{

	protected Collection<OsmIdIterator> inputs;

	public AbstractIdMerge(Collection<OsmIdIterator> inputs)
	{
		this.inputs = inputs;

		initBounds();
	}

	protected boolean hasBounds;
	protected Bounds bounds;

	protected void initBounds()
	{
		hasBounds = false;
		Envelope envelope = new Envelope();
		for (OsmIdIterator iterator : inputs) {
			if (iterator.hasBounds()) {
				hasBounds = true;
				OsmBounds bounds = iterator.getBounds();
				Envelope e = new Envelope(bounds.getLeft(), bounds.getRight(),
						bounds.getBottom(), bounds.getTop());
				envelope.expandToInclude(e);
			}
		}
		bounds = !hasBounds ? null
				: new Bounds(envelope.getMinX(), envelope.getMaxX(),
						envelope.getMaxY(), envelope.getMinY());
	}

}
