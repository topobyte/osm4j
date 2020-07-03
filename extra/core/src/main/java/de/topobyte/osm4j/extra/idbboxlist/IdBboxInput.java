package de.topobyte.osm4j.extra.idbboxlist;

import java.io.IOException;

public interface IdBboxInput
{

	public IdBboxEntry next() throws IOException;

	public void close() throws IOException;

}
