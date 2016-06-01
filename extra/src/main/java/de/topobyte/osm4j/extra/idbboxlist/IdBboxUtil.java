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

package de.topobyte.osm4j.extra.idbboxlist;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

import de.topobyte.melon.io.StreamUtil;
import de.topobyte.osm4j.extra.datatree.BoxUtil;

public class IdBboxUtil
{

	public static List<IdBboxEntry> read(InputStream input) throws IOException
	{
		IdBboxListInputStream bboxes = new IdBboxListInputStream(input);
		List<IdBboxEntry> entries = new ArrayList<>();
		while (true) {
			try {
				entries.add(bboxes.next());
			} catch (EOFException e) {
				break;
			}
		}
		return entries;
	}

	public static List<IdBboxEntry> read(Path path) throws IOException
	{
		InputStream input = StreamUtil.bufferedInputStream(path);
		List<IdBboxEntry> entries = read(input);
		input.close();
		return entries;
	}

	public static List<IdBboxEntry> read(File file) throws IOException
	{
		InputStream input = StreamUtil.bufferedInputStream(file);
		List<IdBboxEntry> entries = read(input);
		input.close();
		return entries;
	}

	public static List<Geometry> readBoxes(InputStream input)
			throws IOException
	{
		GeometryFactory factory = new GeometryFactory();
		List<Geometry> boxList = new ArrayList<>();

		IdBboxListInputStream bboxes = new IdBboxListInputStream(input);
		while (true) {
			try {
				IdBboxEntry entry = bboxes.next();
				Envelope e = entry.getEnvelope().intersection(
						BoxUtil.WORLD_BOUNDS);
				boxList.add(factory.toGeometry(e));
			} catch (EOFException e) {
				break;
			}
		}
		return boxList;
	}

	public static List<Geometry> readBoxes(Path path) throws IOException
	{
		InputStream input = StreamUtil.bufferedInputStream(path);
		List<Geometry> entries = readBoxes(input);
		input.close();
		return entries;
	}

	public static List<Geometry> readBoxes(File file) throws IOException
	{
		InputStream input = StreamUtil.bufferedInputStream(file);
		List<Geometry> entries = readBoxes(input);
		input.close();
		return entries;
	}

}
