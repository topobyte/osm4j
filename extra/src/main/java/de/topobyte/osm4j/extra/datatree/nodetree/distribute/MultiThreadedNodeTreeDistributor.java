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

package de.topobyte.osm4j.extra.datatree.nodetree.distribute;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.access.OsmStreamOutput;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.extra.datatree.DataTree;
import de.topobyte.osm4j.extra.datatree.Node;
import de.topobyte.osm4j.extra.datatree.output.DataTreeOutputFactory;
import de.topobyte.osm4j.extra.threading.ObjectBuffer;
import de.topobyte.osm4j.extra.threading.StatusRunnable;
import de.topobyte.osm4j.utils.buffer.OsmBuffer;
import de.topobyte.osm4j.utils.buffer.ParallelExecutor;

public class MultiThreadedNodeTreeDistributor extends
		AbstractNodeTreeDistributor
{

	private int numOutputThreads;
	private List<DataTreeOutputFactory> outputFactories;

	public MultiThreadedNodeTreeDistributor(DataTree tree, Node head,
			DataTreeOutputFactory outputFactory, OsmIterator iterator,
			int numOutputThreads)
	{
		super(tree, head, iterator);
		this.numOutputThreads = numOutputThreads;

		outputFactories = new ArrayList<>();
		for (int i = 0; i < numOutputThreads; i++) {
			outputFactories.add(outputFactory);
		}
	}

	public MultiThreadedNodeTreeDistributor(DataTree tree, Node head,
			List<DataTreeOutputFactory> outputFactories, OsmIterator iterator)
	{
		super(tree, head, iterator);
		this.outputFactories = outputFactories;
		this.numOutputThreads = outputFactories.size();
	}

	@Override
	protected void initOutputs() throws IOException
	{
		List<Node> leafs = tree.getLeafs();
		for (Node leaf : leafs) {
			DataTreeOutputFactory outputFactory = outputFactories
					.get(bucket(leaf));
			outputFactory.init(leaf, true);
		}
	}

	private OsmBuffer buffer;
	private List<ObjectBuffer<WriteRequest>> obuffers;

	private void initBuffers()
	{
		buffer = new OsmBuffer(10000, 100);
		obuffers = new ArrayList<>();
		for (int i = 0; i < numOutputThreads; i++) {
			obuffers.add(new ObjectBuffer<WriteRequest>(10000, 100));
		}
	}

	@Override
	protected void distributeNodes() throws IOException
	{
		initBuffers();

		run();
	}

	private void printStatus()
	{
		StringBuilder b = new StringBuilder();
		b.append("buffer status: ");
		b.append(buffer.getSize());
		for (ObjectBuffer<WriteRequest> obuffer : obuffers) {
			b.append(", ");
			b.append(obuffer.getSize());
		}
		System.out.println(b.toString());
	}

	private void run() throws IOException
	{
		StatusRunnable status = new StatusRunnable(1000) {

			@Override
			protected void printStatus()
			{
				MultiThreadedNodeTreeDistributor.this.printStatus();
			}
		};

		new Thread(status).start();

		List<Runnable> tasks = new ArrayList<>();

		Runnable distributor = new NodeIteratorRunnable(iterator) {

			@Override
			protected void handle(OsmNode node) throws IOException
			{
				MultiThreadedNodeTreeDistributor.this.handle(node);
			}

			@Override
			protected void finished() throws IOException
			{
				for (ObjectBuffer<WriteRequest> buffer : obuffers) {
					buffer.close();
				}
			}
		};

		tasks.add(distributor);

		for (int i = 0; i < numOutputThreads; i++) {
			WriterRunner writer = new WriterRunner(obuffers.get(i));
			tasks.add(writer);
		}

		ParallelExecutor executor = new ParallelExecutor(tasks);
		executor.execute();

		status.stop();
	}

	private void handle(OsmNode node) throws IOException
	{
		List<Node> leafs = tree.query(head, node.getLongitude(),
				node.getLatitude());
		for (Node leaf : leafs) {
			int bucket = bucket(leaf);
			ObjectBuffer<WriteRequest> buffer = obuffers.get(bucket);

			OsmStreamOutput output = outputs.get(leaf);
			buffer.write(new WriteRequest(node, output));
		}
	}

	private int bucket(Node leaf)
	{
		long path = leaf.getPath();
		return (int) (path % numOutputThreads);
	}

}
