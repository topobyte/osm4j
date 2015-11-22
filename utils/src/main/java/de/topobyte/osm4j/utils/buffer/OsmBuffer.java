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

package de.topobyte.osm4j.utils.buffer;

import java.io.IOException;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.access.OsmOutputStream;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmBounds;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;

public class OsmBuffer implements OsmOutputStream, OsmIterator
{

	private int bufferSize;
	private int maxNumberOfBuffers;

	private int numberOfBuffers;

	private Deque<EntityBuffer> pool = new LinkedList<>();
	private Deque<EntityBuffer> buffers = new LinkedList<>();

	private EntityBuffer currentWriteBuffer;
	private EntityBuffer currentReadBuffer;

	private Object sync = new Object();
	private boolean done = false;

	private boolean valid = true;

	public OsmBuffer(int bufferSize, int maxNumberOfBuffers)
	{
		this.bufferSize = bufferSize;
		this.maxNumberOfBuffers = maxNumberOfBuffers;

		currentWriteBuffer = new EntityBuffer(bufferSize);
		currentReadBuffer = new EntityBuffer(bufferSize);

		numberOfBuffers = 2;
	}

	public void setInvalid()
	{
		valid = false;
	}

	private void write(EntityContainer c)
	{
		if (currentWriteBuffer.size() < bufferSize) {
			currentWriteBuffer.add(c);
		} else {
			if (enqueueCurrentWriteBuffer()) {
				currentWriteBuffer.add(c);
			}
		}
	}

	private boolean enqueueCurrentWriteBuffer()
	{
		synchronized (sync) {
			buffers.add(currentWriteBuffer);
			sync.notify();
			if (!pool.isEmpty()) {
				currentWriteBuffer = pool.removeFirst();
				return true;
			} else if (numberOfBuffers < maxNumberOfBuffers) {
				currentWriteBuffer = new EntityBuffer(bufferSize);
				numberOfBuffers++;
				return true;
			}
		}
		synchronized (sync) {
			while (valid) {
				if (!pool.isEmpty()) {
					currentWriteBuffer = pool.removeFirst();
					return true;
				}
				try {
					sync.wait();
				} catch (InterruptedException e) {
					// continue waiting for a buffer
				}
			}
		}
		return false;
	}

	@Override
	public void write(OsmBounds bounds) throws IOException
	{
		// ignore
	}

	@Override
	public void write(OsmNode node) throws IOException
	{
		EntityContainer c = new EntityContainer(EntityType.Node, node);
		write(c);
	}

	@Override
	public void write(OsmWay way) throws IOException
	{
		EntityContainer c = new EntityContainer(EntityType.Way, way);
		write(c);
	}

	@Override
	public void write(OsmRelation relation) throws IOException
	{
		EntityContainer c = new EntityContainer(EntityType.Relation, relation);
		write(c);
	}

	@Override
	public void complete() throws IOException
	{
		enqueueCurrentWriteBuffer();
		synchronized (sync) {
			done = true;
			sync.notify();
		}
	}

	@Override
	public Iterator<EntityContainer> iterator()
	{
		return this;
	}

	@Override
	public boolean hasNext()
	{
		if (!currentReadBuffer.isEmpty()) {
			return true;
		}
		synchronized (sync) {
			while (valid) {
				if (!buffers.isEmpty()) {
					return true;
				}
				if (done) {
					return false;
				}
				try {
					sync.wait();
				} catch (InterruptedException e) {
					//
				}
			}
		}
		return false;
	}

	@Override
	public EntityContainer next()
	{
		if (!currentReadBuffer.isEmpty()) {
			return currentReadBuffer.remove();
		}
		synchronized (sync) {
			while (valid) {
				if (!buffers.isEmpty()) {
					currentReadBuffer.clear();
					pool.add(currentReadBuffer);
					currentReadBuffer = buffers.remove();
					sync.notify();
					return currentReadBuffer.remove();
				} else if (done) {
					throw new NoSuchElementException();
				}
				try {
					sync.wait();
				} catch (InterruptedException e) {
					// continue
				}
			}
		}
		throw new NoSuchElementException();
	}

	@Override
	public void remove()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean hasBounds()
	{
		return false;
	}

	@Override
	public OsmBounds getBounds()
	{
		return null;
	}

}
