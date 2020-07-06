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

package de.topobyte.osm4j.diskstorage;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.slimjars.dist.gnu.trove.map.hash.TLongObjectHashMap;

/**
 * An implementation of BlockProvider as a cache that can be used to cache
 * results of another BlockProvider.
 * 
 * It caches blocks in a map up to a certain capacity and manages insertion and
 * deletion of cached items by using a FIFO-replacement strategy.
 * 
 * @param <T>
 *            the type of provided elements
 * 
 * @author Sebastian Kuerten (sebastian@topobyte.de)
 */
public class Cache<T> implements BlockProvider<T>
{

	static final Logger logger = LoggerFactory.getLogger(Cache.class);

	private static final int DEFAULT_CAPACITY = 1000;

	private BlockProvider<T> provdier;

	private int capacity = DEFAULT_CAPACITY;
	// private Map<Long, Block> cachedBlocks = new HashMap<Long, Block>();
	private TLongObjectHashMap<T> cachedBlocks = new TLongObjectHashMap<>();
	// private List<Long> list = new ArrayList<Long>();
	private List<Long> list = new LinkedList<>();

	/**
	 * Create a cache for the given BlockProvider.
	 * 
	 * @param provider
	 *            the provider to cache blocks for.
	 */
	public Cache(BlockProvider<T> provider)
	{
		this(provider, DEFAULT_CAPACITY);
	}

	/**
	 * Create a cache for the given BlockProvider.
	 * 
	 * @param provider
	 *            the provider to cache blocks for.
	 * @param capacity
	 *            the number of block to keep in memory.
	 */
	public Cache(BlockProvider<T> provider, int capacity)
	{
		this.provdier = provider;
		this.capacity = capacity;
	}

	private static long last = -1;
	private static int hits = 0;
	private static int miss = 0;
	private static int lastHit = 0;

	// private Block lastBlock = null;
	@Override
	public T getBlock(long pos) throws IOException
	{
		// if (last == pos) {
		// hits += 1;
		// lastHit += 1;
		// return lastBlock;
		// }
		if (cachedBlocks.containsKey(pos)) {
			T block = cachedBlocks.get(pos);
			hits += 1;
			if (pos == last) {
				lastHit += 1;
			}
			last = pos;
			// lastBlock = block;
			return block;
		}
		T block = provdier.getBlock(pos);
		miss += 1;
		putToCache(pos, block);
		last = pos;
		// lastBlock = block;
		return block;
	}

	private void putToCache(long pos, T block)
	{
		cachedBlocks.put(pos, block);
		list.add(pos);
		if (list.size() > capacity) {
			long remove = list.remove(0);
			cachedBlocks.remove(remove);
		}
		// logger.debug("list size: " + list.size());
		// logger.debug("map size: " + cachedBlocks.size());
	}

	/**
	 * Print information about hit and miss rate.
	 */
	public static void printStatistics()
	{
		int total = hits + miss;
		logger.debug(String.format(
				"hits: %d, misses: %d, hitrate: %f, last hitrate: %f", hits,
				miss, hits / (double) total, lastHit / (double) total));
	}

}
