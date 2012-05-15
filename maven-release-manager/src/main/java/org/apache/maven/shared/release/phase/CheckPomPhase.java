package org.apache.maven.shared.release.phase;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.project.MavenProject;
import org.apache.maven.scm.manager.NoSuchScmProviderException;
import org.apache.maven.scm.repository.ScmRepositoryException;
import org.apache.maven.shared.release.ReleaseExecutionException;
import org.apache.maven.shared.release.ReleaseFailureException;
import org.apache.maven.shared.release.ReleaseResult;
import org.apache.maven.shared.release.config.ReleaseDescriptor;
import org.apache.maven.shared.release.env.ReleaseEnvironment;
import org.apache.maven.shared.release.scm.ReleaseScmRepositoryException;
import org.apache.maven.shared.release.scm.ScmRepositoryConfigurator;
import org.apache.maven.shared.release.util.ReleaseUtil;
import org.codehaus.plexus.util.StringUtils;

import java.util.Iterator;
import java.util.List;

/**
 * Phase that checks the validity of the POM before release.
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @plexus.component role="org.apache.maven.shared.release.phase.ReleasePhase" role-hint="check-poms"
 */
public class CheckPomPhase
    extends AbstractReleasePhase
{
    /**
     * Retrieve an SCM repository, useful for validating an URL.
     *
     * @plexus.requirement
     */
    private ScmRepositoryConfigurator scmRepositoryConfigurator;

    public ReleaseResult execute( ReleaseDescriptor releaseDescriptor, ReleaseEnvironment releaseEnvironment, List<MavenProject> reactorProjects )
        throws ReleaseExecutionException, ReleaseFailureException
    {
        ReleaseResult result = new ReleaseResult();

        // Currently, we don't deal with multiple SCM locations in a multiproject
        if ( StringUtils.isEmpty( releaseDescriptor.getScmSourceUrl() ) )
        {
            MavenProject rootProject = ReleaseUtil.getRootProject( reactorProjects );
            if ( rootProject != null && rootProject.getScm() != null )
            {
                if ( rootProject.getScm().getDeveloperConnection() != null )
                {
                    releaseDescriptor.setScmSourceUrl( rootProject.getScm().getDeveloperConnection() );
                }
                else if ( rootProject.getScm().getConnection() != null )
                {
                    releaseDescriptor.setScmSourceUrl( rootProject.getScm().getConnection() );
                }
            }

            if ( StringUtils.isEmpty( releaseDescriptor.getScmSourceUrl() ) )
            {
                throw new ReleaseFailureException(
                    "Missing required setting: scm connection or developerConnection must be specified." );
            }

            try
            {
                scmRepositoryConfigurator.getConfiguredRepository( releaseDescriptor, releaseEnvironment.getSettings() );
            }
            catch ( ScmRepositoryException e )
            {
                throw new ReleaseScmRepositoryException( e.getMessage(), e.getValidationMessages() );
            }
            catch ( NoSuchScmProviderException e )
            {
                throw new ReleaseFailureException(
                    "The provider given in the SCM URL could not be found: " + e.getMessage() );
            }
        }

        boolean containsSnapshotProjects = false;

        for ( Iterator<MavenProject> it = reactorProjects.iterator(); it.hasNext(); )
        {
            MavenProject project = it.next();

            if ( ArtifactUtils.isSnapshot( project.getVersion() ) )
            {
                containsSnapshotProjects = true;
            }
        }

        if ( !containsSnapshotProjects && !releaseDescriptor.isBranchCreation() && !releaseDescriptor.isUpdateVersions() )
        {
            throw new ReleaseFailureException( "You don't have a SNAPSHOT project in the reactor projects list." );
        }

        result.setResultCode( ReleaseResult.SUCCESS );

        return result;
    }

    public ReleaseResult simulate( ReleaseDescriptor releaseDescriptor, ReleaseEnvironment releaseEnvironment, List<MavenProject> reactorProjects )
        throws ReleaseExecutionException, ReleaseFailureException
    {
        // It makes no modifications, so simulate is the same as execute
        return execute( releaseDescriptor, releaseEnvironment, reactorProjects );
    }
}
