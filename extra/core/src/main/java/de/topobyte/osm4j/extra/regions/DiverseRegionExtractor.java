// Copyright 2021 Sebastian Kuerten
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

package de.topobyte.osm4j.extra.regions;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.locationtech.jts.geom.Geometry;

import de.topobyte.osm4j.core.access.OsmIteratorInput;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmEntity;
import de.topobyte.osm4j.core.model.iface.OsmRelationMember;
import de.topobyte.osm4j.core.model.impl.Relation;
import de.topobyte.osm4j.core.model.util.OsmModelUtil;
import de.topobyte.osm4j.core.resolve.EntityNotFoundException;
import de.topobyte.osm4j.core.resolve.OsmEntityProvider;
import de.topobyte.osm4j.diskstorage.EntityDbSetup;
import de.topobyte.osm4j.diskstorage.EntityProviderImpl;
import de.topobyte.osm4j.diskstorage.nodedb.NodeDB;
import de.topobyte.osm4j.diskstorage.vardb.VarDB;
import de.topobyte.osm4j.diskstorage.waydb.WayRecord;
import de.topobyte.osm4j.geometry.GeometryBuilder;
import de.topobyte.osm4j.utils.OsmFile;
import de.topobyte.osm4j.utils.OsmFileInput;
import de.topobyte.simplemapfile.core.EntityFile;
import de.topobyte.simplemapfile.xml.SmxFileWriter;

public class DiverseRegionExtractor
{

	public enum Naming {
		ID,
		TYPE_ID,
		WITH_NAME
	}

	private OsmFile input;
	private Naming naming;

	private Path fileOutDir;

	private NodeDB nodeDB;
	private VarDB<WayRecord> wayDB;

	private OsmEntityProvider entityProvider;

	public void prepare(OsmFile input, String argOutput, Path argNodeData,
			Path argNodeIndex, Path argWayData, Path argWayIndex, Naming naming)
	{
		this.input = input;
		this.naming = naming;

		System.out.println("creating output directory if necessary");
		fileOutDir = Paths.get(argOutput);
		try {
			Files.createDirectories(fileOutDir);
		} catch (IOException e1) {
			System.out.println("unable to create output directory");
			System.exit(1);
		}

		try {
			EntityDbSetup.createNodeDb(input.getPath(), argNodeIndex,
					argNodeData);
		} catch (IOException e) {
			System.out.println(
					"error while populating node database: " + e.getMessage());
			System.exit(1);
		}
		try {
			EntityDbSetup.createWayDb(input.getPath(), argWayIndex, argWayData,
					false);
		} catch (IOException e) {
			System.out.println(
					"error while populating way database: " + e.getMessage());
			System.exit(1);
		}

		System.out.println("opening node datbase");
		try {
			nodeDB = new NodeDB(argNodeData, argNodeIndex);
		} catch (FileNotFoundException e) {
			System.out
					.println("unable to open node database: " + e.getMessage());
			System.exit(1);
		}

		System.out.println("opening way datbase");
		try {
			wayDB = new VarDB<>(argWayData, argWayIndex, new WayRecord(0));
		} catch (FileNotFoundException e) {
			System.out
					.println("unable to open way database: " + e.getMessage());
			System.exit(1);
		}

		entityProvider = new EntityProviderImpl(nodeDB, wayDB);
	}

	public void execute() throws IOException
	{
		GeometryBuilder builder = new GeometryBuilder();

		OsmFileInput in = new OsmFileInput(input);
		OsmIteratorInput iterator = in.createIterator(true, false);

		int i = 0;
		relations: for (EntityContainer container : iterator.getIterator()) {
			if (container.getType() != EntityType.Relation) {
				continue;
			}
			OsmEntity entity = container.getEntity();
			Relation relation = (Relation) entity;

			Map<String, String> tags = OsmModelUtil.getTagsAsMap(relation);

			List<Path> files = select(relation, tags);
			if (files.size() == 0) {
				continue;
			}
			System.out
					.println(String.format("%d: %s", ++i, relation.toString()));
			Geometry region;
			try {
				for (OsmRelationMember member : OsmModelUtil
						.membersAsList(relation)) {
					if (member.getType() == EntityType.Relation) {
						System.out
								.println("Ignoring region with child regions");
						System.out.println("tags: " + tags);
						continue relations;
					}
				}

				region = builder.build(relation, entityProvider);
			} catch (EntityNotFoundException e) {
				System.out.println("unable to build region: " + e.getMessage());
				continue;
			}

			for (Path file : files) {
				Files.createDirectories(file.getParent());
				try {
					export(region, tags, file);
				} catch (IOException e) {
					System.out.println("unable to serialize, IOException: "
							+ e.getMessage());
				} catch (TransformerException e) {
					System.out.println(
							"unable to serialize, TransformerException: "
									+ e.getMessage());
				} catch (ParserConfigurationException e) {
					System.out.println(
							"unable to serialize, ParserConfigurationException: "
									+ e.getMessage());
				}
			}
		}
	}

