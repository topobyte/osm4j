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

package de.topobyte.osm4j.pbf.raf;

import java.io.IOException;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.pbf.util.PbfMeta;

public class FileStructureAnalyzer
{

	final static Logger logger = LoggerFactory
			.getLogger(FileStructureAnalyzer.class);

	/**
	 * Analyze the file structure of the specified PBF file. If not initialized
	 * yet, this method will build the file's block index by calling
	 * {@link PbfFile#buildBlockIndex()}. After that it will scan the blocks for
	 * the contained element types and build a {@link FileStructure} object that
	 * specifies which blocks of the file contain nodes, which contain ways and
	 * which contain relations.
	 * 
	 * The analyzer will find the boundaries between entity types using binary
	 * search. Since a PBF file block needs to be parsed completely (and
	 * possibly extracted beforehand) before we can tell which entities it
	 * contains, it is computationally expensive to read a block. Using binary
	 * search minimizes the number of blocks that will be parsed in order to
	 * find the entity block boundaries.
	 * 
	 * For the algorithm to work correctly, it is of course necessary that the
	 * input file contains the OSM entities in their correct order, i.e. first
	 * nodes if any, then ways if any and finally relations if any.
	 * 
	 * @param pbfFile
	 * @return
	 * @throws IOException
	 */
	public static FileStructure analyze(PbfFile pbfFile) throws IOException
	{
		if (!pbfFile.isBlockIndexInitialized()) {
			pbfFile.buildBlockIndex();
		}

		FileStructureAnalyzer analyzer = new FileStructureAnalyzer(pbfFile);
		analyzer.execute();
		analyzer.logEntityBounds();

		Interval blocksNodes = null;
		Interval blocksWays = null;
		Interval blocksRelations = null;

		if (analyzer.containsNodes) {
			blocksNodes = new Interval(0, analyzer.maxN);
		}
		if (analyzer.containsWays) {
			blocksWays = new Interval(analyzer.minW, analyzer.maxW);
		}
		if (analyzer.containsRelations) {
			blocksRelations = new Interval(analyzer.minR,
					pbfFile.getNumberOfDataBlocks() - 1);
		}

		return new FileStructure(blocksNodes, blocksWays, blocksRelations);
	}

	private PbfFile pbfFile;

	private int maxN = -1;
	private int minW = -1;
	private int maxW = -1;
	private int minR = -1;

	private boolean done = false;
	private boolean containsNodes = true;
	private boolean containsWays = true;
	private boolean containsRelations = true;
	private boolean foundAnyWay = false;
	private boolean foundLastNode = false;
	private boolean foundLastWay = false;

	private FileStructureAnalyzer(PbfFile pbfFile)
	{
		this.pbfFile = pbfFile;
	}

	private void logEntityBounds()
	{
		logger.debug(String.format("maxN: %d, minW: %d, maxW: %d, minR: %d",
				maxN, minW, maxW, minR));
	}

	private void execute() throws IOException
	{
		int numDataBlocks = pbfFile.getNumberOfDataBlocks();
		logger.debug("num blocks: " + numDataBlocks);

		// First look at the types of the first block. If that block already
		// contains relations, we're done immediately. Set up information about
		// nodes and possibly ways otherwise.
		checkFirst();

		if (done) {
			return;
		}

		// Then look at the types in the last block. If that block contains
		// nodes, we're also done immediately. Set up information about
		// relations and possibly ways otherwise.
		checkLast();

		if (done) {
			return;
		}

		// If both the first and last block contain ways, there may only be ways
		// in between
		if (minW == 0 && maxW == numDataBlocks - 1) {
			done = true;
			return;
		}

		// If we're here, we know that the file does not contain only nodes,
		// only ways or only relations. All three cases have been catched above.
		// Hence, the file structure may be either [nodes, ways, relations],
		// [nodes, ways], [nodes, relations] or [ways, relations]

		// Deal with the [ways, relations] case first. We can check for this
		// case because we will already have discovered if the first block does
		// not contain any nodes.

		if (!containsNodes) {
			logger.debug("no nodes. finding last way");
			logEntityBounds();
			findLastWay();
			done = true;
			return;
		}

		// Next, deal with the [nodes, ways] case. We can check for this
		// case because we will already have discovered if the last block does
		// not contain any relations.

		if (!containsRelations) {
			logger.debug("no relations. finding last node");
			logEntityBounds();
			findLastNode();
			done = true;
			return;
		}

		// Remaining possible file structures: [nodes, ways, relations],
		// or [nodes, relations].
		//
		// Hence we know that the first block contains nodes and the last block
		// contains relations and 'maxN' and 'minR' have been initialized.
		// No we will contract the interval between nodes and relations either
		// until we find a block that contains ways or until we discover
		// that there are no ways in the file.

		logger.debug("trying to find any way");
		while (containsWays && !foundAnyWay) {
			if (minR - maxN == 1) {
				// The search interval is empty
				containsWays = false;
				foundLastNode = true;
				foundLastWay = true;
				done = true;
				break;
			}
			contractNodeRelationInterval();
		}

		// The above loop will discover the [nodes, relations] case and will
		// already have set the variables accordingly. If that happens, we're
		// done.

		if (done) {
			return;
		}

		// When we're here, we're dealing with a file that contains all element
		// types, i.e. with a [nodes, ways, relations] structure.
		//

		logEntityBounds();

		logger.debug("file contains all element types");
		findLastNode();
		findLastWay();
	}

	private void findLastNode() throws IOException
	{
		logger.debug("finding last node");
		while (!foundLastNode) {
			if (minW - maxN == 1) {
				// No blocks in between nodes and ways, found boundary
				foundLastNode = true;
			}
			contractNodeWayInterval();
		}
	}

