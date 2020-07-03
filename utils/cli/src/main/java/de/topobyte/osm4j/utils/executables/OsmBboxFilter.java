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

import org.locationtech.jts.geom.Envelope;

import de.topobyte.adt.geo.BBox;
import de.topobyte.adt.geo.BBoxString;
import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.model.iface.OsmBounds;
import de.topobyte.osm4j.core.model.impl.Bounds;
import de.topobyte.osm4j.utils.AbstractAreaFilter;
import de.topobyte.osm4j.utils.OsmBoundsUtil;
import de.topobyte.osm4j.utils.areafilter.BboxFilter;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

public class OsmBboxFilter extends AbstractAreaFilter
{

	private static final String OPTION_BBOX = "bbox";

	@Override
	protected String getHelpMessage()
	{
		return OsmBboxFilter.class.getSimpleName() + " [options]";
	}

	public static void main(String[] args) throws IOException
	{
		OsmBboxFilter convert = new OsmBboxFilter();

		convert.setup(args);

		convert.init();

		convert.run();

		convert.finish();
	}

	private BBox bbox;

	public OsmBboxFilter()
	{
		// @formatter:off
		OptionHelper.addL(options, OPTION_BBOX, true, true, "the bbox to extract (lon1,lat1,lon2,lat2)");
		// @formatter:on
	}

	@Override
	protected void setup(String[] args)
	{
		super.setup(args);

		String argBbox = line.getOptionValue(OPTION_BBOX);
		BBoxString bboxString = BBoxString.parse(argBbox);
		bbox = bboxString.toBbox();

		if (bbox.getLat1() == 0 && bbox.getLat2() == 0 && bbox.getLon1() == 0
				&& bbox.getLon2() == 0) {
			System.out.println("invalid bounding box");
			System.exit(1);
		}
	}

	protected void run() throws IOException
	{
		OsmIterator iterator = createIterator();

		OsmBounds oldBounds = iterator.getBounds();
		BBox oldBbox = OsmBoundsUtil.toBbox(oldBounds);
		Envelope intersectionEnvelope = oldBbox.toEnvelope()
				.intersection(bbox.toEnvelope());
		BBox newBbox = new BBox(intersectionEnvelope);
		Bounds newBounds = OsmBoundsUtil.toBounds(newBbox);

		osmOutputStream.write(newBounds);

		BboxFilter filter = new BboxFilter(osmOutputStream, iterator, bbox,
				onlyNodes);
		filter.run();
	}

}
