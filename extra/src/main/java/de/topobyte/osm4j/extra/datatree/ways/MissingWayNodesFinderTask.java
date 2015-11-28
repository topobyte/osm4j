package de.topobyte.osm4j.extra.datatree.ways;

import gnu.trove.iterator.TLongIterator;
import gnu.trove.iterator.TLongObjectIterator;
import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import de.topobyte.osm4j.core.access.OsmIdIterator;
import de.topobyte.osm4j.core.access.OsmIdIteratorInput;
import de.topobyte.osm4j.core.access.OsmIteratorInput;
import de.topobyte.osm4j.core.dataset.InMemoryMapDataSet;
import de.topobyte.osm4j.core.dataset.MapDataSetLoader;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.IdContainer;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.extra.idlist.IdListOutputStream;
import de.topobyte.osm4j.extra.threading.Task;
import de.topobyte.osm4j.utils.OsmFile;
import de.topobyte.osm4j.utils.OsmFileInput;
import de.topobyte.osm4j.utils.StreamUtil;

public class MissingWayNodesFinderTask implements Task
{

	private long counter = 0;
	private long found = 0;
	private long notFound = 0;

	private OsmFile fileNodes;
	private OsmFile fileWays;
	private File fileOutput;
	private boolean verbose;

	public MissingWayNodesFinderTask(OsmFile inputNodes, OsmFile inputWays,
			File fileOutput, boolean verbose)
	{
		this.fileNodes = inputNodes;
		this.fileWays = inputWays;
		this.fileOutput = fileOutput;
		this.verbose = verbose;
	}

	public long getCounter()
	{
		return counter;
	}

	public long getFound()
	{
		return found;
	}

	public long getNotFound()
	{
		return notFound;
	}

	@Override
	public void execute() throws IOException
	{
		if (verbose) {
			long nodesSize = fileNodes.getPath().toFile().length();
			System.out.println(String.format(
					"Loading nodes file of size: %.3fMB",
					nodesSize / 1024. / 1024.));
		}

		OsmIdIteratorInput nodeInput = new OsmFileInput(fileNodes)
				.createIdIterator();
		TLongSet nodeIds = read(nodeInput.getIterator());

		if (verbose) {
			long waysSize = fileWays.getPath().toFile().length();
			System.out.println(String.format(
					"Loading ways file of size: %.3fMB",
					waysSize / 1024. / 1024.));
		}

		OsmIteratorInput wayInput = new OsmFileInput(fileWays).createIterator(
				false, false);
		InMemoryMapDataSet dataWays = MapDataSetLoader.read(wayInput, true,
				true, true);

		nodeInput.close();
		wayInput.close();

		if (verbose) {
			System.out.println("Number of ways: " + dataWays.getWays().size());
		}

		TLongSet missingIds = new TLongHashSet();

		TLongObjectIterator<OsmWay> ways = dataWays.getWays().iterator();
		while (ways.hasNext()) {
			ways.advance();
			OsmWay way = ways.value();
			build(way, nodeIds, missingIds);
		}

		if (verbose) {
			System.out.println("Sorting id list of size: " + missingIds.size());
		}

		TLongList missingIdList = new TLongArrayList(missingIds);
		missingIdList.sort();

		if (verbose) {
			System.out.println("Writing missing ids");
		}
		OutputStream bos = StreamUtil.bufferedOutputStream(fileOutput);
		IdListOutputStream idOutput = new IdListOutputStream(bos);
		TLongIterator iterator = missingIdList.iterator();
		while (iterator.hasNext()) {
			idOutput.write(iterator.next());
		}
		idOutput.close();
	}

	private TLongSet read(OsmIdIterator idIterator)
	{
		TLongSet ids = new TLongHashSet();
		while (idIterator.hasNext()) {
			IdContainer container = idIterator.next();
			if (container.getType() != EntityType.Node) {
				break;
			}
			ids.add(container.getId());
		}
		return ids;
	}

	private void build(OsmWay way, TLongSet nodeIds, TLongSet missing)
			throws IOException
	{
		for (int i = 0; i < way.getNumberOfNodes(); i++) {
			long nodeId = way.getNodeId(i);
			if (nodeIds.contains(nodeId)) {
				found++;
			} else {
				notFound++;
				missing.add(nodeId);
			}
		}
		counter++;
	}

}