	private void findLastWay() throws IOException
	{
		logger.debug("finding last way");
		while (!foundLastWay) {
			if (minR - maxW == 1) {
				// No blocks in between ways and relations, found boundary
				foundLastWay = true;
				break;
			}
			contractWayRelationInterval();
		}
	}

	private void checkFirst() throws IOException
	{
		// Inspect first block
		Set<EntityType> types = getTypes(0);
		logger.debug("first: " + types);

		if (types.contains(EntityType.Relation)) {
			// relations in the first block, we're already done
			done = true;
			foundLastNode = true;
			foundLastWay = true;

			containsNodes = types.contains(EntityType.Node);
			containsWays = types.contains(EntityType.Way);
			minR = 0;
			if (containsNodes) {
				maxN = 0;
			}
			if (containsWays) {
				minW = 0;
				maxW = 0;
			}
		} else if (contains(types, EntityType.Node, EntityType.Way)) {
			// no relations, but nodes and ways
			foundAnyWay = true;
			foundLastNode = true;

			maxN = 0;
			minW = 0;
		} else if (types.contains(EntityType.Node)) {
			// only nodes
			maxN = 0;
		} else if (types.contains(EntityType.Way)) {
			// only ways
			containsNodes = false;
			foundLastNode = true;
			foundAnyWay = true;

			minW = 0;
			maxW = 0;
		}
	}

	private void checkLast() throws IOException
	{
		// Inspect last block
		int last = pbfFile.getNumberOfDataBlocks() - 1;
		Set<EntityType> types = getTypes(last);
		logger.debug("last: " + types);

		if (types.contains(EntityType.Node)) {
			// If the last block contains nodes, all block before may only
			// contain nodes.
			done = true;
			foundLastNode = true;
			foundLastWay = true;

			containsWays = contains(types, EntityType.Way);
			containsRelations = contains(types, EntityType.Relation);
			maxN = last;
			if (containsWays) {
				foundAnyWay = true;
				minW = last;
				maxW = last;
			}
			if (containsRelations) {
				minR = last;
			}
		} else if (types.contains(EntityType.Way)) {
			containsRelations = contains(types, EntityType.Relation);
			if (containsRelations) {
				minR = last;
			}
			if (!foundAnyWay) {
				foundAnyWay = true;
				minW = last;
			}
			foundLastWay = true;
			maxW = last;
		} else if (types.contains(EntityType.Relation)) {
			minR = last;
		}
	}

	// Helper method for finding any block with ways using binary search. Our
	// search interval is between the last node block and the first relation
	// block found so far. This method looks in the middle of the interval and
	// checks for ways.
	private void contractNodeRelationInterval() throws IOException
	{
		int l = maxN;
		int r = minR;
		int p = (l + r) / 2;
		logger.debug("[" + l + "," + r + "] -> " + p);
		Set<EntityType> types = getTypes(p);
		logger.debug("types: " + types);

		if (types.contains(EntityType.Way)) {
			// We found a way block
			minW = p;
			maxW = p;
			foundAnyWay = true;
			if (types.contains(EntityType.Node)) {
				// We found the first block with ways. It also contains nodes,
				// so we have found the last node block as well.
				maxN = p;
				foundLastNode = true;
			}
			if (types.contains(EntityType.Relation)) {
				// We found the last block with ways. It also contains
				// relations, so we have found the first relation block as well.
				minR = p;
				foundLastWay = true;
			}
		} else if (contains(types, EntityType.Node, EntityType.Relation)) {
			// We found a block with nodes and relations, we're done
			maxN = p;
			minR = p;
			containsWays = false;
			foundLastNode = true;
			foundLastWay = true;
			done = true;
		} else if (types.contains(EntityType.Node)) {
			// This block contains nodes only, adjust search boundary
			maxN = p;
		} else if (types.contains(EntityType.Relation)) {
			// This block contains relations only, adjust search boundary
			minR = p;
		}
	}

	private void contractNodeWayInterval() throws IOException
	{
		int l = maxN;
		int r = minW;
		int p = (l + r) / 2;
		logger.debug("[" + l + "," + r + "] -> " + p);
		Set<EntityType> types = getTypes(p);
		logger.debug("types: " + types);

		if (contains(types, EntityType.Node, EntityType.Way)) {
			// Block with both nodes and ways, boundary found
			maxN = p;
			minW = p;
			foundLastNode = true;
		} else if (types.contains(EntityType.Node)) {
			// Block with only nodes
			maxN = p;
		} else if (types.contains(EntityType.Way)) {
			// Block with only ways
			minW = p;
		}
	}

	private void contractWayRelationInterval() throws IOException
	{
		int l = maxW;
		int r = minR;
		int p = (l + r) / 2;
		logger.debug("[" + l + "," + r + "] -> " + p);
		Set<EntityType> types = getTypes(p);
		logger.debug("types: " + types);

		if (contains(types, EntityType.Way, EntityType.Relation)) {
			// Block with both ways and relations, boundary found
			maxW = p;
			minR = p;
			foundLastWay = true;
		} else if (types.contains(EntityType.Way)) {
			// Block with only ways
			maxW = p;
		} else if (types.contains(EntityType.Relation)) {
			// Block with only relations
			minR = p;
		}
	}

	private Set<EntityType> getTypes(int p) throws IOException
	{
		return PbfMeta.getContentTypes(pbfFile.getDataBlock(p));
	}

	private boolean contains(Set<EntityType> blockTypes, EntityType... types)
			throws IOException
	{
		boolean allFound = true;
		for (EntityType type : types) {
			allFound &= blockTypes.contains(type);
		}
		return allFound;
	}

}
