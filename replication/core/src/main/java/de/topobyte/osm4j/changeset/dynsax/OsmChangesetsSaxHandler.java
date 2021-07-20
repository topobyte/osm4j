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

package de.topobyte.osm4j.changeset.dynsax;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.topobyte.osm4j.changeset.Comment;
import de.topobyte.osm4j.changeset.OsmChangeset;
import de.topobyte.osm4j.core.model.iface.OsmTag;
import de.topobyte.osm4j.core.model.impl.Tag;
import de.topobyte.xml.dynsax.Child;
import de.topobyte.xml.dynsax.ChildType;
import de.topobyte.xml.dynsax.Data;
import de.topobyte.xml.dynsax.DynamicSaxHandler;
import de.topobyte.xml.dynsax.Element;
import de.topobyte.xml.dynsax.ParsingException;

class OsmChangesetsSaxHandler extends DynamicSaxHandler
{

	static OsmChangesetsSaxHandler createInstance()
	{
		return new OsmChangesetsSaxHandler(null);
	}

	static OsmChangesetsSaxHandler createInstance(OsmChangesetsHandler handler)
	{
		return new OsmChangesetsSaxHandler(handler);
	}

	private OsmChangesetsHandler handler;
	private DateParser dateParser = new DateParser();

	private OsmChangesetsSaxHandler(OsmChangesetsHandler handler)
	{
		this.handler = handler;
		setRoot(createRoot(), true);
	}

	void setHandler(OsmChangesetsHandler handler)
	{
		this.handler = handler;
	}

	private Element root, changeset, tag, discussion, comment, text;

	private static final String NAME_OSM = "osm";
	private static final String NAME_CHANGESET = "changeset";
	private static final String NAME_TAG = "tag";
	private static final String NAME_DISCUSSION = "discussion";
	private static final String NAME_COMMENT = "comment";
	private static final String NAME_TEXT = "text";

	private static final String ATTR_ID = "id";
	private static final String ATTR_CREATED_AT = "created_at";
	private static final String ATTR_CLOSED_AT = "closed_at";
	private static final String ATTR_OPEN = "open";
	private static final String ATTR_NUM_CHANGES = "num_changes";
	private static final String ATTR_USER = "user";
	private static final String ATTR_UID = "uid";
	private static final String ATTR_MIN_LAT = "min_lat";
	private static final String ATTR_MAX_LAT = "max_lat";
	private static final String ATTR_MIN_LON = "min_lon";
	private static final String ATTR_MAX_LON = "max_lon";
	private static final String ATTR_COMMENTS_COUNT = "comments_count";
	private static final String ATTR_DATE = "date";

	private static final String ATTR_K = "k";
	private static final String ATTR_V = "v";

	private Element createRoot()
	{
		root = new Element(NAME_OSM, false);

		changeset = new Element(NAME_CHANGESET, false);
		tag = new Element(NAME_TAG, false);
		discussion = new Element(NAME_DISCUSSION, false);
		comment = new Element(NAME_COMMENT, false);
		text = new Element(NAME_TEXT, true);

		changeset.addAttribute(ATTR_ID);
		changeset.addAttribute(ATTR_CREATED_AT);
		changeset.addAttribute(ATTR_CLOSED_AT);
		changeset.addAttribute(ATTR_OPEN);
		changeset.addAttribute(ATTR_NUM_CHANGES);
		changeset.addAttribute(ATTR_USER);
		changeset.addAttribute(ATTR_UID);
		changeset.addAttribute(ATTR_MIN_LAT);
		changeset.addAttribute(ATTR_MAX_LAT);
		changeset.addAttribute(ATTR_MIN_LON);
		changeset.addAttribute(ATTR_MAX_LON);
		changeset.addAttribute(ATTR_COMMENTS_COUNT);

		tag.addAttribute(ATTR_K);
		tag.addAttribute(ATTR_V);

		comment.addAttribute(ATTR_USER);
		comment.addAttribute(ATTR_UID);
		comment.addAttribute(ATTR_DATE);

		root.addChild(new Child(changeset, ChildType.IGNORE, true));

		changeset.addChild(new Child(tag, ChildType.LIST, false));
		changeset.addChild(new Child(discussion, ChildType.SINGLE, false));
		discussion.addChild(new Child(comment, ChildType.LIST, false));
		comment.addChild(new Child(text, ChildType.SINGLE, false));

		return root;
	}

