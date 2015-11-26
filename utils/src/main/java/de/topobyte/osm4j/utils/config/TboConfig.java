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

package de.topobyte.osm4j.utils.config;

import de.topobyte.osm4j.tbo.Compression;
import de.topobyte.osm4j.tbo.data.Definitions;
import de.topobyte.osm4j.utils.config.limit.ElementCountLimit;
import de.topobyte.osm4j.utils.config.limit.NodeLimit;
import de.topobyte.osm4j.utils.config.limit.RelationLimit;
import de.topobyte.osm4j.utils.config.limit.RelationMemberLimit;
import de.topobyte.osm4j.utils.config.limit.WayLimit;
import de.topobyte.osm4j.utils.config.limit.WayNodeLimit;

public class TboConfig
{

	private Compression compression = Compression.LZ4;

	private NodeLimit limitNodes = new ElementCountLimit(
			Definitions.DEFAULT_BATCH_SIZE_NODES);
	private WayLimit limitWays = new WayNodeLimit(
			Definitions.DEFAULT_BATCH_SIZE_WAY_NODES);
	private RelationLimit limitRelations = new RelationMemberLimit(
			Definitions.DEFAULT_BATCH_SIZE_RELATION_MEMBERS);

	public Compression getCompression()
	{
		return compression;
	}

	public void setCompression(Compression compression)
	{
		this.compression = compression;
	}

	public NodeLimit getLimitNodes()
	{
		return limitNodes;
	}

	public void setLimitNodes(NodeLimit limitNodes)
	{
		this.limitNodes = limitNodes;
	}

	public WayLimit getLimitWays()
	{
		return limitWays;
	}

	public void setLimitWays(WayLimit limitWays)
	{
		this.limitWays = limitWays;
	}

	public RelationLimit getLimitRelations()
	{
		return limitRelations;
	}

	public void setLimitRelations(RelationLimit limitRelations)
	{
		this.limitRelations = limitRelations;
	}

}
