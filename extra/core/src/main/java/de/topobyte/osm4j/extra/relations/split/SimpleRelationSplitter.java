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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.topobyte.melon.io.StreamUtil;
import de.topobyte.osm4j.core.access.OsmIteratorInput;
import de.topobyte.osm4j.core.access.OsmIteratorInputFactory;
import de.topobyte.osm4j.core.access.OsmOutputStream;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.util.RelationIterator;
import de.topobyte.osm4j.extra.OutputUtil;
import de.topobyte.osm4j.utils.OsmIoUtils;
import de.topobyte.osm4j.utils.OsmOutputConfig;

public class SimpleRelationSplitter
{

	final static Logger logger = LoggerFactory
			.getLogger(SimpleRelationSplitter.class);

	private int maxMembers = 100 * 1000;

	private Path pathOutput;
	private String fileNamesRelations;
	private OsmIteratorInputFactory iteratorFactory;

	private OsmOutputConfig outputConfig;

	public SimpleRelationSplitter(Path pathOutput, String fileNamesRelations,
			OsmIteratorInputFactory iteratorFactory,
			OsmOutputConfig outputConfig)
	{
		this.pathOutput = pathOutput;
		this.fileNamesRelations = fileNamesRelations;
		this.iteratorFactory = iteratorFactory;
		this.outputConfig = outputConfig;
	}

	public void execute() throws IOException
	{
		OutputUtil.ensureOutputDirectory(pathOutput);

		RelationBatch batch = new RelationBatch(maxMembers);

		OsmIteratorInput iteratorInput = iteratorFactory.createIterator(true,
				outputConfig.isWriteMetadata());
		RelationIterator relations = new RelationIterator(
				iteratorInput.getIterator());

		for (OsmRelation relation : relations) {
			if (relation.getNumberOfMembers() == 0) {
				continue;
			}
			if (batch.fits(relation)) {
				batch.add(relation);
			} else {
				process(batch);
				status();
				batch.clear();
				batch.add(relation);
			}
		}
		if (!batch.getElements().isEmpty()) {
			process(batch);
			status();
			batch.clear();
		}

		logger.info(String.format("Wrote %s relations in %d batches",
				format.format(relationCount), batchCount));
	}

	private int batchCount = 0;
	private int relationCount = 0;

	private long start = System.currentTimeMillis();
	private NumberFormat format = NumberFormat.getNumberInstance(Locale.US);

	private void status()
	{
		long now = System.currentTimeMillis();
		long past = now - start;

		double seconds = past / 1000;
		long perSecond = Math.round(relationCount / seconds);

		logger.debug(String.format(
				"Processed: %s relations, time passed: %.2f per second: %s",
				format.format(relationCount), past / 1000 / 60.,
				format.format(perSecond)));
	}

	private void process(RelationBatch batch) throws IOException
	{
		List<OsmRelation> relations = batch.getElements();

		batchCount++;

		String subdirName = String.format("%d", batchCount);
		Path subdir = pathOutput.resolve(subdirName);
		Path path = subdir.resolve(fileNamesRelations);
		Files.createDirectory(subdir);

		OutputStream output = StreamUtil.bufferedOutputStream(path);
		OsmOutputStream osmOutput = OsmIoUtils.setupOsmOutput(output,
				outputConfig);

		for (OsmRelation relation : relations) {
			osmOutput.write(relation);
		}

		osmOutput.complete();
		output.close();

		relationCount += relations.size();
	}

}
