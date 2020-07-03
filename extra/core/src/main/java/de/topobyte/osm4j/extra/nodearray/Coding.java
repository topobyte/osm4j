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

public class Coding
{

	// We're mapping double values to integral data ranges by scaling the
	// possible values [-180..180] and [-90..90] to [0..1] and then fanning out
	// from [0..1] to the value range of the integral data type.

	// We don't use the whole value range for encoding, instead we use a
	// slightly smaller range to have some code points left that will not be
	// used by ordinary data and which can thus be used to encode special
	// situations, such as NULL or OUT OF RANGE.

	// When encoding as ints, we're mapping to the values in the range
	// [INT_MIN, INT_MIN + 1, INT_MIN + 2, ..., INT_MIN + INT_RANGE],
	// where INT_MIN + INT_RANGE = INT_MAX - 3

	// When encoding as shorts, we're mapping to the values in the range
	// [SHORT_MIN, SHORT_MIN + 1, SHORT_MIN + 2, ..., SHORT_MIN + SHORT_RANGE],
	// where SHORT_MIN + SHORT_RANGE = SHORT_MAX - 3

	// we need the int bounds as longs for calculations
	private static long INT_MIN = Integer.MIN_VALUE;
	private static long INT_MAX = Integer.MAX_VALUE;
	// and the short bounds as ints, also for calculations
	private static int SHORT_MIN = Short.MIN_VALUE;
	private static int SHORT_MAX = Short.MAX_VALUE;

	// Reserve three special values, hence the -3
	// One is for NULL entries, the other two could be used for invalid values
	// (e.g. out of valid bounds)
	private static long INT_RANGE = INT_MAX - INT_MIN - 3;
	private static int SHORT_RANGE = SHORT_MAX - SHORT_MIN - 3;

	// The ranges as double for calculations without cluttering casts
	private static double INT_RANGE_D = INT_RANGE;
	private static double SHORT_RANGE_D = SHORT_RANGE;

	// Special values used to encode NULL entries
	public static int INT_NULL = Integer.MAX_VALUE;
	public static short SHORT_NULL = Short.MAX_VALUE;

	/*
	 * Here are the actual encoding and decoding operations
	 */

	public static int encodeLonAsInt(double lon)
	{
		// lon = -180 .. 180
		double a = (lon + 180) / 360; // 0 .. 1
		return (int) (INT_MIN + Math.round(a * INT_RANGE));
	}

	public static int encodeLatAsInt(double lat)
	{
		// lat -90 to 90
		double a = (lat + 90) / 180; // 0 .. 1
		return (int) (INT_MIN + Math.round(a * INT_RANGE));
	}

	public static short encodeLonAsShort(double lon)
	{
		// lon = -180 .. 180
		double a = (lon + 180) / 360; // 0 .. 1
		return (short) (SHORT_MIN + Math.round(a * SHORT_RANGE));
	}

	public static short encodeLatAsShort(double lat)
	{
		// lat -90 to 90
		double a = (lat + 90) / 180; // 0 .. 1
		return (short) (SHORT_MIN + Math.round(a * SHORT_RANGE));
	}

	public static double decodeLonFromInt(int value)
	{
		double a = (value - INT_MIN) / INT_RANGE_D; // 0 .. 1
		return a * 360 - 180;
	}

	public static double decodeLatFromInt(int value)
	{
		double a = (value - INT_MIN) / INT_RANGE_D; // 0 .. 1
		return a * 180 - 90;
	}

	public static double decodeLonFromShort(short value)
	{
		double a = (value - SHORT_MIN) / SHORT_RANGE_D; // 0 .. 1
		return a * 360 - 180;
	}

	public static double decodeLatFromShort(short value)
	{
		double a = (value - SHORT_MIN) / SHORT_RANGE_D; // 0 .. 1
		return a * 180 - 90;
	}

}
