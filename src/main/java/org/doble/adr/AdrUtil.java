package org.doble.adr;

import org.doble.adr.model.Link;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class AdrUtil {
	/**
	 * Writes the ADR (status section) with id supersededID that it has been
	 * superseded by the ADR with the id supersedesID.
	 *
	 * The message written in the superseded ADR has the form Superseded by the
	 * architecture decision record [supersedesID]
	 *
	 * @param supersededID The id of the superseded ADR.
	 * @param supersedesID The id of the ADR that supersedes it. TODO REMOVE as this
	 *                     is now not used.
	 */
	public static  void supersede(Path docsPath, int supersededID, int supersedesID) throws ADRException {
		Path supersededADRFile;
		Path[] paths;

		// Get the ADR file that is to be superseded
		try (Stream<Path> stream = Files.list(docsPath)) {
			paths = stream.filter(ADRFilter.filter((int) supersededID)).toArray(Path[]::new);

			if (paths.length == 1) {
				supersededADRFile = paths[0];

				// Read in the file
				List<String> lines = Files.readAllLines(supersededADRFile);

				// Find the Status section (before the context station) and add the reverse link
				// comment
				String line;
				for (int index = 0; index < lines.size(); index++) {
					line = lines.get(index);
					if (line.startsWith("== Context")) { // TODO Need to have use constants for the titles

						lines.add(index,String.format("Superseded by link:%s[%s]",ADR.getADRFileName(supersedesID, docsPath),"[ADR " + supersedesID + "]"));
						/*lines.add(index, "Superseded by [ADR " + supersedesID + "]("
							+ ADR.getADRFileName(supersedesID, docsPath) + ")");*/
						lines.add(index + 1, "");
						break;
					}
				}

				// Write out the file
				Files.write(supersededADRFile, lines); // TODO use a temporary file when making such changes
			} else {
				throw new ADRException(
					"FATAL: No matching ADR file found or more than one matching ADR file found with the id "
						+ supersededID);
			}
		} catch (Exception e) {
			throw new ADRException("FATAL: Problem with the superseding of ADR: " + supersededID, e);
		}
	}

	public static void addLinks(Path docsPath, Link link, int newId, boolean isForward) throws ADRException {
		Path existingADRFile;
		Path[] paths;


		// Get the ADR file that is to add forwardLinks
		try (Stream<Path> stream = Files.list(docsPath)) {
			paths = stream.filter(ADRFilter.filter((int) link.getId())).toArray(Path[]::new);

			if (paths.length == 1) {
				existingADRFile = paths[0];

				// Read in the file
				List<String> lines = Files.readAllLines(existingADRFile);

				// Find the Link section add the forward link
				// comment
				String line;
				for (int index = 0; index < lines.size(); index++) {
					line = lines.get(index);
					if (line.startsWith("<!-- end of file -->")) { // TODO Need to have use constants for the titles
						if (isForward) {


							String comment = link.getReverseComment().length() == 0 ?  "Influences" : link.getReverseComment();
							String linkText = String.format("* %s link:%s%s",comment, ADR.getADRFileName(newId, docsPath),"[ADR " + newId + "]");
							lines.add(index,linkText);

							/*lines.add(index, "* " + link.getReverseComment() + " " + "[ADR " + newId + "]("
								+ ADR.getADRFileName(newId, docsPath) + ")");*/
						} else {
							String linkText = String.format("* %s link:%s[%s]",link.getComment(), ADR.getADRFileName(newId, docsPath),"[ADR " + newId + "]");
							lines.add(index,linkText);
							/*lines.add(index, "* " + link.getComment() + " " + "[ADR " + newId + "]("
								+ ADR.getADRFileName(newId, docsPath) + ")");*/
						}

						lines.add(index + 1, "");
						break;
					}
				}

				// Write out the file
				Files.write(existingADRFile, lines); // TODO use a temporary file when making such changes
			} else {
				throw new ADRException(
					"FATAL: No matching ADR file found or more than one matching ADR file found with the id "
						+ link.getId());
			}
		} catch (Exception e) {
			throw new ADRException("FATAL: Problem with adding forward links in ADR: " + link.getId(), e);
		}
	}
}
