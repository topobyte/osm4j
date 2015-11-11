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

package de.topobyte.osm4j.extra.relations;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.topobyte.osm4j.core.access.OsmOutputStream;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.extra.StreamUtil;
import de.topobyte.osm4j.utils.AbstractTaskSingleInputIterator;
import de.topobyte.osm4j.utils.FileFormat;
import de.topobyte.osm4j.utils.OsmIoUtils;
import de.topobyte.osm4j.utils.config.PbfConfig;
import de.topobyte.osm4j.utils.config.PbfOptions;
import de.topobyte.osm4j.utils.config.TboConfig;
import de.topobyte.osm4j.utils.config.TboOptions;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

public class SplitRelations extends AbstractTaskSingleInputIterator
{

	private static final String OPTION_OUTPUT = "output";
	private static final String OPTION_OUTPUT_FORMAT = "output_format";
	private static final String OPTION_FILE_NAMES_RELATIONS = "relations";

	@Override
	protected String getHelpMessage()
	{
		return SplitRelations.class.getSimpleName() + " [options]";
	}

	public static void main(String[] args) throws IOException
	{
		SplitRelations task = new SplitRelations();

		task.setup(args);

		task.init();

		task.execute();

		task.finish();
	}

	private String pathOutput;
	private FileFormat outputFormat;
	private PbfConfig pbfConfig;
	private TboConfig tboConfig;
	private boolean writeMetadata = true;

	private Path dirOutput;
	private String fileNamesRelations;

	public SplitRelations()
	{
		// @formatter:off
		OptionHelper.add(options, OPTION_OUTPUT_FORMAT, true, true, "the file format of the output");
		OptionHelper.add(options, OPTION_OUTPUT, true, true, "directory to store output in");
		OptionHelper.add(options, OPTION_FILE_NAMES_RELATIONS, true, true, "names of the relation files in each directory");
		PbfOptions.add(options);
		TboOptions.add(options);
		// @formatter:on
	}

	@Override
	protected void setup(String[] args)
	{
		super.setup(args);

		String outputFormatName = line.getOptionValue(OPTION_OUTPUT_FORMAT);
		outputFormat = FileFormat.parseFileFormat(outputFormatName);
		if (outputFormat == null) {
			System.out.println("invalid output format");
			System.out.println("please specify one of: "
					+ FileFormat.getHumanReadableListOfSupportedFormats());
			System.exit(1);
		}

		pbfConfig = PbfOptions.parse(line);
		tboConfig = TboOptions.parse(line);

		pathOutput = line.getOptionValue(OPTION_OUTPUT);

		fileNamesRelations = line.getOptionValue(OPTION_FILE_NAMES_RELATIONS);
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

	private int maxMembers = 400 * 1000;

	private class Batch
	{

		List<OsmRelation> relations = new ArrayList<>();
		int members = 0;

		void clear()
		{
			relations.clear();
			members = 0;
		}

		boolean fits(OsmRelation relation)
		{
			if (relations.isEmpty()) {
				return true;
			}
			if (members + relation.getNumberOfMembers() <= maxMembers) {
				return true;
			}
			return false;
		}

		void add(OsmRelation way)
		{
			relations.add(way);
			members += way.getNumberOfMembers();
		}

	}

	private void execute() throws IOException
	{
		Batch batch = new Batch();

		for (EntityContainer container : inputIterator) {
			if (container.getType() != EntityType.Relation) {
				continue;
			}
			OsmRelation relation = (OsmRelation) container.getEntity();
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
		if (!batch.relations.isEmpty()) {
			process(batch);
			status();
			batch.clear();
		}
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

		System.out.println(String.format(
				"Processed: %s relations, time passed: %.2f per second: %s",
				format.format(relationCount), past / 1000 / 60.,
				format.format(perSecond)));
	}

	private void process(Batch batch) throws IOException
	{
		List<OsmRelation> relations = batch.relations;

		batchCount++;

		String subdirName = String.format("%d", batchCount);
		Path subdir = dirOutput.resolve(subdirName);
		Path path = subdir.resolve(fileNamesRelations);
		Files.createDirectory(subdir);

		OutputStream output = StreamUtil.bufferedOutputStream(path.toFile());
		OsmOutputStream osmOutput = OsmIoUtils.setupOsmOutput(output,
				outputFormat, writeMetadata, pbfConfig, tboConfig);

		for (OsmRelation relation : relations) {
			osmOutput.write(relation);
		}

		osmOutput.complete();
		output.close();

		relationCount += relations.size();
	}

}
