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

package de.topobyte.osm4j.extra.datatree;

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.vividsolutions.jts.geom.Envelope;

import de.topobyte.adt.geo.BBox;
import de.topobyte.utilities.apache.commons.cli.OptionHelper;

public class CreateEmptyDataTreeFromOther
{

	private static final String OPTION_INPUT = "input";
	private static final String OPTION_OUTPUT = "output";

	private static final String HELP_MESSAGE = CreateEmptyDataTreeFromOther.class
			.getSimpleName() + " [options]";

	public static void main(String[] args) throws IOException
	{
		Options options = new Options();

		// @formatter:off
		OptionHelper.add(options, OPTION_INPUT, true, true, "directory with data tree");
		OptionHelper.add(options, OPTION_OUTPUT, true, true, "directory in which to create a new data tree");
		// @formatter:on

		CommandLine line = null;
		try {
			line = new GnuParser().parse(options, args);
		} catch (ParseException e) {
			System.out.println("unable to parse command line: "
					+ e.getMessage());
			new HelpFormatter().printHelp(HELP_MESSAGE, options);
			System.exit(1);
		}

		String pathInput = line.getOptionValue(OPTION_INPUT);
		String pathOutput = line.getOptionValue(OPTION_OUTPUT);

		File dirInputTree = new File(pathInput);
		File dirOutputTree = new File(pathOutput);

		CreateEmptyDataTreeFromOther task = new CreateEmptyDataTreeFromOther(
				dirInputTree, dirOutputTree);
		task.execute();
	}

	private File dirInputTree;
	private File dirOutputTree;

	public CreateEmptyDataTreeFromOther(File dirInputTree, File dirOutputTree)
	{
		this.dirInputTree = dirInputTree;
		this.dirOutputTree = dirOutputTree;
	}

	private void execute() throws IOException
	{
		System.out.println("Opening data tree: " + dirInputTree);

		DataTree tree = DataTreeOpener.open(dirInputTree);

		System.out.println("Creating new data tree: " + dirOutputTree);

		dirOutputTree.mkdirs();

		if (!dirOutputTree.isDirectory()) {
			System.out.println("Unable to create output directory");
			System.exit(1);
		}

		if (dirOutputTree.listFiles().length != 0) {
			System.out.println("Output directory not empty");
			System.exit(1);
		}

		Envelope envelope = tree.getRoot().getEnvelope();
		BBox bbox = new BBox(envelope);
		DataTreeUtil.writeTreeInfo(dirOutputTree, bbox);

		for (Node leaf : tree.getLeafs()) {
			String subdirName = Long.toHexString(leaf.getPath());
			File subdir = new File(dirOutputTree, subdirName);
			subdir.mkdir();
		}
	}

}
