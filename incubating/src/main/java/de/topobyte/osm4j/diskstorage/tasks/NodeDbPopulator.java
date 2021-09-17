// Copyright 2020 Sebastian Kuerten
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

package de.topobyte.osm4j.diskstorage.tasks;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.impl.Node;
import de.topobyte.osm4j.diskstorage.nodedb.DbNode;
import de.topobyte.osm4j.diskstorage.nodedb.NodeDB;

/**
 * Create a node database from an osm iterator.
 * 
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
public class NodeDbPopulator
{

	static final Logger logger = LoggerFactory.getLogger(NodeDbPopulator.class);

	private final OsmIterator iterator;
	private final Path outputIndex;
	private final Path outputData;

	public NodeDbPopulator(OsmIterator iterator, Path outputIndex,
			Path outputData)
	{
		this.iterator = iterator;
		this.outputIndex = outputIndex;
		this.outputData = outputData;
	}

	public void execute() throws IOException
	{
		// make sure we have fresh files for the node database.
		logger.debug("making sure database is empty");
		if (Files.exists(outputIndex)) {
			Files.delete(outputIndex);
			if (Files.exists(outputIndex)) {
				throw new IOException("unable to delete existing index");
			}
		}
		if (Files.exists(outputData)) {
			Files.delete(outputData);
			if (Files.exists(outputData)) {
				throw new IOException("unable to delete existing database");
			}
		}

		// create the database
		logger.debug("creating database");
		NodeDB nodeDB;
		try {
			nodeDB = new NodeDB(outputData, outputIndex);
		} catch (FileNotFoundException e) {
			throw new IOException("unable to create database");
		}

		// read and insert nodes
		logger.debug("inserting nodes");
		int i = 0; // count nodes
		while (iterator.hasNext()) {
			EntityContainer container = iterator.next();
			if (container.getType() == EntityType.Node) {
				Node node = (Node) container.getEntity();

				DbNode n = new DbNode(node.getId(), node.getLongitude(),
						node.getLatitude());
				try {
					nodeDB.addNode(n);
				} catch (IOException e) {
					logger.error("unable to insert node");
					logger.debug(e.getMessage());
				}
				i++;
				if ((i % 10000) == 0) {
					logger.debug("nodes inserted: " + i);
				}
			}
		}
		logger.debug("nodes inserted: " + i);

		// close database
		logger.debug("closing database");
		try {
			nodeDB.close();
		} catch (IOException e) {
			throw new IOException("unable to close database");
		}
	}

}
