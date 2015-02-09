package uk.ac.soton.ecs.jsh2.m2e.jtemp;

import java.io.File;
import java.util.Set;

import org.apache.maven.plugin.MojoExecution;
import org.codehaus.plexus.util.Scanner;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2e.core.project.configurator.MojoExecutionBuildParticipant;
import org.sonatype.plexus.build.incremental.BuildContext;

/**
 * BuildParticpant for jtemp
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class JtempBuildParticipant extends MojoExecutionBuildParticipant {

	public JtempBuildParticipant(MojoExecution execution) {
		super(execution, true);
	}

	@Override
	public Set<IProject> build(int kind, IProgressMonitor monitor) throws Exception {
		final BuildContext buildContext = getBuildContext();

		final File base = getMavenProjectFacade().getMavenProject(monitor).getBasedir();
		// check if any of the jtemp files changed
		final File source = new File(base, "/src/main/jtemp");
		Scanner ds = buildContext.newScanner(source);
		ds.scan();
		final String[] includedFiles = ds.getIncludedFiles();

		// check if any of the jtemp files changed
		final File testSource = new File(base, "/src/test/jtemp");
		ds = buildContext.newScanner(testSource);
		ds.scan();
		final String[] includedTestFiles = ds.getIncludedFiles();

		if (includedFiles == null || includedFiles.length <= 0)
		{
			if (includedTestFiles == null || includedTestFiles.length <= 0) {
				return null;
			}
		}

		// execute mojo
		final Set<IProject> result = super.build(kind, monitor);

		// tell m2e builder to refresh generated files
		buildContext.refresh(new File(base, "target/generated-sources/jtemp"));
		buildContext.refresh(new File(base, "target/generated-test-sources/jtemp"));

		return result;
	}
}
