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

package de.topobyte.osm4j.core.access;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProgressMonitor
{

	final static Logger logger = LoggerFactory.getLogger(ProgressMonitor.class);

	private String title;

	private int nodeCount = 0;
	private int wayCount = 0;
	private int relationCount = 0;

	public ProgressMonitor(String title)
	{
		this.title = title;
	}

	public void nodeProcessed()
	{
		nodeCount++;
		if (nodeCount % 10000 == 0) {
			logger.info(title + " done n: " + nodeCount);
		}
	}

	public void wayProcessed()
	{
		wayCount++;
		if (wayCount % 10000 == 0) {
			logger.info(title + " done w: " + wayCount);
		}
	}

	public void relationProcessed()
	{
		relationCount++;
		if (relationCount % 10000 == 0) {
			logger.info(title + " done r: " + relationCount);
		}
	}

}
