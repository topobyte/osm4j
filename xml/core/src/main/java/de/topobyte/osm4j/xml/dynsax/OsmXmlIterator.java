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

package de.topobyte.osm4j.xml.dynsax;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import de.topobyte.osm4j.core.access.OsmHandler;
import de.topobyte.osm4j.core.access.OsmInputException;
import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.access.OsmReader;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmBounds;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;

/**
 * This class allows iteration over OSM XML data. It wraps an OsmXmlReader in a
 * separate thread and reads data in batches into an internal buffer. When
 * either the buffer is full or the end of the element stream has been reached,
 * the iterator methods <code>hasNext()</code> and <code>next()</code> return
 * data. When the buffer is not full, those methods will use a monitor to wait
 * for more data.
 * 
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
public class OsmXmlIterator implements OsmIterator, OsmHandler
{

	private static int LIMIT = 128;
	private Object mutex = new Object();
	private State state = State.PUSH;
	private List<EntityContainer> list = new ArrayList<>();
	private Exception exception = null;
	private OsmBounds bounds = null;
	private boolean beyondBounds = false;

	public OsmXmlIterator(InputStream inputStream, boolean parseMetadata)
	{
		init(inputStream, parseMetadata);
	}

	public OsmXmlIterator(File file, boolean parseMetadata)
			throws FileNotFoundException
	{
		InputStream fis = new FileInputStream(file);
		InputStream bis = new BufferedInputStream(fis);
		init(bis, parseMetadata);
	}

	public OsmXmlIterator(String pathname, boolean parseMetadata)
			throws FileNotFoundException
	{
		this(new File(pathname), parseMetadata);
	}

	// Run OsmXmlReader in separate thread
	private void init(final InputStream inputStream,
			final boolean parseMetadata)
	{
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run()
			{
				OsmReader reader = new OsmXmlReader(inputStream, parseMetadata);
				reader.setHandler(OsmXmlIterator.this);

				try {
					reader.read();
				} catch (OsmInputException e) {
					synchronized (mutex) {
						beyondBounds = true;
						state = State.EXCEPTION;
						OsmXmlIterator.this.exception = e;
						mutex.notify();
					}
				} finally {
					try {
						inputStream.close();
					} catch (IOException e) {
						// ignore
					}
				}
			}
		});
		thread.start();
	}

	@Override
	public Iterator<EntityContainer> iterator()
	{
		return this;
	}

	@Override
	public boolean hasNext()
	{
		while (true) {
			synchronized (mutex) {
				switch (state) {
				case READ:
					// When in this state, there must be elements left.
					return true;
				case END:
					// When the end has been reached, deliver buffered elements.
					return !list.isEmpty();
				case EXCEPTION:
					// An exception occurred. Deliver buffered elements and
					// raise an exception after the last valid element.
					if (!list.isEmpty()) {
						return true;
					}
					throw new RuntimeException("error while processing input",
							exception);
				case PUSH:
					try {
						mutex.wait();
					} catch (InterruptedException e) {
						// interrupted
					}
					break;
				}
			}
		}
	}

	@Override
	public EntityContainer next()
	{
		while (true) {
			synchronized (mutex) {
				// Return an element if we're in one of the states where it is
				// allowed to read and if the buffer contains elements.
				switch (state) {
				case READ:
				case END:
				case EXCEPTION:
					if (!list.isEmpty()) {
						EntityContainer next = list.remove(0);
						if (list.isEmpty() && state == State.READ) {
							// Just removed the last element from the buffer and
							// we've been in the READ state; move to PUSH state.
							state = State.PUSH;
							mutex.notify();
						}
						return next;
					}
					break;
				case PUSH:
					break;
				}

				// At this point, we're either in PUSH state or the buffer is
				// empty.

				if (state == State.EXCEPTION) {
					// No elements in the buffer, exception occurred.
					throw new RuntimeException("error while processing input",
							exception);
				}
				if (state == State.END) {
					// No elements in the buffer and parsing is finished.
					throw new NoSuchElementException(
							"End of stream has been reached");
				}

				// Either we're in READ and no elements in the buffer, or we're
				// in PUSH state. Wait until the parser notifies us.
				try {
					mutex.wait();
				} catch (InterruptedException e) {
					// interrupted
				}
			}
		}
	}

	@Override
	public void remove()
	{
		throw new UnsupportedOperationException(
				"an iterator over osm files is read-only");
	}

	private enum State {
		// This state indicates that the parser thread should fill the element
		// buffer. No reading of data is possible in this state. The parser
		// thread will switch to READ state once the buffer is full or to END
		// when the end of the stream has been reached.
		PUSH,
		// This state indicates that the reader may access buffered data. The
		// reader will switch back to PUSH once all elements in the buffer have
		// been consumed.
		READ,
		// Reading is also possible, however don't move to PUSH state once the
		// buffer is empty.
		END,
		// An exception occurred during parsing. Allow reading all previously
		// buffered elements, but raise an exception when trying to access
		// subsequent elements.
		EXCEPTION
	}

	@Override
	public void complete()
	{
		synchronized (mutex) {
			beyondBounds = true;
			state = State.END;
			mutex.notify();
		}
	}

	@Override
	public void handle(OsmBounds bounds) throws IOException
	{
		synchronized (mutex) {
			if (!beyondBounds) {
				beyondBounds = true;
				this.bounds = bounds;
			}
		}
	}

	@Override
	public void handle(OsmNode node)
	{
		synchronized (mutex) {
			while (true) {
				if (state == State.PUSH) {
					beyondBounds = true;
					list.add(new EntityContainer(EntityType.Node, node));
					if (list.size() == LIMIT) {
						state = State.READ;
						mutex.notify();
					}
					return;
				}
				try {
					mutex.wait();
				} catch (InterruptedException e) {
					// interrupted
				}
			}
		}
	}

	@Override
	public void handle(OsmWay way)
	{
		synchronized (mutex) {
			while (true) {
				if (state == State.PUSH) {
					beyondBounds = true;
					list.add(new EntityContainer(EntityType.Way, way));
					if (list.size() == LIMIT) {
						state = State.READ;
						mutex.notify();
					}
					return;
				}
				try {
					mutex.wait();
				} catch (InterruptedException e) {
					// interrupted
				}
			}
		}
	}

	@Override
	public void handle(OsmRelation relation)
	{
		synchronized (mutex) {
			while (true) {
				if (state == State.PUSH) {
					beyondBounds = true;
					list.add(
							new EntityContainer(EntityType.Relation, relation));
					if (list.size() == LIMIT) {
						state = State.READ;
						mutex.notify();
					}
					return;
				}
				try {
					mutex.wait();
				} catch (InterruptedException e) {
					// interrupted
				}
			}
		}
	}

	@Override
	public boolean hasBounds()
	{
		synchronized (mutex) {
			while (true) {
				if (beyondBounds) {
					return bounds != null;
				}
				try {
					mutex.wait();
				} catch (InterruptedException e) {
					// interrupted
				}
			}
		}
	}

	@Override
	public OsmBounds getBounds()
	{
		synchronized (mutex) {
			while (true) {
				if (beyondBounds) {
					return bounds;
				}
				try {
					mutex.wait();
				} catch (InterruptedException e) {
					// interrupted
				}
			}
		}
	}

}
