// Copyright 2021 Sebastian Kuerten
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

package de.topobyte.osm4j.changeset;

public class Comment
{

	private long uid;
	private String user;
	private long date;
	private String text;

	public Comment(long uid, String user, long date, String text)
	{
		this.uid = uid;
		this.user = user;
		this.date = date;
		this.text = text;
	}

	public long getUid()
	{
		return uid;
	}

	public String getUser()
	{
		return user;
	}

	public long getDate()
	{
		return date;
	}

	public String getText()
	{
		return text;
	}

}
