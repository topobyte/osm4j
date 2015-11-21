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

package de.topobyte.osm4j.extra.relations;

import java.io.IOException;
import java.nio.file.Paths;

import de.topobyte.osm4j.core.access.OsmInputException;
import de.topobyte.osm4j.utils.OsmOutputConfig;

public class DistributeComplexRelations extends DistributeRelationsBase
{

	@Override
	protected String getHelpMessage()
	{
		return DistributeComplexRelations.class.getSimpleName() + " [options]";
	}

	public static void main(String[] args) throws IOException,
			OsmInputException
	{
		DistributeComplexRelations task = new DistributeComplexRelations();

		task.setup(args);

		task.execute();
	}

	private void execute() throws IOException, OsmInputException
	{
		OsmOutputConfig outputConfig = new OsmOutputConfig(outputFormat,
				pbfConfig, tboConfig, writeMetadata);

		ComplexRelationsDistributor distributor = new ComplexRelationsDistributor(
				Paths.get(pathTree), Paths.get(pathData),
				Paths.get(pathOutputEmpty), Paths.get(pathOutputNonTree),
				Paths.get(pathOutputBboxes), fileNamesRelations, fileNamesWays,
				fileNamesNodes, fileNamesTreeRelations, inputFormat,
				outputConfig);

		distributor.execute();
	}

}
