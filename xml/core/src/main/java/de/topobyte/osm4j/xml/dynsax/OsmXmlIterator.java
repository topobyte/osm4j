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

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import de.topobyte.osm4j.core.access.OsmHandler;
import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmRelation;
import de.topobyte.osm4j.core.model.iface.OsmWay;

/**
 * This class allows iteration over OSM XML data.
 * 
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
public class OsmXmlIterator implements OsmIterator, OsmHandler
{

	private static int LIMIT = 128;
	private Object mutex = new Object();
	private State state = State.PUSH;
	private List<EntityContainer> list = new ArrayList<EntityContainer>();
	private Exception exception = null;

	public OsmXmlIterator(InputStream inputStream, boolean parseMetadata)
			throws ParserConfigurationException, SAXException
	{
		init(inputStream, parseMetadata);
	}

	public OsmXmlIterator(File file, boolean parseMetadata)
			throws ParserConfigurationException, SAXException,
			FileNotFoundException
	{
		FileInputStream fis = new FileInputStream(file);
		BufferedInputStream bis = new BufferedInputStream(fis);
		init(bis, parseMetadata);
	}

	public OsmXmlIterator(String fileName, boolean parseMetadata)
			throws ParserConfigurationException, SAXException,
			FileNotFoundException
	{
		this(new File(fileName), parseMetadata);
	}

	private void init(final InputStream inputStream, final boolean parseMetadata)
			throws ParserConfigurationException, SAXException
	{
		SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
		final SAXParser parser = saxParserFactory.newSAXParser();

		Thread thread = new Thread(new Runnable() {

			@Override
			public void run()
			{
				OsmSaxHandler saxHandler = OsmSaxHandler.createInstance(
						OsmXmlIterator.this, parseMetadata);

				try {
					parser.parse(inputStream, saxHandler);
				} catch (SAXException | IOException e) {
					state = State.EXCEPTION;
					OsmXmlIterator.this.exception = e;
				} finally {
					complete();
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
	public void complete()
	{
		synchronized (mutex) {
			state = State.END;
			mutex.notify();
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
		while (true) {
			synchronized (mutex) {
				if (list.size() > 0)
					return true;
				if (state == State.END && list.size() == 0)
					return false;
				if (state == State.EXCEPTION) {
					throw new RuntimeException("error while processing input",
							exception);
				}
			}
		}
	}

	@Override
	public EntityContainer next()
	{
		EntityContainer next;
		while (true) {
			synchronized (mutex) {
				if (state == State.READ
						|| (state == State.END && list.size() > 0)) {
					next = list.remove(0);
					if (list.size() == 0) {
						if (state != State.END)
							state = State.PUSH;
						mutex.notify();
					}
					return next;
				}
				if (state == State.EXCEPTION) {
					throw new RuntimeException("error while processing input",
							exception);
				}
				try {
					mutex.wait();
				} catch (InterruptedException e) {
					// interrupted
				}
				continue;
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
		PUSH,
		READ,
		END,
		EXCEPTION
	}

	@Override
	public void handle(OsmNode node)
	{
		synchronized (mutex) {
			while (true) {
				if (state == State.PUSH) {
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
					list.add(new EntityContainer(EntityType.Relation, relation));
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

}
