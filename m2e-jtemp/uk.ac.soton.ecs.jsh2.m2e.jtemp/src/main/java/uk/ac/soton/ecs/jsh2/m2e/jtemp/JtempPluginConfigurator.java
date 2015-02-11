package uk.ac.soton.ecs.jsh2.m2e.jtemp;

import java.io.File;

import org.apache.maven.plugin.MojoExecution;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.m2e.core.lifecyclemapping.model.IPluginExecutionMetadata;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.configurator.AbstractBuildParticipant;
import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest;
import org.eclipse.m2e.jdt.AbstractSourcesGenerationProjectConfigurator;
import org.eclipse.m2e.jdt.IClasspathDescriptor;
import org.eclipse.m2e.jdt.IJavaProjectConfigurator;

/**
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class JtempPluginConfigurator extends AbstractSourcesGenerationProjectConfigurator
		implements
IJavaProjectConfigurator
{

	static final String BUNDLE_ID = "uk.ac.soton.ecs.jsh2.m2e.jtemp";

	@Override
	public void configure(ProjectConfigurationRequest request, IProgressMonitor monitor) throws CoreException {

	}

	@Override
	protected File[] getSourceFolders(ProjectConfigurationRequest request, MojoExecution mojoExecution,
			IProgressMonitor monitor) throws CoreException
	{
		return new File[] {
				new File("/target/generated-sources/jtemp"),
				new File("/target/generated-test-sources/jtemp")
		};
	}

	@Override
	public void configureRawClasspath(ProjectConfigurationRequest request, IClasspathDescriptor classpath,
			IProgressMonitor monitor) throws CoreException
	{
		final IMavenProjectFacade facade = request.getMavenProjectFacade();

		assertHasNature(request.getProject(), JavaCore.NATURE_ID);

		final File base = facade.getProject().getLocation().toFile();
		for (final MojoExecution mojoExecution : getMojoExecutions(request, monitor)) {
			final File[] sources = getSourceFolders(request, mojoExecution, monitor);

			for (File source : sources) {
				source = new File(base, source.toString());
				final IPath sourcePath = getFullPath(facade, source);

				final ILog log = Platform.getLog(Platform.getBundle(JtempPluginConfigurator.BUNDLE_ID));
				log.log(new Status(IStatus.INFO, BUNDLE_ID, source + " " + sourcePath));

				if (sourcePath != null) {
					classpath.addSourceEntry(sourcePath, facade.getOutputLocation(), true);
				}
			}
		}
	}

	@Override
	public AbstractBuildParticipant getBuildParticipant(IMavenProjectFacade projectFacade, MojoExecution execution,
			IPluginExecutionMetadata executionMetadata)
	{
		return new JtempBuildParticipant(execution);
	}
}
