package uk.ac.soton.ecs.jsh2.m2e.jtemp;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.apache.maven.plugin.MojoExecution;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.m2e.core.lifecyclemapping.model.IPluginExecutionMetadata;
import org.eclipse.m2e.core.project.IMavenProjectFacade;
import org.eclipse.m2e.core.project.configurator.AbstractBuildParticipant;
import org.eclipse.m2e.core.project.configurator.AbstractProjectConfigurator;
import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest;
import org.eclipse.m2e.jdt.IClasspathDescriptor;
import org.eclipse.m2e.jdt.IJavaProjectConfigurator;

/**
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class JtempPluginConfigurator extends AbstractProjectConfigurator implements IJavaProjectConfigurator {

	private static final String BUNDLE_ID = "uk.ac.soton.ecs.jsh2.m2e.jtemp";

	@Override
	public void configure(ProjectConfigurationRequest request, IProgressMonitor monitor) throws CoreException {

	}

	@Override
	public void configureRawClasspath(ProjectConfigurationRequest request, IClasspathDescriptor classpath,
			IProgressMonitor monitor) throws CoreException
	{
		final ILog log = Platform.getLog(Platform.getBundle(BUNDLE_ID));

		final IProject project = request.getProject();
		final IJavaProject javaProject = JavaCore.create(project);
		final IMavenProjectFacade facade = request.getMavenProjectFacade();

		final File base = facade.getMavenProject(monitor).getBasedir();
		try {
			createSource(classpath, project, javaProject, facade, base + "/target/generated-sources/jtemp");
		} catch (final Throwable e) {
			log.log(new Status(IStatus.ERROR, BUNDLE_ID, "Error adding generated classes to the build path", e));
		}
		try {
			createSource(classpath, project, javaProject, facade, base + "/target/generated-test-sources");
		} catch (final Throwable e) {
			log.log(new Status(IStatus.ERROR, BUNDLE_ID, "Error adding generated test classes to the build path", e));
		}
	}

	protected void createSource(IClasspathDescriptor classpath, final IProject project, final IJavaProject javaProject,
			final IMavenProjectFacade facade, String sourceAttribute) throws IOException, CoreException
	{
		if (!new File(sourceAttribute).isAbsolute()) {
			sourceAttribute = project.getLocationURI().toString().replace("file:/", "") + "/" + sourceAttribute;
		}
		final File file = new File(sourceAttribute);
		final File canonicalFile = file.getCanonicalFile();
		if (!canonicalFile.exists())
			canonicalFile.mkdirs();
		URI uri = canonicalFile.toURI();
		uri = project.getPathVariableManager().convertToRelative(uri, false, "TEST");

		final String relative = uri.toString().replace("PROJECT_LOC", "");
		final IFolder folder = project.getFolder(relative);

		IPath path = folder.getProjectRelativePath();
		path = javaProject.getPath().append(path);

		if (!classpath.containsPath(path))
			classpath.addSourceEntry(path, facade.getOutputLocation(), true);
	}

	@Override
	public void configureClasspath(IMavenProjectFacade facade, IClasspathDescriptor classpath, IProgressMonitor monitor)
			throws CoreException
	{
		// All Done in getRawClassPath
	}

	@Override
	public AbstractBuildParticipant getBuildParticipant(IMavenProjectFacade projectFacade, MojoExecution execution,
			IPluginExecutionMetadata executionMetadata)
	{
		return new JtempBuildParticipant(execution);
	}
}
