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

package de.topobyte.osm4j.extra.nodearray.coding;

public class PrintSomeCodings
{

	public static void main(String[] args)
	{
		System.out.println(Long.MAX_VALUE);
		System.out.println(Long.MIN_VALUE);
		System.out.println(Double.longBitsToDouble(Long.MAX_VALUE));
		System.out.println(Double.longBitsToDouble(Long.MIN_VALUE));
		for (int i = 1; i < 10; i++) {
			System.out.println(Double.longBitsToDouble(Long.MAX_VALUE - i));
			System.out.println(Double.longBitsToDouble(Long.MIN_VALUE + i));
		}

		System.out.println(Integer.MAX_VALUE);
		System.out.println(Integer.MIN_VALUE);
		System.out.println(Float.intBitsToFloat(Integer.MAX_VALUE));
		System.out.println(Float.intBitsToFloat(Integer.MIN_VALUE));
		for (int i = 1; i < 10; i++) {
			System.out.println(Float.intBitsToFloat(Integer.MAX_VALUE - i));
			System.out.println(Float.intBitsToFloat(Integer.MIN_VALUE + i));
		}

		System.out.println(Long.toHexString(Long.MAX_VALUE));
		System.out.println(Long.toHexString(Long.MIN_VALUE));

		System.out.println(Integer.toHexString(Integer.MAX_VALUE));
		System.out.println(Integer.toHexString(Integer.MIN_VALUE));

		System.out.println(Double.doubleToLongBits(Double.NaN));
		System.out.println(Double.doubleToLongBits(Double.NEGATIVE_INFINITY));
		System.out.println(Double.doubleToLongBits(Double.POSITIVE_INFINITY));
		System.out.println(Double.doubleToLongBits(Double.MAX_VALUE));
		System.out.println(Double.doubleToLongBits(Double.MIN_VALUE));
	}

}
