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

package de.topobyte.osm4j.pbf.access;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import crosby.binary.file.FileBlock;
import de.topobyte.osm4j.core.access.OsmHandler;
import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.pbf.PbfParser;

public class PbfIterator implements OsmIterator, OsmHandler
{

	private InputStream input;
	private PbfParser parser;

	public PbfIterator(InputStream input, boolean fetchMetadata)
	{
		this.input = input;
		this.parser = new PbfParser(this, fetchMetadata);
	}

	@Override
	public boolean hasNext()
	{
		if (available == 0) {
			if (finished) {
				return false;
			}
			try {
				advanceBlock();
			} catch (IOException e) {
				throw new RuntimeException("error while reading block", e);
			}
		}
		return available > 0;
	}

	@Override
	public EntityContainer next()
	{
		available--;
		if (nodes.size() > 0) {
			OsmNode node = nodes.remove(0);
			return new EntityContainer(EntityType.Node, node);
		} else if (ways.size() > 0) {
			OsmWay way = ways.remove(0);
			return new EntityContainer(EntityType.Way, way);
		} else {
			OsmRelation relation = relations.remove(0);
			return new EntityContainer(EntityType.Relation, relation);
		}
	}

	@Override
	public void remove()
	{
		// not implemented
	}

	private void advanceBlock() throws IOException
	{
		try {
			while (available == 0) {
				FileBlock.process(input, parser);
			}
		} catch (EOFException e) {
			parser.complete();
		}
	}

	private List<OsmNode> nodes = new LinkedList<>();
	private List<OsmWay> ways = new LinkedList<>();
	private List<OsmRelation> relations = new LinkedList<>();

	private int available = 0;
	private boolean finished = false;

	@Override
	public void handle(OsmNode node) throws IOException
	{
		nodes.add(node);
		available++;
	}

	@Override
	public void handle(OsmWay way) throws IOException
	{
		ways.add(way);
		available++;
	}

	@Override
	public void handle(OsmRelation relation) throws IOException
	{
		relations.add(relation);
		available++;
	}

	@Override
	public void complete() throws IOException
	{
		finished = true;
	}

	@Override
	public Iterator<EntityContainer> iterator()
	{
		return this;
	}

}
