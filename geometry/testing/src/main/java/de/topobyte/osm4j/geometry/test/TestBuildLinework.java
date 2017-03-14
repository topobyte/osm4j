// Copyright 2017 Sebastian Kuerten
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

package de.topobyte.osm4j.geometry.test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Arrays;

import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.dataset.InMemoryMapDataSet;
import de.topobyte.osm4j.core.dataset.MapDataSetLoader;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.resolve.EntityNotFoundException;
import de.topobyte.osm4j.geometry.LineworkBuilder;
import de.topobyte.osm4j.geometry.MissingEntitiesStrategy;
import de.topobyte.osm4j.geometry.MissingWayNodeStrategy;
import de.topobyte.osm4j.geometry.RegionBuilder;
import de.topobyte.osm4j.tbo.access.TboIterator;
import gnu.trove.map.TLongObjectMap;

public class TestBuildLinework
{

	final static Logger logger = LoggerFactory
			.getLogger(TestBuildLinework.class);

	private static void usage()
	{
		System.out.println("usage: " + TestBuildLinework.class.getSimpleName()
				+ " <tbo file>");
		System.exit(1);
	}

	public static void main(String[] args)
			throws MalformedURLException, IOException,
			ParserConfigurationException, SAXException, EntityNotFoundException
	{
		if (args.length == 0) {
			System.out.println("Please specify an input file");
			usage();
		}

		if (args.length != 1) {
			System.out.println("Please specify only one input file");
			usage();
		}

		InputStream input = new FileInputStream(args[0]);

		OsmIterator iterator = new TboIterator(input, true, true);
		InMemoryMapDataSet data = MapDataSetLoader.read(iterator, false, false,
				true);

		TLongObjectMap<OsmRelation> relations = data.getRelations();

		if (relations.isEmpty()) {
			logger.warn("No relation found");
			return;
		}

		LineworkBuilder lineworkBuilder = new LineworkBuilder();
		lineworkBuilder.setMissingEntitiesStrategy(
				MissingEntitiesStrategy.BUILD_PARTIAL);
		lineworkBuilder.setMissingWayNodeStrategy(
				MissingWayNodeStrategy.SPLIT_POLYLINE);

		RegionBuilder regionBuilder = new RegionBuilder();
		regionBuilder.setMissingEntitiesStrategy(
				MissingEntitiesStrategy.BUILD_PARTIAL);

		long[] ids = relations.keys();
		Arrays.sort(ids);
		for (long id : ids) {
			OsmRelation relation = relations.get(id);
			logger.info("Relation " + relation.getId());
			lineworkBuilder.build(relation, data);

			regionBuilder.build(relation, data);
		}
	}

}
