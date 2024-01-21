package org.doble.commands;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

import org.doble.adr.*;
import org.doble.adr.model.Link;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

/**
 * Subcommand to link ADRs with one another
 *
 * TODO expand this description
 *
 * @author adoble
 *
 */

@Command(name = "link",
         description = "Links ADRs with one another.")
public class CommandLink implements Callable<Integer> {
	@Parameters(hidden = true)
	List<String> parameters;
	@Parameters(index = "0", description = "Identifier of ADR to be linked from")    int sourceADRId;
    @Parameters(index = "1", description = "Identifier of ADR to be linked to")    int targetADRId;

    @Option(names = "-sd", arity="0..1", description = "Description of the source link")
    String sourceDescription;

    @Option(names = "-td",  arity="0..1", description = "Description of the target link")
    String targetDescription;


	@ParentCommand
	private CommandADR commandADR;

	private Environment env;
	private ADRProperties properties;

	/**
	 * TODO
	 */
	public CommandLink()  {

	}

	@Override
	public Integer call() throws ADRException {
		env = commandADR.getEnvironment();

		properties = new ADRProperties(env);

		// Load the properties
		try {
			properties.load();
		} catch (ADRException e) {
			env.err.println("FATAL: Cannot load properties file. Exception message ->" + e.getMessage() );
			return ADR.ERRORGENERAL;
		}

		Path rootPath = ADR.getRootPath(env);
		Path docsPath = rootPath.resolve(properties.getProperty("docPath"));

		env.out.println("Source ADR ID:" + sourceADRId);
		env.out.println("Target ADR ID:" + targetADRId);
		env.out.println("Source ADR Description:" + sourceDescription);
		env.out.println("Target ADR Description:" + targetDescription);

		try {
			if (!checkADRExists(targetADRId)) {
				String msg = "Linked to ADR (" + targetADRId + "), but this ADR does not exist";
				System.err.println("ERROR: " + msg);
				throw new ADRException(msg);
			}

			if (!checkADRExists(sourceADRId)) {
				String msg = "Linked to ADR (" + targetADRId + "), but this ADR does not exist";
				System.err.println("ERROR: " + msg);
				throw new ADRException(msg);
			}

			String linkSpecSource = String.format("%d:%s:%s",targetADRId,sourceDescription,targetDescription);
			Link linkSource = new Link(linkSpecSource, docsPath);

			String linkSpecTarget = String.format("%d:%s:%s",sourceADRId,sourceDescription,targetDescription);
			Link linkTarget = new Link(linkSpecTarget, docsPath);

			AdrUtil.addLinks(docsPath, linkSource, sourceADRId, true);
			AdrUtil.addLinks(docsPath, linkTarget, targetADRId, false);

		} catch (IOException | LinkSpecificationException e) {
			String msg = "ERROR: -l parameter incorrectly formed.";
			env.err.println(msg);   //TODO check that there is a test for this.
			return CommandLine.ExitCode.USAGE;  // Ensure that the usage instruction are shown
		}


		//

		return 0;
	}

	private boolean checkADRExists(Integer adrID) throws ADRException, IOException {
		// Get the doc path
		Path rootPath = ADR.getRootPath(env);
		Path docsPath = rootPath.resolve(properties.getProperty("docPath"));

		// Format the ADR ID
		String formattedADRID = String.format("%04d", adrID);

		boolean found = false;
		try (DirectoryStream<Path>  stream = Files.newDirectoryStream(docsPath)) {
			for (Path entry: stream) {
				if (entry.getFileName().toString().startsWith(formattedADRID)) {
					found = true;
					break;
				}
			}
		}

		return found;
	}
}
