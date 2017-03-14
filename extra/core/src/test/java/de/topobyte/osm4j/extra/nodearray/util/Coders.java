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

package de.topobyte.osm4j.extra.nodearray.util;

import de.topobyte.osm4j.extra.nodearray.Coding;

public class Coders
{

	public static Coder<Integer> CODER_INT_LON = new Coder<Integer>() {

		@Override
		public Integer encode(double value)
		{
			return Coding.encodeLonAsInt(value);
		}

		@Override
		public double decode(Integer value)
		{
			return Coding.decodeLonFromInt(value);
		}

		@Override
		public Interval interval()
		{
			return Intervals.LONGITUDE;
		}
	};

	public static Coder<Integer> CODER_INT_LAT = new Coder<Integer>() {

		@Override
		public Integer encode(double value)
		{
			return Coding.encodeLatAsInt(value);
		}

		@Override
		public double decode(Integer value)
		{
			return Coding.decodeLatFromInt(value);
		}

		@Override
		public Interval interval()
		{
			return Intervals.LATITUDE;
		}
	};

	public static Coder<Short> CODER_SHORT_LON = new Coder<Short>() {

		@Override
		public Short encode(double value)
		{
			return Coding.encodeLonAsShort(value);
		}

		@Override
		public double decode(Short value)
		{
			return Coding.decodeLonFromShort(value);
		}

		@Override
		public Interval interval()
		{
			return Intervals.LONGITUDE;
		}
	};

	public static Coder<Short> CODER_SHORT_LAT = new Coder<Short>() {

		@Override
		public Short encode(double value)
		{
			return Coding.encodeLatAsShort(value);
		}

		@Override
		public double decode(Short value)
		{
			return Coding.decodeLatFromShort(value);
		}

		@Override
		public Interval interval()
		{
			return Intervals.LATITUDE;
		}
	};

}
