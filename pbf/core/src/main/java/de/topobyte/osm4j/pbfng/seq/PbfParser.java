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

package de.topobyte.osm4j.pbfng.seq;

import java.io.IOException;

import crosby.binary.Osmformat;
import crosby.binary.Osmformat.HeaderBBox;
import de.topobyte.osm4j.core.access.OsmHandler;
import de.topobyte.osm4j.pbfng.util.PbfUtil;

public class PbfParser extends BlockParser
{

	private OsmHandler handler;
	private boolean fetchMetadata;

	public PbfParser(OsmHandler handler, boolean fetchMetadata)
	{
		this.handler = handler;
		this.fetchMetadata = fetchMetadata;
	}

	@Override
	protected void parse(Osmformat.HeaderBlock block) throws IOException
	{
		HeaderBBox bbox = block.getBbox();
		handler.handle(PbfUtil.bounds(bbox));
	}

	@Override
	protected void parse(Osmformat.PrimitiveBlock block) throws IOException
	{
		PrimParser primParser = new PrimParser(block, fetchMetadata);

		for (Osmformat.PrimitiveGroup group : block.getPrimitivegroupList()) {
			primParser.parseNodes(group.getNodesList(), handler);
			primParser.parseWays(group.getWaysList(), handler);
			primParser.parseRelations(group.getRelationsList(), handler);
			if (group.hasDense()) {
				primParser.parseDense(group.getDense(), handler);
			}
		}
	}

}
