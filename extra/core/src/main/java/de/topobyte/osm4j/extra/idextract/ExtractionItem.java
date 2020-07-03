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

package de.topobyte.osm4j.extra.idextract;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ExtractionItem
{

	private List<Path> pathsIds;
	private Path pathOutput;

	public ExtractionItem(Path pathIds, Path pathOutput)
	{
		this.pathsIds = new ArrayList<>();
		pathsIds.add(pathIds);
		this.pathOutput = pathOutput;
	}

	public ExtractionItem(List<Path> pathIds, Path pathOutput)
	{
		this.pathsIds = pathIds;
		this.pathOutput = pathOutput;
	}

	public List<Path> getPathsIds()
	{
		return pathsIds;
	}

	public Path getPathOutput()
	{
		return pathOutput;
	}

}
