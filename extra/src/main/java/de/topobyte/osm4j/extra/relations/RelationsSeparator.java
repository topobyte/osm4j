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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;

import de.topobyte.osm4j.core.access.OsmInputAccessFactory;
import de.topobyte.osm4j.core.access.OsmIteratorInput;
import de.topobyte.osm4j.core.access.OsmOutputStream;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmRelationMember;
import de.topobyte.osm4j.utils.OsmIoUtils;
import de.topobyte.osm4j.utils.OsmOutputConfig;
import de.topobyte.osm4j.utils.StreamUtil;

public class RelationsSeparator
{

	private OsmInputAccessFactory inputFactory;
	private Path pathOutputSimple;
	private Path pathOutputComplex;
	private OsmOutputConfig outputConfig;

	public RelationsSeparator(OsmInputAccessFactory inputFactory,
			Path pathOutputSimple, Path pathOutputComplex,
			OsmOutputConfig outputConfig)
	{
		this.inputFactory = inputFactory;
		this.pathOutputSimple = pathOutputSimple;
		this.pathOutputComplex = pathOutputComplex;
		this.outputConfig = outputConfig;
	}

	private TLongSet idsHasRelationMembers = new TLongHashSet();
	private TLongSet idsIsRelationMember = new TLongHashSet();

	public void execute() throws IOException
	{
		findComplexRelations();

		separateRelations();
	}

	private void findComplexRelations() throws IOException
	{
		OsmIteratorInput input = inputFactory.createIterator(false);

		for (OsmRelation relation : new RelationIterator(input.getIterator())) {
			boolean hasRelationMembers = false;
			for (int i = 0; i < relation.getNumberOfMembers(); i++) {
				OsmRelationMember member = relation.getMember(i);
				if (member.getType() == EntityType.Relation) {
					hasRelationMembers = true;
					idsIsRelationMember.add(member.getId());
				}
			}
			if (hasRelationMembers) {
				idsHasRelationMembers.add(relation.getId());
			}
		}

		input.close();
	}

	private void separateRelations() throws IOException
	{
		OutputStream outSimple = StreamUtil
				.bufferedOutputStream(pathOutputSimple);
		OutputStream outComplex = StreamUtil
				.bufferedOutputStream(pathOutputComplex);

		OsmOutputStream osmOutputSimple = OsmIoUtils.setupOsmOutput(outSimple,
				outputConfig);
		OsmOutputStream osmOutputComplex = OsmIoUtils.setupOsmOutput(
				outComplex, outputConfig);

		OsmIteratorInput input = inputFactory.createIterator(outputConfig
				.isWriteMetadata());

		for (OsmRelation relation : new RelationIterator(input.getIterator())) {
			long id = relation.getId();
			if (idsIsRelationMember.contains(id)
					|| idsHasRelationMembers.contains(id)) {
				osmOutputComplex.write(relation);
			} else {
				osmOutputSimple.write(relation);
			}
		}

		osmOutputSimple.complete();
		osmOutputComplex.complete();

		outSimple.close();
		outComplex.close();

		input.close();
	}

}
