// Copyright 2020 Sebastian Kuerten
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

package de.topobyte.osm4j.geometry;

import de.topobyte.osm4j.core.resolve.EntityNotFoundStrategy;

class Util
{

	public static EntityNotFoundStrategy strategy(MissingEntitiesStrategy mes,
			boolean log, LogLevel logLevel)
	{
		switch (mes) {
		default:
		case THROW_EXCEPTION:
			return EntityNotFoundStrategy.THROW;
		case BUILD_EMPTY:
			return EntityNotFoundStrategy.THROW;
		case BUILD_PARTIAL:
			if (log) {
				switch (logLevel) {
				default:
				case INFO:
					return EntityNotFoundStrategy.LOG_INFO;
				case DEBUG:
					return EntityNotFoundStrategy.LOG_DEBUG;
				case WARN:
					return EntityNotFoundStrategy.LOG_WARN;
				}
			} else {
				return EntityNotFoundStrategy.IGNORE;
			}
		}
	}

}
