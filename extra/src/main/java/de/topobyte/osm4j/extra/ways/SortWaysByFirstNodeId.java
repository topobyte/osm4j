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

package de.topobyte.osm4j.extra.ways;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import de.topobyte.osm4j.core.access.OsmOutputStream;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.extra.StreamUtil;
import de.topobyte.osm4j.utils.AbstractTaskSingleInputIteratorOutput;
import de.topobyte.osm4j.utils.OsmIoUtils;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

public class SortWaysByFirstNodeId extends
		AbstractTaskSingleInputIteratorOutput
{

	private static final String OPTION_OUTPUT = "output";

	@Override
	protected String getHelpMessage()
	{
		return SortWaysByFirstNodeId.class.getSimpleName() + " [options]";
	}

	public static void main(String[] args) throws IOException
	{
		SortWaysByFirstNodeId task = new SortWaysByFirstNodeId();

		task.setup(args);

		task.init();

		task.execute();

		task.finish();
	}

	protected String pathOutput;
	protected Path dirOutput;

	public SortWaysByFirstNodeId()
	{
		// @formatter:off
		OptionHelper.add(options, OPTION_OUTPUT, true, true, "directory to store output in");
		// @formatter:on
	}

	@Override
	protected void setup(String[] args)
	{
		super.setup(args);

		pathOutput = line.getOptionValue(OPTION_OUTPUT);
	}

	@Override
	protected void init() throws IOException
	{
		super.init();

		dirOutput = Paths.get(pathOutput);

		if (!Files.exists(dirOutput)) {
			System.out.println("Creating output directory");
			Files.createDirectories(dirOutput);
		}
		if (!Files.isDirectory(dirOutput)) {
			System.out.println("Output path is not a directory");
			System.exit(1);
		}
		if (dirOutput.toFile().list().length != 0) {
			System.out.println("Output directory is not empty");
			System.exit(1);
		}
	}

	private int maxWays = 800 * 1000;
	private int maxWayNodes = 10 * 1000 * 1000;

	private class Batch
	{

		List<OsmWay> ways = new ArrayList<>();
		int wayNodes = 0;

		void clear()
		{
			ways.clear();
			wayNodes = 0;
		}

		boolean fits(OsmWay way)
		{
			if (ways.isEmpty()) {
				return true;
			}
			if (ways.size() < maxWays
					&& wayNodes + way.getNumberOfNodes() <= maxWayNodes) {
				return true;
			}
			return false;
		}

		void add(OsmWay way)
		{
			ways.add(way);
			wayNodes += way.getNumberOfNodes();
		}

	}

	private void execute() throws IOException
	{
		Batch batch = new Batch();

		for (EntityContainer container : inputIterator) {
			if (container.getType() != EntityType.Way) {
				continue;
			}
			OsmWay way = (OsmWay) container.getEntity();
			if (way.getNumberOfNodes() == 0) {
				continue;
			}
			if (batch.fits(way)) {
				batch.add(way);
			} else {
				process(batch);
				status();
				batch.clear();
				batch.add(way);
			}
		}
		if (!batch.ways.isEmpty()) {
			process(batch);
			status();
			batch.clear();
		}
	}

	private int batchCount = 0;
	private int wayCount = 0;

	private long start = System.currentTimeMillis();
	private NumberFormat format = NumberFormat.getNumberInstance(Locale.US);

	private void status()
	{
		long now = System.currentTimeMillis();
		long past = now - start;

		double seconds = past / 1000;
		long perSecond = Math.round(wayCount / seconds);

		System.out.println(String.format(
				"Processed: %s ways, time passed: %.2f per second: %s",
				format.format(wayCount), past / 1000 / 60.,
				format.format(perSecond)));
	}

	private void process(Batch batch) throws IOException
	{
		List<OsmWay> ways = batch.ways;
		Collections.sort(ways, new WayNodeIdComparator());

		batchCount++;

		String filename = String.format("%d%s", batchCount,
				OsmIoUtils.extension(outputFormat));
		Path path = dirOutput.resolve(filename);
		File file = path.toFile();
		OutputStream output = StreamUtil.bufferedOutputStream(file);
		OsmOutputStream osmOutput = OsmIoUtils.setupOsmOutput(output,
				outputFormat, writeMetadata, pbfConfig, tboConfig);

		for (OsmWay way : ways) {
			osmOutput.write(way);
		}

		osmOutput.complete();
		output.close();

		wayCount += ways.size();
	}

}
