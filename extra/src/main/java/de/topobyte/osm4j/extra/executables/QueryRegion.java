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

package de.topobyte.osm4j.extra.executables;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;

import de.topobyte.jts.utils.predicate.PredicateEvaluatorPrepared;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

public class QueryRegion extends BaseQuery
{

	private static final String OPTION_REGION = "region";

	@Override
	protected String getHelpMessage()
	{
		return BaseQuery.class.getSimpleName() + " [options]";
	}

	public static void main(String[] args) throws IOException
	{
		QueryRegion task = new QueryRegion();

		task.setup(args);

		task.execute();
	}

	public QueryRegion()
	{
		// @formatter:off
		OptionHelper.add(options, OPTION_REGION, true, true, "a WKT file containing the region to extract");
		// @formatter:on
	}

	private Geometry region;

	@Override
	protected void setup(String[] args)
	{
		super.setup(args);

		String argRegion = line.getOptionValue(OPTION_REGION);

		try {
			Reader reader = new FileReader(argRegion);
			region = new WKTReader().read(reader);
		} catch (Exception e) {
			System.out.println("Error while reading region");
			e.printStackTrace();
			System.exit(1);
		}
	}

	@Override
	protected void execute() throws IOException
	{
		queryEnvelope = region.getEnvelopeInternal();

		test = new PredicateEvaluatorPrepared(region);

		super.execute();
	}

}
