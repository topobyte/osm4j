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
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.slimjars.dist.gnu.trove.set.TLongSet;
import com.slimjars.dist.gnu.trove.set.hash.TLongHashSet;

import de.topobyte.melon.io.StreamUtil;
import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmRelationMember;
import de.topobyte.osm4j.core.model.util.OsmModelUtil;
import de.topobyte.osm4j.extra.idlist.IdListOutputStream;
import de.topobyte.osm4j.utils.FileFormat;
import de.topobyte.osm4j.utils.OsmIoUtils;

public class MemberIdsExtractor
{

	final static Logger logger = LoggerFactory
			.getLogger(MemberIdsExtractor.class);

	private Path[] dirsData;

	private String fileNamesRelations;
	private String fileNamesNodeIds;
	private String fileNamesWayIds;

	private FileFormat inputFormat;

	private List<Path> subdirs;

	public MemberIdsExtractor(Path[] dirsData, String fileNamesRelations,
			String fileNamesNodeIds, String fileNamesWayIds,
			FileFormat inputFormat)
	{
		this.dirsData = dirsData;
		this.fileNamesRelations = fileNamesRelations;
		this.fileNamesNodeIds = fileNamesNodeIds;
		this.fileNamesWayIds = fileNamesWayIds;
		this.inputFormat = inputFormat;
	}

	public void execute() throws IOException
	{
		init();

		int i = 0;
		for (Path path : subdirs) {
			logger.info(String.format("Processing directory %d of %d", ++i,
					subdirs.size()));
			extract(path);
		}
	}

	private void init() throws IOException
	{
		for (Path dirData : dirsData) {
			if (!Files.isDirectory(dirData)) {
				String error = "Data path is not a directory: " + dirData;
				logger.error(error);
				throw new IOException(error);
			}
		}

		subdirs = new ArrayList<>();
		for (Path dirData : dirsData) {
			try (DirectoryStream<Path> subs = Files
					.newDirectoryStream(dirData)) {
				for (Path sub : subs) {
					if (!Files.isDirectory(sub)) {
						continue;
					}
					Path relations = sub.resolve(fileNamesRelations);
					if (!Files.exists(relations)) {
						continue;
					}
					subdirs.add(sub);
				}
			}
		}
	}

	private void extract(Path path) throws IOException
	{
		Path pathRelations = path.resolve(fileNamesRelations);
		Path pathNodeIds = path.resolve(fileNamesNodeIds);
		Path pathWayIds = path.resolve(fileNamesWayIds);

		InputStream input = StreamUtil
				.bufferedInputStream(pathRelations.toFile());
		OsmIterator osmIterator = OsmIoUtils.setupOsmIterator(input,
				inputFormat, false);

		OutputStream outputNodeIds = StreamUtil
				.bufferedOutputStream(pathNodeIds.toFile());
		IdListOutputStream idOutputNodeIds = new IdListOutputStream(
				outputNodeIds);

		TLongSet nodeIdsSet = new TLongHashSet();
		TLongSet wayIdsSet = new TLongHashSet();

		for (EntityContainer container : osmIterator) {
			if (container.getType() != EntityType.Relation) {
				continue;
			}
			OsmRelation relation = (OsmRelation) container.getEntity();
			for (OsmRelationMember member : OsmModelUtil
					.membersAsList(relation)) {
				if (member.getType() == EntityType.Node) {
					nodeIdsSet.add(member.getId());
				} else if (member.getType() == EntityType.Way) {
					wayIdsSet.add(member.getId());
				}
			}
		}

		input.close();

		long[] nodesIds = nodeIdsSet.toArray();
		Arrays.sort(nodesIds);
		for (long id : nodesIds) {
			idOutputNodeIds.write(id);
		}
		idOutputNodeIds.close();

		OutputStream outputWayIds = StreamUtil
				.bufferedOutputStream(pathWayIds.toFile());
		IdListOutputStream idOutputWayIds = new IdListOutputStream(
				outputWayIds);

		long[] wayIds = wayIdsSet.toArray();
		Arrays.sort(wayIds);
		for (long id : wayIds) {
			idOutputWayIds.write(id);
		}
		idOutputWayIds.close();
	}

}
