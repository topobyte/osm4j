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

package de.topobyte.osm4j.extra.relations.split;

import gnu.trove.map.TLongIntMap;
import gnu.trove.map.hash.TLongIntHashMap;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.vividsolutions.jts.geom.Envelope;

import de.topobyte.largescalefileio.ClosingFileOutputStreamFactory;
import de.topobyte.largescalefileio.SimpleClosingFileOutputStreamFactory;
import de.topobyte.osm4j.core.access.OsmIteratorInputFactory;
import de.topobyte.osm4j.core.access.OsmOutputStream;
import de.topobyte.osm4j.core.access.OsmOutputStreamStreamOutput;
import de.topobyte.osm4j.core.access.OsmStreamOutput;
import de.topobyte.osm4j.core.model.impl.Bounds;
import de.topobyte.osm4j.extra.idbboxlist.IdBboxEntry;
import de.topobyte.osm4j.extra.idbboxlist.IdBboxUtil;
import de.topobyte.osm4j.utils.FileFormat;
import de.topobyte.osm4j.utils.OsmIoUtils;
import de.topobyte.osm4j.utils.StreamUtil;
import de.topobyte.osm4j.utils.config.PbfConfig;
import de.topobyte.osm4j.utils.config.TboConfig;

public class RelationSorterBase
{

	private int maxMembers = 500 * 1000;

	private Path pathInputBboxes;
	private Path pathOutput;
	private String fileNamesRelations;
	protected OsmIteratorInputFactory iteratorFactory;

	private FileFormat outputFormat;
	protected boolean writeMetadata;

	private PbfConfig pbfConfig;
	private TboConfig tboConfig;

	protected NumberFormat format = NumberFormat.getNumberInstance(Locale.US);

	protected TLongIntMap idToBatch;
	protected List<List<IdBboxEntry>> batches;
	protected List<OsmStreamOutput> outputs;

	public RelationSorterBase(Path pathInputBboxes, Path pathOutput,
			String fileNamesRelations, OsmIteratorInputFactory iteratorFactory,
			FileFormat outputFormat, boolean writeMetadata,
			PbfConfig pbfConfig, TboConfig tboConfig)
	{
		this.pathInputBboxes = pathInputBboxes;
		this.pathOutput = pathOutput;
		this.fileNamesRelations = fileNamesRelations;
		this.iteratorFactory = iteratorFactory;
		this.outputFormat = outputFormat;
		this.writeMetadata = writeMetadata;
		this.pbfConfig = pbfConfig;
		this.tboConfig = tboConfig;
	}

	protected void ensureOutputDirectory() throws IOException
	{
		if (!Files.exists(pathOutput)) {
			System.out.println("Creating output directory");
			Files.createDirectories(pathOutput);
		}
		if (!Files.isDirectory(pathOutput)) {
			System.out.println("Output path is not a directory");
			System.exit(1);
		}
		if (pathOutput.toFile().list().length != 0) {
			System.out.println("Output directory is not empty");
			System.exit(1);
		}
	}

	protected void createBatchOutputs() throws IOException
	{
		InputStream inputBboxes = StreamUtil
				.bufferedInputStream(pathInputBboxes);
		List<IdBboxEntry> bboxes = IdBboxUtil.read(inputBboxes);
		inputBboxes.close();

		batches = BatchSorting.sort(bboxes, maxMembers);
		System.out.println("number of batches: " + batches.size());

		idToBatch = new TLongIntHashMap();
		int batchCounter = 0;
		for (List<IdBboxEntry> batch : batches) {
			for (IdBboxEntry entry : batch) {
				idToBatch.put(entry.getId(), batchCounter);
			}
			batchCounter++;
		}

		ClosingFileOutputStreamFactory factory = new SimpleClosingFileOutputStreamFactory();

		outputs = new ArrayList<>();
		for (int i = 0; i < batches.size(); i++) {
			String subdirName = String.format("%d", i + 1);
			Path subdir = pathOutput.resolve(subdirName);
			Path path = subdir.resolve(fileNamesRelations);
			Files.createDirectory(subdir);

			OutputStream output = new BufferedOutputStream(factory.create(path
					.toFile()));
			OsmOutputStream osmOutput = OsmIoUtils.setupOsmOutput(output,
					outputFormat, writeMetadata, pbfConfig, tboConfig);
			outputs.add(new OsmOutputStreamStreamOutput(output, osmOutput));

			Envelope e = new Envelope();
			for (IdBboxEntry entry : batches.get(i)) {
				e.expandToInclude(entry.getEnvelope());
			}
			Bounds bounds = new Bounds(e.getMinX(), e.getMaxX(), e.getMaxY(),
					e.getMinY());
			osmOutput.write(bounds);
		}
	}

	protected void closeOutputs() throws IOException
	{
		for (OsmStreamOutput output : outputs) {
			output.getOsmOutput().complete();
			output.close();
		}
	}

}
