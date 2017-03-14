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

package de.topobyte.osm4j.extra.threading.write;

import java.io.IOException;

import de.topobyte.osm4j.extra.threading.ObjectBuffer;
import de.topobyte.osm4j.utils.buffer.StoppableRunnable;

public class WriterRunner implements StoppableRunnable
{

	private boolean stopped = false;
	private ObjectBuffer<? extends WriteRequest> objectBuffer;

	public WriterRunner(ObjectBuffer<? extends WriteRequest> objectBuffer)
	{
		this.objectBuffer = objectBuffer;
	}

	@Override
	public void run()
	{
		while (!stopped && objectBuffer.hasNext()) {
			WriteRequest request = objectBuffer.next();
			try {
				request.perform();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public void stop()
	{
		stopped = true;
	}

}
