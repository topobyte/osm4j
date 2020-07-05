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

package de.topobyte.osm4j.extra.extracts.query;

import java.util.ArrayList;
import java.util.List;

import de.topobyte.osm4j.utils.OsmFileInput;

public class IntermediateFiles
{

	// Lists of files that need to be merged in the end
	private List<OsmFileInput> filesNodes = new ArrayList<>();
	private List<OsmFileInput> filesWays = new ArrayList<>();
	private List<OsmFileInput> filesSimpleRelations = new ArrayList<>();
	private List<OsmFileInput> filesComplexRelations = new ArrayList<>();

	public List<OsmFileInput> getFilesNodes()
	{
		return filesNodes;
	}

	public List<OsmFileInput> getFilesWays()
	{
		return filesWays;
	}

	public List<OsmFileInput> getFilesSimpleRelations()
	{
		return filesSimpleRelations;
	}

	public List<OsmFileInput> getFilesComplexRelations()
	{
		return filesComplexRelations;
	}

}