	private void export(Geometry region, Map<String, String> tags, Path file)
			throws TransformerException, ParserConfigurationException,
			IOException
	{
		EntityFile entityFile = new EntityFile();
		entityFile.setGeometry(region);
		for (String key : tags.keySet()) {
			entityFile.addTag(key, tags.get(key));
		}
		SmxFileWriter.write(entityFile, file);
	}

	private List<Path> select(Relation relation, Map<String, String> tags)
	{
		List<Path> files = new ArrayList<>();

		selectAdminAreas(files, relation, tags);

		selectBoundaryPostalCodes(files, relation, tags);

		selectPostalCodes(files, relation, tags);

		selectMultipolygons(files, relation, tags);

		selectBoundaries(files, relation, tags);

		selectAll(files, relation, tags);

		return files;
	}

	private void selectAll(List<Path> files, Relation relation,
			Map<String, String> tags)
	{
		Path allDir = fileOutDir.resolve("all");
		String filename = String.format("%d.smx", relation.getId());
		Path output = allDir.resolve(filename);
		files.add(output);
	}

	private void selectPostalCodes(List<Path> files, Relation relation,
			Map<String, String> tags)
	{
		String code = tags.get("postal_code");
		if (code == null) {
			return;
		}
		String filename = name(relation.getId(), tags, "postal_code");
		Path postDir = fileOutDir.resolve("postalcode2");
		Path output = postDir.resolve(filename);
		files.add(output);
	}

	private void selectBoundaryPostalCodes(List<Path> files, Relation relation,
			Map<String, String> tags)
	{
		String boundary = tags.get("boundary");
		if (boundary == null) {
			return;
		}
		if (boundary.equals("postal_code")) {
			String code = tags.get("postal_code");
			if (code == null) {
				code = "null";
			}
			Path postDir = fileOutDir.resolve("postalcode1");
			String filename = name(relation.getId(), tags, "postal_code");
			Path output = postDir.resolve(filename);
			files.add(output);
		}
	}

	private void selectAdminAreas(List<Path> files, Relation relation,
			Map<String, String> tags)
	{
		String adminLevel = tags.get("admin_level");
		if (adminLevel == null) {
			return;
		}
		Path adminLevels = fileOutDir.resolve("admin");
		Path adminDir = adminLevels.resolve(adminLevel);
		String filename = name(relation.getId(), tags, "name");
		Path output = adminDir.resolve(filename);
		files.add(output);
	}

	private void selectMultipolygons(List<Path> files, Relation relation,
			Map<String, String> tags)
	{
		String type = tags.get("type");
		if (type == null) {
			return;
		}
		if (!type.equals("multipolygon")) {
			return;
		}
		Path dir = fileOutDir.resolve("multipolygons");
		String filename = name(relation.getId(), tags, "name");
		Path output = dir.resolve(filename);
		files.add(output);
	}

	private void selectBoundaries(List<Path> files, Relation relation,
			Map<String, String> tags)
	{
		String type = tags.get("type");
		if (type == null) {
			return;
		}
		if (!type.equals("boundary")) {
			return;
		}
		Path dir = fileOutDir.resolve("boundaries");
		String filename = name(relation.getId(), tags, "name");
		Path output = dir.resolve(filename);
		files.add(output);
	}

	private String name(long id, Map<String, String> tags, String key)
	{
		if (naming == Naming.ID) {
			return String.format("%d.smx", id);
		} else if (naming == Naming.TYPE_ID) {
			return String.format("relation-%d.smx", id);
		} else if (naming == Naming.WITH_NAME) {
			String name = tags.get(key);
			if (name == null) {
				name = "null";
			}
			name = sane(name);
			return String.format("%d_%s.smx", id, name);
		}
		return null;
	}

	private String sane(String input)
	{
		String sane = input.replaceAll("/", "_");
		return sane;
	}

}
