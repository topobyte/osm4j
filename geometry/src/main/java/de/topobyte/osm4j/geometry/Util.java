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
