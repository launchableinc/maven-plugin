package com.launchableinc.client.maven;

import org.apache.maven.RepositoryUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.eventspy.AbstractEventSpy;
import org.apache.maven.eventspy.EventSpy;
import org.apache.maven.execution.ExecutionEvent;
import org.apache.maven.execution.ExecutionEvent.Type;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.lifecycle.internal.DependencyContext;
import org.apache.maven.lifecycle.internal.MojoExecutor;
import org.apache.maven.lifecycle.internal.PhaseRecorder;
import org.apache.maven.lifecycle.internal.ProjectIndex;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.PluginManagerException;
import org.apache.maven.plugin.PluginResolutionException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.eclipse.aether.impl.ArtifactResolver;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;

import java.io.File;
import java.net.MalformedURLException;

/**
 *
 */
@Component(role = EventSpy.class, hint = "launchable")
public class Bootstrap extends AbstractEventSpy {

    @Requirement
    ArtifactResolver resolver;

    @Requirement
    private Logger logger;

    @Requirement
    BuildPluginManager pluginManager;

    @Requirement
    ArtifactFactory artifactFactory;

    @Override
    public void onEvent(Object event) throws Exception {
        try {
            if (event instanceof ExecutionEvent) {
                ExecutionEvent ee = (ExecutionEvent) event;
                if (ee.getType()== Type.MojoStarted) {
                    onMojoStarted(ee);
                }
            }
        } catch (RuntimeException|Error e) {
            // default error handling in EventSpy eats the stack trace, so let's print that by ourselves
            logger.error("Unexpected error",e);
        }
    }

    /**
     * Called when a mojo starts.
     *
     * {@link MojoExecutor#execute(MavenSession, MojoExecution, ProjectIndex, DependencyContext, PhaseRecorder)} calls this.
     */
    private void onMojoStarted(ExecutionEvent ee) {
        MojoExecution me = ee.getMojoExecution();
        System.out.println(me.getGroupId()+":"+me.getArtifactId()+":"+me.getGoal());

        if (me.getGroupId().equals("org.apache.maven.plugins")) {
            if ((me.getArtifactId().equals("maven-surefire-plugin") && me.getGoal().equals("test"))
             || (me.getArtifactId().equals("maven-failsafe-plugin") && me.getGoal().equals("integration-test")))
                injectSurefireProvider(ee, me);

        }
    }

    private void injectSurefireProvider(ExecutionEvent ee, MojoExecution me) {
        try {
            PluginDescriptor pd = me.getMojoDescriptor().getPluginDescriptor();
            Artifact artifact = artifactFactory.createArtifactWithClassifier(
                "com.launchableinc.client", "surefire-provider", "1.0-SNAPSHOT", "jar", "shaded");
            ArtifactRequest request = new ArtifactRequest(
                RepositoryUtils.toArtifact(artifact),
                RepositoryUtils.toRepos(ee.getProject().getRemoteArtifactRepositories()),
                null
            );
            ArtifactResult result = resolver.resolveArtifact(ee.getSession().getRepositorySession(), request);
            File jar = result.getArtifact().getFile();

            // make this available
            ClassRealm realm = pluginManager.getPluginRealm(ee.getSession(), me.getMojoDescriptor().getPluginDescriptor());
            realm.addURL(jar.toURL());

            // Surefire looks up this map to determine what classpath to create for a forked JVM
            artifact.setFile(jar);
            pd.getArtifactMap().put(
                artifact.getGroupId()+":"+artifact.getArtifactId(), artifact);
        } catch (ArtifactResolutionException | MalformedURLException | PluginResolutionException | PluginManagerException e) {
            logger.error("Failed to resolve Launchable surefire provider", e);
        }
    }

// this gets ignored
//    private void injectSurefireProvider(ExecutionEvent ee, MojoExecution me) {
//        ComponentDependency dep = new ComponentDependency();
//        dep.setGroupId("com.launchableinc.client");
//        dep.setArtifactId("surefire-provider");
//        dep.setVersion("1.0-SNAPSHOT");
//        dep.setType("jar");
//        me.getMojoDescriptor().getPluginDescriptor().getDependencies().add(
//            dep
//        );
//    }


    // turns out at this point Maven hasn't yet created a class loader
//    private void injectSurefireProvider(ExecutionEvent ee, MojoExecution me) {
//        try {
//            PluginDescriptor pd = me.getMojoDescriptor().getPluginDescriptor();
//            ArtifactRequest request = new ArtifactRequest(
//                new DefaultArtifact(
//                    "com.launchableinc.client", "surefire-provider", "jar", "1.0-SNAPSHOT"
//                ),
//                RepositoryUtils.toRepos(ee.getProject().getRemoteArtifactRepositories()),
//                null
//            );
//            ArtifactResult result = resolver.resolveArtifact(ee.getSession().getRepositorySession(), request);
//            pd.getClassRealm().addURL(result.getArtifact().getFile().toURL());
//        } catch (ArtifactResolutionException | MalformedURLException e) {
//            logger.error("Failed to resolve Launchable surefire provider", e);
//        }
//    }
}
