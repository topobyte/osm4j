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

package de.topobyte.osm4j.utils;

import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import de.topobyte.osm4j.core.access.OsmInputException;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.OsmBounds;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.pbf.seq.PbfIterator;
import de.topobyte.osm4j.tbo.access.TboIterator;
import de.topobyte.osm4j.xml.dynsax.OsmXmlIterator;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

public abstract class AbstractEntityCollector extends
		AbstractTaskSingleInputReaderSingleOutput
{

	private static final String OPTION_REFERENCES = "references";
	private static final String OPTION_REFERENCES_FORMAT = "references_format";

	private String pathReferences;
	private InputStream inRefs;

	protected Iterator<EntityContainer> iteratorReferences;
	protected TLongSet ids = new TLongHashSet();

	public AbstractEntityCollector()
	{
		// @formatter:off
		OptionHelper.add(options, OPTION_REFERENCES, true, true, "the file to determine references from");
		OptionHelper.add(options, OPTION_REFERENCES_FORMAT, true, true, "the file format of the references file");
		// @formatter:on
	}

	@Override
	protected void setup(String[] args)
	{
		super.setup(args);
		pathReferences = line.getOptionValue(OPTION_REFERENCES);
	}

	@Override
	protected void init() throws IOException
	{
		File file = new File(pathReferences);
		FileInputStream fis = new FileInputStream(file);
		inRefs = new BufferedInputStream(fis);

		switch (inputFormat) {
		case XML:
			iteratorReferences = new OsmXmlIterator(inRefs, readMetadata);
			break;
		case TBO:
			iteratorReferences = new TboIterator(inRefs, readMetadata);
			break;
		case PBF:
			iteratorReferences = new PbfIterator(inRefs, readMetadata);
			break;
		}

		super.init();
	}

	@Override
	protected void run() throws OsmInputException
	{
		readReferences();
		try {
			inRefs.close();
		} catch (IOException e) {
			throw new OsmInputException("unable to close references input", e);
		}

		super.run();
	}

	@Override
	public void handle(OsmBounds bounds) throws IOException
	{
		// ignore bounds
	}

	@Override
	public void handle(OsmNode node) throws IOException
	{
		if (take(node)) {
			osmOutputStream.write(node);
		}
	}

	@Override
	public void handle(OsmWay way) throws IOException
	{
		if (take(way)) {
			osmOutputStream.write(way);
		}
	}

	@Override
	public void handle(OsmRelation relation) throws IOException
	{
		if (take(relation)) {
			osmOutputStream.write(relation);
		}
	}

	@Override
	public void complete() throws IOException
	{
		osmOutputStream.complete();
	}

	protected abstract void readReferences();

	protected abstract boolean take(OsmNode node);

	protected abstract boolean take(OsmWay way);

	protected abstract boolean take(OsmRelation relation);

}
