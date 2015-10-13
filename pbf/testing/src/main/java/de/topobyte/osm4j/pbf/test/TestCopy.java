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

package de.topobyte.osm4j.pbf.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import crosby.binary.file.BlockInputStream;
import crosby.binary.file.BlockOutputStream;
import de.topobyte.osm4j.core.access.OsmHandler;
import de.topobyte.osm4j.core.access.ProgressMonitor;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.pbf.PbfParser;
import de.topobyte.osm4j.pbf.PbfSerializer;

public class TestCopy implements OsmHandler
{

	public static void main(String[] args) throws IOException
	{
		if (args.length != 2) {
			System.out.println("usage: " + TestCopy.class.getSimpleName()
					+ " <input> <output>");
			System.exit(1);
		}

		File fileInput = new File(args[0]);
		File fileOutput = new File(args[1]);

		FileOutputStream output = new FileOutputStream(fileOutput);
		BlockOutputStream blockOutputStream = new BlockOutputStream(output);
		PbfSerializer serializer = new PbfSerializer(blockOutputStream, true);
		serializer.setUseDense(true);
		serializer.configBatchLimit(8000);

		TestCopy test = new TestCopy(serializer);
		PbfParser parser = new PbfParser(test, true);

		FileInputStream input = new FileInputStream(fileInput);
		BlockInputStream blockInputStream = new BlockInputStream(input, parser);
		blockInputStream.process();
	}

	private final PbfSerializer serializer;

	private int nc = 0, wc = 0, rc = 0;

	private ProgressMonitor progressMonitor;

	public TestCopy(PbfSerializer serializer)
	{
		this.serializer = serializer;
		progressMonitor = new ProgressMonitor("copy");
	}

	@Override
	public void handle(OsmNode node)
	{
		nc++;
		progressMonitor.nodeProcessed();
		serializer.process(node);
	}

	@Override
	public void handle(OsmWay way)
	{
		wc++;
		progressMonitor.wayProcessed();
		serializer.process(way);
	}

	@Override
	public void handle(OsmRelation relation)
	{
		rc++;
		progressMonitor.relationProcessed();
		serializer.process(relation);
	}

	@Override
	public void complete()
	{
		System.out.println("nodes: " + nc);
		System.out.println("ways: " + wc);
		System.out.println("relations: " + rc);
		serializer.complete();
		serializer.release();
	}

}