	@Override
	public void emit(Data data) throws ParsingException
	{
		if (data.getElement() == changeset) {
			OsmChangeset cs = createChangeset(data);
			fillTags(cs, data);
			fillDiscussion(cs, data);

			try {
				handler.handle(cs);
			} catch (IOException e) {
				throw new ParsingException("while handling create", e);
			}
		}
	}

	private void fillTags(OsmChangeset cs, Data data)
	{
		List<Data> list = data.getList(NAME_TAG);
		if (list == null) {
			return;
		}

		List<OsmTag> tags = new ArrayList<>();
		for (Data child : list) {
			String k = child.getAttribute(ATTR_K);
			String v = child.getAttribute(ATTR_V);
			tags.add(new Tag(k, v));
		}
		cs.setTags(tags);
	}

	private void fillDiscussion(OsmChangeset cs, Data data)
	{
		Data discussion = data.getSingle(NAME_DISCUSSION);
		if (discussion == null) {
			return;
		}
		List<Data> commentsData = discussion.getList(NAME_COMMENT);
		List<Comment> comments = new ArrayList<>(commentsData.size());
		for (Data comment : commentsData) {
			String aUid = comment.getAttribute("uid");
			String user = comment.getAttribute("user");
			String aDate = comment.getAttribute("date");
			long uid = parseLong(aUid, -1);
			long date = parseDate(aDate, -1);
			Data text = comment.getSingle(NAME_TEXT);
			comments.add(new Comment(uid, user, date, text.getText()));
		}
		cs.setComments(comments);
	}

	private OsmChangeset createChangeset(Data data)
	{
		String aId = data.getAttribute(ATTR_ID);
		String aCreatedAt = data.getAttribute(ATTR_CREATED_AT);
		String aClosedAt = data.getAttribute(ATTR_CLOSED_AT);
		String aOpen = data.getAttribute(ATTR_OPEN);
		String aNumChanges = data.getAttribute(ATTR_NUM_CHANGES);
		String user = data.getAttribute(ATTR_USER);
		String aUid = data.getAttribute(ATTR_UID);
		String aMinLat = data.getAttribute(ATTR_MIN_LAT);
		String aMaxLat = data.getAttribute(ATTR_MAX_LAT);
		String aMinLon = data.getAttribute(ATTR_MIN_LON);
		String aMaxLon = data.getAttribute(ATTR_MAX_LON);
		String aCommentsCount = data.getAttribute(ATTR_COMMENTS_COUNT);

		long id = parseLong(aId, -1);

		long createdAt = parseDate(aCreatedAt, -1);
		long closedAt = parseDate(aClosedAt, -1);

		boolean open = false;
		if (aOpen != null) {
			open = aOpen.equals("true");
		}

		int numChanges = parseInt(aNumChanges, -1);

		if (user == null) {
			user = "";
		}

		long uid = parseLong(aUid, -1);

		double minLat = parseDouble(aMinLat, Double.NaN);
		double maxLat = parseDouble(aMaxLat, Double.NaN);
		double minLon = parseDouble(aMinLon, Double.NaN);
		double maxLon = parseDouble(aMaxLon, Double.NaN);

		int commentsCount = parseInt(aCommentsCount, -1);

		return new OsmChangeset(id, createdAt, closedAt, open, numChanges, user,
				uid, minLat, maxLat, minLon, maxLon, commentsCount);
	}

	private int parseInt(String value, int defaultValue)
	{
		if (value == null) {
			return defaultValue;
		}
		return Integer.parseInt(value);
	}

	private long parseLong(String value, long defaultValue)
	{
		if (value == null) {
			return defaultValue;
		}
		return Long.parseLong(value);
	}

	private double parseDouble(String value, double defaultValue)
	{
		if (value == null) {
			return defaultValue;
		}
		return Double.parseDouble(value);
	}

	private long parseDate(String value, long defaultValue)
	{
		if (value == null) {
			return defaultValue;
		}
		return dateParser.parse(value).getMillis();
	}

}
