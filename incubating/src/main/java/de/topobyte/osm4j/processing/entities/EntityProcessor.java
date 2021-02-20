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

package de.topobyte.osm4j.processing.entities;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.TopologyException;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.locationtech.jts.geom.prep.PreparedGeometryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.slimjars.dist.gnu.trove.iterator.TLongIterator;
import com.slimjars.dist.gnu.trove.map.TLongObjectMap;
import com.slimjars.dist.gnu.trove.map.hash.TLongObjectHashMap;

import de.topobyte.osm4j.core.access.ProgressMonitor;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmEntity;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmRelationMember;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.core.model.util.OsmModelUtil;
import de.topobyte.osm4j.core.resolve.EntityNotFoundException;
import de.topobyte.osm4j.core.resolve.OsmEntityProvider;
import de.topobyte.osm4j.diskstorage.EntityProviderImpl;
import de.topobyte.osm4j.diskstorage.nodedb.NodeDB;
import de.topobyte.osm4j.diskstorage.vardb.VarDB;
import de.topobyte.osm4j.diskstorage.waydb.WayRecordWithTags;
import de.topobyte.osm4j.geometry.GeometryBuilder;
import de.topobyte.osm4j.geometry.OsmEntityGeometryHandler;
import de.topobyte.simplemapfile.core.EntityFile;
import de.topobyte.simplemapfile.xml.SmxFileWriter;

public class EntityProcessor
{

	static final Logger logger = LoggerFactory.getLogger(EntityProcessor.class);

	private NodeDB nodeDB = null;
	private VarDB<WayRecordWithTags> wayDB = null;

	private ProgressMonitor progressMonitor;

	private GeometryBuilder builder;

	private TLongObjectMap<OsmRelation> relations = new TLongObjectHashMap<>();
	private RelationStore relationStore;
	private OsmEntityProvider entityProvider;

	private Geometry boundary;
	private GeometryFactory factory = new GeometryFactory();
	private Path failedPolygonsDir;
	private PreparedGeometry pg;

	private OsmEntityGeometryHandler handler;

	public EntityProcessor(OsmEntityGeometryHandler handler, NodeDB nodeDB,
			VarDB<WayRecordWithTags> wayDB, Geometry boundary,
			Path failedPolygonsDir)
	{
		this.handler = handler;
		this.nodeDB = nodeDB;
		this.wayDB = wayDB;
		this.boundary = boundary;
		this.failedPolygonsDir = failedPolygonsDir;
	}

	public void prepare() throws FileNotFoundException, IOException
	{
		progressMonitor = new ProgressMonitor("entity processor");

		relationStore = new RelationStore();

		entityProvider = new CombinedEntityProvider(
				new EntityProviderImpl(nodeDB, wayDB), relations,
				relationStore);

		if (failedPolygonsDir != null) {
			Util.ensureDirectoryExistsAndIsWritable(failedPolygonsDir,
					"dump dir for failed polygons");
		}

		pg = PreparedGeometryFactory.prepare(boundary);

		builder = new GeometryBuilder();
		builder.getWayBuilder().setIncludePuntal(false);
		builder.getRegionBuilder().setIncludeLineal(false);
		builder.getRegionBuilder().setIncludePuntal(false);
	}

	public void processRelationPass1(OsmRelation relation)
	{
		relations.put(relation.getId(), relation);
		/*
		 * Iterate relations to store them in a store so that we may later look
		 * up relations from ways that may be contained within relations. We use
		 * this do deduce tags of relations and throw away tags from outer ways.
		 */
		Map<String, String> tags = OsmModelUtil.getTagsAsMap(relation);
		if (!useRelation(tags)) {
			return;
		}
		relationStore.put(relation);
	}

	public void relationsPass1Done()
	{
		TLongIterator iter = relationStore.getRelationData().keySet()
				.iterator();
		while (iter.hasNext()) {
			OsmRelation relation = relations.get(iter.next());
			transform(relation);
		}
	}

