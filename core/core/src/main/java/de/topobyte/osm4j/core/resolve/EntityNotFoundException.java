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

package de.topobyte.osm4j.core.resolve;

/**
 * This exception is thrown by methods that might fail on retrieving some entity
 * while doing something.
 * 
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
public class EntityNotFoundException extends Exception
{

	private static final long serialVersionUID = 3215406014208968124L;

	public EntityNotFoundException(String message)
	{
		super(message);
	}

	public EntityNotFoundException(Throwable cause)
	{
		super(cause);
	}

	public EntityNotFoundException(String message, Throwable cause)
	{
		super(message, cause);
	}

}
