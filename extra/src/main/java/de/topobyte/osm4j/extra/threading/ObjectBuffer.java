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

package de.topobyte.osm4j.extra.threading;

import java.io.IOException;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

public class ObjectBuffer<T> implements Iterable<T>, Iterator<T>
{

	private int bufferSize;
	private int maxNumberOfBuffers;

	private int numberOfBuffers;

	private Deque<IternalObjectBuffer<T>> pool = new LinkedList<>();
	private Deque<IternalObjectBuffer<T>> buffers = new LinkedList<>();

	private IternalObjectBuffer<T> currentWriteBuffer;
	private IternalObjectBuffer<T> currentReadBuffer;

	private Object sync = new Object();
	private boolean done = false;

	private boolean valid = true;

	public ObjectBuffer(int bufferSize, int maxNumberOfBuffers)
	{
		this.bufferSize = bufferSize;
		this.maxNumberOfBuffers = maxNumberOfBuffers;

		currentWriteBuffer = new IternalObjectBuffer<>(bufferSize);
		currentReadBuffer = new IternalObjectBuffer<>(bufferSize);

		numberOfBuffers = 2;
	}

	public int getSize()
	{
		return buffers.size();
	}

	public void setInvalid()
	{
		valid = false;
	}

	public void write(T c)
	{
		if (currentWriteBuffer.size() < bufferSize) {
			currentWriteBuffer.add(c);
		} else {
			if (enqueueCurrentWriteBuffer()) {
				currentWriteBuffer.add(c);
			}
		}
	}

	public void close() throws IOException
	{
		enqueueCurrentWriteBuffer();
		synchronized (sync) {
			done = true;
			sync.notify();
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
				currentWriteBuffer = new IternalObjectBuffer<>(bufferSize);
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
	public Iterator<T> iterator()
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
	public T next()
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

}