	private void transform(OsmRelation relation)
	{
		List<OsmWay> outerWays = new ArrayList<>();

		List<OsmRelationMember> members = OsmModelUtil.membersAsList(relation);
		for (OsmRelationMember member : members) {
			if (member.getType() != EntityType.Way) {
				continue;
			}
			String role = member.getRole();
			if (role.equals("outer")) {
				try {
					outerWays.add(entityProvider.getWay(member.getId()));
				} catch (EntityNotFoundException e) {
					// ignore
				}
			}
		}

		int nOuterWays = outerWays.size();
		if (nOuterWays == 1) {
			OsmWay outer = outerWays.get(0);
			Map<String, String> tags = OsmModelUtil.getTagsAsMap(outer);
			relationStore.applyTags(tags, relation);
		} else if (nOuterWays > 1) {
			Map<String, String> commonTags = TagHelper
					.commonTagsEntities(outerWays);
			relationStore.applyTags(commonTags, relation);
		}
	}

	private void warn(OsmEntity entity, String message)
	{
		logger.warn(String.format("%s %d: %s", entity.getType().toString(),
				entity.getId(), message));
	}

	private void warnBuildGeometry(OsmEntity entity, IllegalArgumentException e)
	{
		logger.warn(String.format(
				"%s %d: unable to build geometry. IllegalArgumentException: %s",
				entity.getType().toString(), entity.getId(), e.getMessage()));
	}

	private void warnBuildGeometry(OsmEntity entity, EntityNotFoundException e)
	{
		logger.warn(String.format(
				"%s %d: unable to build geometry. Entity not found: %s",
				entity.getType().toString(), entity.getId(), e.getMessage()));
	}

	public void processNode(OsmNode node)
	{
		progressMonitor.nodeProcessed();
		Map<String, String> tags = OsmModelUtil.getTagsAsMap(node);

		Point point = builder.build(node);

		if (!pg.intersects(point)) {
			return;
		}

		handler.processNode(node, point, tags);
	}

	public void processWay(OsmWay way)
	{
		progressMonitor.wayProcessed();
		Map<String, String> tags = OsmModelUtil.getTagsAsMap(way);

		/*
		 * Remove from inner ways of relations the tags that the relation
		 * contains. This removes for example building=yes tags from inner
		 * building ways.
		 */
		if (relationStore.isInner(way)) {
			Collection<OsmRelation> relations = relationStore
					.getInnerRelations(way);
			for (OsmRelation relation : relations) {
				relation = relationStore.getReplacement(relation);
				Map<String, String> relationTags = OsmModelUtil
						.getTagsAsMap(relation);
				relationStore.subtractTags(tags, relationTags);
			}
		}

		/*
		 * Remove from outer ways of relations the tags that the relation
		 * contains. This removes for example building=yes tags from outer
		 * building ways.
		 */
		if (relationStore.isOuter(way)) {
			Collection<OsmRelation> relations = relationStore
					.getOuterRelations(way);
			for (OsmRelation relation : relations) {
				relation = relationStore.getReplacement(relation);
				Map<String, String> relationTags = OsmModelUtil
						.getTagsAsMap(relation);
				relationStore.subtractTags(tags, relationTags);
			}
		}

		if (tags.isEmpty()) {
			return;
		}

		Geometry geometry;
		try {
			geometry = builder.build(way, entityProvider);
		} catch (IllegalArgumentException e) {
			warnBuildGeometry(way, e);
			return;
		} catch (EntityNotFoundException e) {
			warnBuildGeometry(way, e);
			return;
		}
		if (!geometry.isValid()) {
			return;
		}

		if (!(geometry instanceof LineString)) {
			return;
		}

		LineString string = (LineString) geometry;

		if (!pg.intersects(string)) {
			return;
		}

		if (pg.covers(string)) {
			handler.processWayString(way, string, tags);
			return;
		}

		if (!string.isClosed()) {
			Geometry intersection = string.intersection(boundary);
			if (intersection instanceof LineString) {
				string = (LineString) intersection;
				handler.processWayString(way, string, tags);
			} else if (intersection instanceof MultiLineString) {
				MultiLineString multi = (MultiLineString) intersection;
				for (int i = 0; i < multi.getNumGeometries(); i++) {
					Geometry part = multi.getGeometryN(i);
					LineString partString = (LineString) part;
					handler.processWayString(way, partString, tags);
				}
			} else {
				String wkt = intersection.toString();
				int maxChars = 100;
				String ellipsis = "";
				if (wkt.length() > maxChars) {
					wkt = wkt.substring(0, maxChars);
					ellipsis = "...";
				}
				warn(way, String.format(
						"intersection of LineString not processed. tags: %s, geometry: %s%s",
						tags, wkt, ellipsis));
			}
			return;
		}

		LinearRing ring = factory.createLinearRing(string.getCoordinates());
		Polygon polygon = factory.createPolygon(ring, null);
		Geometry intersection = null;
		try {
			intersection = boundary.intersection(polygon);
		} catch (TopologyException e) {
			warn(way,
					"TopologyException while computing intersection of LineString with boundary: "
							+ e.getMessage());
			handler.processWayString(way, string, tags);
			return;
		}

		if (intersection instanceof Polygon) {
			Polygon p = (Polygon) intersection;
			MultiPolygon mp = factory.createMultiPolygon(new Polygon[] { p });
			processCuttedPolygon(way, mp, way, tags);
		} else if (intersection instanceof MultiPolygon) {
			MultiPolygon mp = (MultiPolygon) intersection;
			processCuttedPolygon(way, mp, way, tags);
		} else {
			warn(way,
					"closed way intersection is neither a polygon nor a multipolygon: "
							+ intersection);
		}
	}

