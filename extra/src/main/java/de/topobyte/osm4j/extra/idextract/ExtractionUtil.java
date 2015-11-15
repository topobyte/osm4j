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

		return extractionItems;
	}

}
