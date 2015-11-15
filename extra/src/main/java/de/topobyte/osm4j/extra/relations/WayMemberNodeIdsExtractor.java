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

import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.model.util.OsmModelUtil;
import de.topobyte.osm4j.extra.idlist.IdListOutputStream;
import de.topobyte.osm4j.utils.FileFormat;
import de.topobyte.osm4j.utils.OsmIoUtils;
import de.topobyte.osm4j.utils.StreamUtil;

public class WayMemberNodeIdsExtractor
{

	private Path[] dirsData;

	private String fileNamesWays;
	private String fileNamesNodeIds;

	private FileFormat inputFormat;

	private List<Path> subdirs;

	public WayMemberNodeIdsExtractor(Path[] dirsData, String fileNamesWays,
			String fileNamesNodeIds, FileFormat inputFormat)
	{
		this.dirsData = dirsData;
		this.fileNamesWays = fileNamesWays;
		this.fileNamesNodeIds = fileNamesNodeIds;
		this.inputFormat = inputFormat;
	}

	public void execute() throws IOException
	{
		init();

		int i = 0;
		for (Path path : subdirs) {
			System.out.println(String.format("Processing directory %d of %d",
					++i, subdirs.size()));
			extract(path);
		}
	}

	private void init() throws IOException
	{
		for (Path dirData : dirsData) {
			if (!Files.isDirectory(dirData)) {
				System.out.println("Data path is not a directory: " + dirData);
				System.exit(1);
			}
		}

		subdirs = new ArrayList<>();
		for (Path dirData : dirsData) {
			File[] subs = dirData.toFile().listFiles();
			for (File sub : subs) {
				if (!sub.isDirectory()) {
					continue;
				}
				Path subPath = sub.toPath();
				Path ways = subPath.resolve(fileNamesWays);
				if (!Files.exists(ways)) {
					continue;
				}
				subdirs.add(subPath);
			}
		}
	}

	private void extract(Path path) throws IOException
	{
		Path pathWays = path.resolve(fileNamesWays);
		Path pathNodeIds = path.resolve(fileNamesNodeIds);

		InputStream input = StreamUtil.bufferedInputStream(pathWays.toFile());
		OsmIterator osmIterator = OsmIoUtils.setupOsmIterator(input,
				inputFormat, false);

		OutputStream outputNodeIds = StreamUtil
				.bufferedOutputStream(pathNodeIds.toFile());
		IdListOutputStream idOutputNodeIds = new IdListOutputStream(
				outputNodeIds);

		TLongSet nodeIdsSet = new TLongHashSet();

		for (EntityContainer container : osmIterator) {
			if (container.getType() != EntityType.Way) {
				continue;
			}
			OsmWay way = (OsmWay) container.getEntity();
			for (long id : OsmModelUtil.nodesAsList(way).toArray()) {
				nodeIdsSet.add(id);
			}
		}

		input.close();

		long[] nodesIds = nodeIdsSet.toArray();
		Arrays.sort(nodesIds);
		for (long id : nodesIds) {
			idOutputNodeIds.write(id);
		}
		idOutputNodeIds.close();
	}

}