	public void processRelationPass2(OsmRelation relation)
	{
		progressMonitor.relationProcessed();
		Map<String, String> originalTags = OsmModelUtil.getTagsAsMap(relation);

		if (!useRelation(originalTags)) {
			return;
		}

		relation = relationStore.getReplacement(relation);

		Map<String, String> tags = OsmModelUtil.getTagsAsMap(relation);
		// logger.debug("merged tags: " + tags);

		boolean useless = false;

		if (tags.size() == 0) {
			useless = true;
		}
		if (tags.containsKey("type")) {
			String type = tags.get("type");
			if (type.equals("boundary") || type.equals("multipolygon")) {
				if (tags.size() == 1) {
					useless = true;
				}
			}
		}

		if (useless) {
			dumpUselessRelation(relation, tags);
			return;
		}

		Geometry geometry;
		try {
			geometry = builder.build(relation, entityProvider);
		} catch (IllegalArgumentException e) {
			warnBuildGeometry(relation, e);
			return;
		} catch (EntityNotFoundException e) {
			warnBuildGeometry(relation, e);
			return;
		}

		boolean valid = false;
		try {
			valid = geometry.isValid();
		} catch (RuntimeException e) {
			warn(relation, "RuntimeException while checking isValid(): "
					+ e.getMessage());
		}

		if (!(geometry instanceof MultiPolygon)) {
			return;
		}

		MultiPolygon polygon = (MultiPolygon) geometry;

		if (!valid) {
			try {
				Geometry buffer = polygon.buffer(0);
				if (buffer instanceof MultiPolygon) {
					polygon = (MultiPolygon) buffer;
				} else if (buffer instanceof Polygon) {
					polygon = factory.createMultiPolygon(
							new Polygon[] { (Polygon) buffer });
				} else {
					warn(relation, "Error while creating buffer"
							+ " of invalid polygon: result is not polygonal");
				}
			} catch (RuntimeException e) {
				warn(relation, "RuntimeException while creating buffer of"
						+ " invalid polygon: " + e.getMessage());
			}
		}

		if (polygon.isEmpty()) {
			warn(relation, "empty multipolygon.");
			return;
		}

		Point centroid;
		try {
			centroid = polygon.getCentroid();
			if (!centroid.isValid()) {
				warn(relation, "polygon centroid not valid.");
				return;
			}
		} catch (RuntimeException e) {
			warn(relation, "RuntimeException while computing centroid: "
					+ e.getMessage());
			dumpFailedPolygon(polygon, relation, tags, "centroid calculation");
			return;
		}

		try {
			if (!boundary.intersects(polygon)) {
				return;
			}
		} catch (TopologyException e) {
			warn(relation,
					"TopologyException while testing for intersection with boundary: "
							+ e.getMessage());
			dumpFailedPolygon(polygon, relation, tags,
					"intersection test with boundary");
			handler.processMultipolygon(relation, polygon, tags, centroid);
			return;
		}

		try {
			if (boundary.contains(polygon)) {
				handler.processMultipolygon(relation, polygon, tags, centroid);
				return;
			}
		} catch (TopologyException e) {
			warn(relation, "TopologyException while testing for containment: "
					+ e.getMessage());
			dumpFailedPolygon(polygon, relation, tags,
					"testing for containment");
			handler.processMultipolygon(relation, polygon, tags, centroid);
			return;
		}

		Geometry intersection = null;
		try {
			intersection = boundary.intersection(polygon);
		} catch (TopologyException e) {
			warn(relation, "TopologyException while computing intersection: "
					+ e.getMessage());
			dumpFailedPolygon(polygon, relation, tags,
					"calculating intersection with boundary");
			handler.processMultipolygon(relation, polygon, tags, centroid);
			return;
		}

		if (intersection instanceof Polygon) {
			Polygon p = (Polygon) intersection;
			MultiPolygon mp = factory.createMultiPolygon(new Polygon[] { p });
			processCuttedPolygon(relation, mp, relation, tags);
		} else if (intersection instanceof MultiPolygon) {
			MultiPolygon mp = (MultiPolygon) intersection;
			processCuttedPolygon(relation, mp, relation, tags);
		} else {
			dumpFailedPolygon(polygon, relation, tags,
					"illegal type of geometry: "
							+ intersection.getGeometryType());
			warn(relation,
					"relation intersection is neither a polygon nor a multipolygon: "
							+ intersection);
		}

	}

