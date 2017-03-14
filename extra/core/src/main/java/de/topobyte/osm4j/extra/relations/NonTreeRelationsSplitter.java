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
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.topobyte.osm4j.extra.batch.BatchFilesUtil;
import de.topobyte.osm4j.utils.FileFormat;
import de.topobyte.osm4j.utils.OsmFile;
import de.topobyte.osm4j.utils.OsmFileInput;
import de.topobyte.osm4j.utils.OsmFileSetInput;
import de.topobyte.osm4j.utils.OsmIoUtils;
import de.topobyte.osm4j.utils.OsmOutputConfig;

public class NonTreeRelationsSplitter
{

	private int maxMembersSimple;
	private int maxMembersComplex;

	private Path pathInputSimple;
	private Path pathInputComplex;
	private Path pathInputSimpleBboxes;
	private Path pathInputComplexBboxes;
	private Path pathInputSimpleOld;
	private Path pathInputComplexOld;
	private Path pathOutputSimple;
	private Path pathOutputComplex;

	private FileFormat inputFormat;
	private OsmOutputConfig outputConfig;

	private Path pathOutputSimpleBboxList;
	private Path pathOutputComplexBboxList;

	public NonTreeRelationsSplitter(Path pathInputSimple,
			Path pathInputComplex, Path pathInputSimpleBboxes,
			Path pathInputComplexBboxes, Path pathInputSimpleOld,
			Path pathInputComplexOld, Path pathOutputSimple,
			Path pathOutputComplex, FileFormat inputFormat,
			OsmOutputConfig outputConfig, Path pathOutputSimpleBboxList,
			Path pathOutputComplexBboxList, int maxMembersSimple,
			int maxMembersComplex)
	{
		this.pathInputSimple = pathInputSimple;
		this.pathInputComplex = pathInputComplex;
		this.pathInputSimpleBboxes = pathInputSimpleBboxes;
		this.pathInputComplexBboxes = pathInputComplexBboxes;
		this.pathInputSimpleOld = pathInputSimpleOld;
		this.pathInputComplexOld = pathInputComplexOld;
		this.pathOutputSimple = pathOutputSimple;
		this.pathOutputComplex = pathOutputComplex;
		this.inputFormat = inputFormat;
		this.outputConfig = outputConfig;
		this.pathOutputSimpleBboxList = pathOutputSimpleBboxList;
		this.pathOutputComplexBboxList = pathOutputComplexBboxList;
		this.maxMembersSimple = maxMembersSimple;
		this.maxMembersComplex = maxMembersComplex;
	}

	public void execute() throws IOException
	{
		List<Path> nodePathsSimple = BatchFilesUtil
				.getPaths(pathInputSimpleOld,
						"nodes" + OsmIoUtils.extension(inputFormat));
		Collection<OsmFile> nodeFilesSimple = createOsmFiles(nodePathsSimple);

		List<Path> wayPathsSimple = BatchFilesUtil.getPaths(pathInputSimpleOld,
				"ways" + OsmIoUtils.extension(inputFormat));
		Collection<OsmFile> wayFilesSimple = createOsmFiles(wayPathsSimple);

		List<Path> nodePathsComplex = BatchFilesUtil.getPaths(
				pathInputComplexOld,
				"nodes" + OsmIoUtils.extension(inputFormat));
		Collection<OsmFile> nodeFilesComplex = createOsmFiles(nodePathsComplex);

		List<Path> wayPathsComplex = BatchFilesUtil
				.getPaths(pathInputComplexOld,
						"ways" + OsmIoUtils.extension(inputFormat));
		Collection<OsmFile> wayFilesComplex = createOsmFiles(wayPathsComplex);

		OsmFileSetInput inputNodesSimple = new OsmFileSetInput(nodeFilesSimple);
		OsmFileSetInput inputWaysSimple = new OsmFileSetInput(wayFilesSimple);

		OsmFileSetInput inputNodesComplex = new OsmFileSetInput(
				nodeFilesComplex);
		OsmFileSetInput inputWaysComplex = new OsmFileSetInput(wayFilesComplex);

		OsmFileInput inputSimpleRelations = new OsmFileInput(pathInputSimple,
				inputFormat);
		OsmFileInput inputComplexRelations = new OsmFileInput(pathInputComplex,
				inputFormat);

		Path pathOutputSimpleRelations = pathOutputSimple;
		Path pathOutputComplexRelations = pathOutputComplex;

		String fileNamesRelations = "relations"
				+ OsmIoUtils.extension(outputConfig.getFileFormat());

		SimpleRelationsSorterAndMemberCollector task1 = new SimpleRelationsSorterAndMemberCollector(
				inputSimpleRelations, pathInputSimpleBboxes,
				pathOutputSimpleRelations, fileNamesRelations, inputWaysSimple,
				inputNodesSimple, outputConfig, pathOutputSimpleBboxList,
				maxMembersSimple);

		task1.execute();

		ComplexRelationsSorterAndMemberCollector task2 = new ComplexRelationsSorterAndMemberCollector(
				inputComplexRelations, pathInputComplexBboxes,
				pathOutputComplexRelations, fileNamesRelations,
				inputWaysComplex, inputNodesComplex, outputConfig,
				pathOutputComplexBboxList, maxMembersComplex);

		task2.execute();
	}

	private Collection<OsmFile> createOsmFiles(List<Path> paths)
	{
		List<OsmFile> files = new ArrayList<>();
		for (Path path : paths) {
			files.add(new OsmFile(path, inputFormat));
		}
		return files;
	}

}
