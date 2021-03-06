package org.apache.maven.shared.release.exec;

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

import org.apache.maven.shared.release.ReleaseResult;
import org.apache.maven.shared.release.env.DefaultReleaseEnvironment;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;

import java.io.File;

public abstract class AbstractMavenExecutor
    implements MavenExecutor, LogEnabled
{

    private Logger logger;

    protected AbstractMavenExecutor()
    {
    }

    public void executeGoals( File workingDirectory,
                              String goals,
                              boolean interactive,
                              String additionalArguments,
                              String pomFileName,
                              ReleaseResult result )
        throws MavenExecutorException
    {
        executeGoals( workingDirectory, goals, new DefaultReleaseEnvironment(), interactive, additionalArguments, pomFileName, result );
    }

    public void executeGoals( File workingDirectory,
                              String goals,
                              boolean interactive,
                              String additionalArguments,
                              ReleaseResult result )
        throws MavenExecutorException
    {
        executeGoals( workingDirectory, goals, new DefaultReleaseEnvironment(), interactive, additionalArguments, result );
    }

    protected final Logger getLogger()
    {
        return logger;
    }

    public void enableLogging( Logger logger )
    {
        this.logger = logger;
    }

}
