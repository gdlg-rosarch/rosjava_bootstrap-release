package org.ros.gradle_plugins;

import org.gradle.api.Project;
import org.gradle.api.Plugin;
import org.gradle.api.*;

/*
 * Provides information about the ros workspace.
 *
 * - project.ros.mavenPath : location of local ros maven repositories (in your chained workspaces)
 * - project.ros.mavenDeploymentRepository : location of the ros maven repository you will publish to
 *
 * It also performs the following actions
 * 
 * - checks and makes sure the maven plugin is running
 * - constructs the sequence of dependent maven repos (local ros maven repos, mavenLocal, external ros maven repo)
 * - configures the uploadArchives for artifact deployment to the local ros maven repo (devel/share/maven)
 */
class RosPlugin implements Plugin<Project> {
    Project project
    
	def void apply(Project project) {
	    this.project = project
        if (!project.plugins.findPlugin('maven')) {
            project.apply(plugin: 'maven')
        }
        /* Create project.ros.* property extensions */
        project.extensions.create("ros", RosExtension)
        project.ros.mavenPath = "$System.env.ROS_MAVEN_PATH".split(':')
        project.ros.mavenDeploymentRepository = "$System.env.ROS_MAVEN_DEPLOYMENT_REPOSITORY"
        def mavenRepository = "$System.env.ROS_MAVEN_REPOSITORY"
        if ( mavenRepository != 'null' ) {
            project.ros.mavenRepository = mavenRepository
        }
        /* 
         * Could use some better handling for when this is not defined as it sets
         * file://null, but it doesn't seem to hurt the process any
         */
        def repoURLs = project.ros.mavenPath.collect { 'file://' + it }
        project.repositories {
            repoURLs.each { p ->
                maven {
                    url p
                }
            }
            mavenLocal()
            maven {
                url project.ros.mavenRepository
            }
            mavenCentral()
        }
    }
}

/* http://www.gradle.org/docs/nightly/dsl/org.gradle.api.plugins.ExtensionAware.html */
class RosExtension {
    List<String> mavenPath
    String mavenDeploymentRepository
    String mavenRepository
    
    RosExtension() {
        /* Initialising the strings here gets rid of the dynamic property deprecated warnings. */
        this.mavenDeploymentRepository = ""
        this.mavenRepository = "https://github.com/rosjava/rosjava_mvn_repo/raw/master"
    }
}
