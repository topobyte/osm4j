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

package de.topobyte.osm4j.extra.nodearray;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

import de.topobyte.osm4j.extra.nodearray.coding.ErrorDeltas;

public class Factories
{

	public static Factory DOUBLE = new Factory() {

		@Override
		public NodeArrayWriter createWriter(DataOutputStream out)
		{
			return new NodeArrayWriterDouble(out);
		}

		@Override
		public NodeArray createNodeArray(File file) throws IOException
		{
			return new NodeArrayDouble(file);
		}

		@Override
		public double getErrorDeltaLon()
		{
			return 0;
		}

		@Override
		public double getErrorDeltaLat()
		{
			return 0;
		}

		@Override
		public double getExpectedValue(double value)
		{
			return value;
		}

	};

	public static Factory FLOAT = new Factory() {

		@Override
		public NodeArrayWriter createWriter(DataOutputStream out)
		{
			return new NodeArrayWriterFloat(out);
		}

		@Override
		public NodeArray createNodeArray(File file) throws IOException
		{
			return new NodeArrayFloat(file);
		}

		@Override
		public double getErrorDeltaLon()
		{
			return 0;
		}

		@Override
		public double getErrorDeltaLat()
		{
			return 0;
		}

		@Override
		public double getExpectedValue(double value)
		{
			return (float) value;
		}

	};

	public static Factory INTEGER = new Factory() {

		@Override
		public NodeArrayWriter createWriter(DataOutputStream out)
		{
			return new NodeArrayWriterInteger(out);
		}

		@Override
		public NodeArray createNodeArray(File file) throws IOException
		{
			return new NodeArrayInteger(file);
		}

		@Override
		public double getErrorDeltaLon()
		{
			return ErrorDeltas.DELTA_INT_LON;
		}

		@Override
		public double getErrorDeltaLat()
		{
			return ErrorDeltas.DELTA_INT_LAT;
		}

		@Override
		public double getExpectedValue(double value)
		{
			return value;
		}

	};

	public static Factory SHORT = new Factory() {

		@Override
		public NodeArrayWriter createWriter(DataOutputStream out)
		{
			return new NodeArrayWriterShort(out);
		}

		@Override
		public NodeArray createNodeArray(File file) throws IOException
		{
			return new NodeArrayShort(file);
		}

		@Override
		public double getErrorDeltaLon()
		{
			return ErrorDeltas.DELTA_SHORT_LON;
		}

		@Override
		public double getErrorDeltaLat()
		{
			return ErrorDeltas.DELTA_SHORT_LAT;
		}

		@Override
		public double getExpectedValue(double value)
		{
			return value;
		}

	};

}
