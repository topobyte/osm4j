package de.topobyte.osm4j.extra.idlist;

import java.io.IOException;

public interface IdInput
{

	public long next() throws IOException;

	public void close() throws IOException;

}
