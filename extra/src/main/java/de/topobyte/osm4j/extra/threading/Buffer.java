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

public class Buffer<T> implements Iterable<T>, Iterator<T>
{

	private int numberOfObjects = 0;
	private int maxNumberOfObjects;

	private Deque<T> buffer = new LinkedList<>();

	private Object sync = new Object();
	private boolean done = false;

	private boolean valid = true;

	public Buffer(int maxNumberOfObjects)
	{
		this.maxNumberOfObjects = maxNumberOfObjects;
	}

	public void returnObject(T object)
	{
		synchronized (sync) {
			numberOfObjects--;
			sync.notify();
		}
	}

	public int getSize()
	{
		return buffer.size();
	}

	public void setInvalid()
	{
		valid = false;
	}

	public void complete() throws IOException
	{
		synchronized (sync) {
			done = true;
			sync.notify();
		}
	}

	public void write(T object)
	{
		synchronized (sync) {
			while (valid) {
				if (numberOfObjects < maxNumberOfObjects) {
					buffer.add(object);
					numberOfObjects++;
					sync.notify();
					return;
				} else {
					try {
						sync.wait();
					} catch (InterruptedException e) {
						// continue
					}
				}
			}
		}
	}

	@Override
	public Iterator<T> iterator()
	{
		return this;
	}

	@Override
	public boolean hasNext()
	{
		synchronized (sync) {
			while (valid) {
				if (!buffer.isEmpty()) {
					return true;
				}
				if (done) {
					return false;
				}
				try {
					sync.wait();
				} catch (InterruptedException e) {
					// continue
				}
			}
		}
		return false;
	}

	@Override
	public T next()
	{
		synchronized (sync) {
			while (valid) {
				if (!buffer.isEmpty()) {
					T object = buffer.remove();
					sync.notify();
					return object;
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