	private void dumpUselessRelation(OsmRelation relation,
			Map<String, String> tags)
	{
		warn(relation, "could not be used, tags: " + tags);
		for (int i = 0; i < relation.getNumberOfMembers(); i++) {
			OsmRelationMember member = relation.getMember(i);
			if (!(member.getType() == EntityType.Way)) {
				continue;
			}
			if (!member.getRole().equals("outer")) {
				continue;
			}
			long wayId = member.getId();
			try {
				OsmWay way = entityProvider.getWay(wayId);
				Map<String, String> wayTags = OsmModelUtil.getTagsAsMap(way);
				logger.warn("outer way tags: " + wayTags);
			} catch (EntityNotFoundException e) {
				// ignore here
			}
		}
	}

	private boolean useRelation(Map<String, String> tags)
	{
		String type = tags.get("type");
		if (type == null) {
			return false;
		}
		return type.equals("multipolygon");
	}

	private void processCuttedPolygon(OsmWay way, MultiPolygon mp,
			OsmEntity entity, Map<String, String> tags)
	{
		Point centroid;
		try {
			centroid = mp.getCentroid();
			if (!centroid.isValid()) {
				logger.debug("polygon created from way not valid: "
						+ entity.getId());
				return;
			}
		} catch (RuntimeException e) {
			logger.debug("RuntimeException with closed way: " + entity.getId());
			return;
		}

		handler.processMultipolygon(way, mp, tags, centroid);
	}

	private void processCuttedPolygon(OsmRelation relation, MultiPolygon mp,
			OsmEntity entity, Map<String, String> tags)
	{
		Point centroid;
		try {
			centroid = mp.getCentroid();
			if (!centroid.isValid()) {
				logger.debug("polygon created from way not valid: "
						+ entity.getId());
				return;
			}
		} catch (RuntimeException e) {
			logger.debug("RuntimeException with closed way: " + entity.getId());
			return;
		}

		handler.processMultipolygon(relation, mp, tags, centroid);
	}

	private void dumpFailedPolygon(MultiPolygon polygon, OsmEntity osmEntity,
			Map<String, String> tags, String failureCause)
	{
		logger.debug("dumping failed polygon");
		if (failedPolygonsDir == null) {
			logger.debug("dumping disabled");
			return;
		}
		EntityFile entityFile = new EntityFile();
		entityFile.setGeometry(polygon);
		for (String key : tags.keySet()) {
			String value = tags.get(key);
			entityFile.addTag(key, value);
		}
		entityFile.addTag("cause of failure", failureCause);
		String filename = null;
		if (osmEntity instanceof OsmRelation) {
			filename = "fail-relation" + osmEntity.getId() + ".smx";
		} else if (osmEntity instanceof OsmWay) {
			filename = "fail-way" + osmEntity.getId() + ".smx";
		} else {
			return;
		}
		Path dumpFile = failedPolygonsDir.resolve(filename);
		try {
			SmxFileWriter.write(entityFile, dumpFile.toFile());
		} catch (TransformerException e) {
			logger.warn("error while dumping entity", e);
		} catch (ParserConfigurationException e) {
			logger.warn("error while dumping entity", e);
		} catch (IOException e) {
			logger.warn("error while dumping entity", e);
		}
	}

}
