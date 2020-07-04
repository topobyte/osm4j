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

package de.topobyte.osm4j.extra.idextract;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ExtractionUtil
{

	public static List<ExtractionItem> createExtractionItems(Path dirData,
			String fileNameIds, String fileNamesOutput) throws IOException
	{
		return createExtractionItems(dirData, new String[] { fileNameIds },
				fileNamesOutput);
	}

	public static List<ExtractionItem> createExtractionItems(Path dirData,
			String[] fileNamesIds, String fileNamesOutput) throws IOException
	{
		List<ExtractionItem> extractionItems = new ArrayList<>();

		DirectoryStream<Path> directories = Files.newDirectoryStream(dirData);
		sub: for (Path path : directories) {
			if (!Files.isDirectory(path)) {
				continue;
			}
			List<Path> pathsIds = new ArrayList<>();
			for (String fileNameIds : fileNamesIds) {
				Path ids = path.resolve(fileNameIds);
				if (!Files.exists(ids)) {
					continue sub;
				}
				pathsIds.add(ids);
			}
			Path pathOutput = path.resolve(fileNamesOutput);
			extractionItems.add(new ExtractionItem(pathsIds, pathOutput));
		}
		directories.close();

		return extractionItems;
	}

}
