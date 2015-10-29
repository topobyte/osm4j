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

package de.topobyte.osm4j.tbo.data;

public class Definitions
{

	public static final int BLOCK_TYPE_NODES = 1;
	public static final int BLOCK_TYPE_WAYS = 2;
	public static final int BLOCK_TYPE_RELATIONS = 3;

	public static final int DEFAULT_BATCH_SIZE_NODES = 4096;
	public static final int DEFAULT_BATCH_SIZE_WAY_NODES = 6144;
	public static final int DEFAULT_BATCH_SIZE_RELATION_MEMBERS = 8192;

	public static final int VERSION = 1;

	public static final String KEY_CREATION_TIME = "creation-time";

	// Metadata situation of a whole block
	public static final int METADATA_NONE = 1;
	public static final int METADATA_ALL = 2;
	public static final int METADATA_MIXED = 3;

	// In case of METADATA_MIXED: for each element
	public static final int METADATA_NO = 0;
	public static final int METADATA_YES = 1;

}
