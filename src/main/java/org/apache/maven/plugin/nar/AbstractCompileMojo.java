package org.apache.maven.plugin.nar;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.tools.ant.Project;

/**
 * @author Mark Donszelmann
 */
public abstract class AbstractCompileMojo
    extends AbstractDependencyMojo
{

    /**
     * C++ Compiler
     * 
     * @parameter expression=""
     */
    private Cpp cpp;

    /**
     * C Compiler
     * 
     * @parameter expression=""
     */
    private C c;

    /**
     * Fortran Compiler
     * 
     * @parameter expression=""
     */
    private Fortran fortran;

    /**
     * By default NAR compile will attempt to compile using all known compilers against files in the directories specified by convention.
     * This allows configuration to a reduced set, you will have to specify each compiler to use in the configuration. 
     * 
     * @parameter expression="false"
     */
    protected boolean onlySpecifiedCompilers;

    /**
     * Maximum number of Cores/CPU's to use. 0 means unlimited.
     * 
     * @parameter expression=""
     */
    private int maxCores = 0;


    /**
     * Fail on compilation/linking error.
     * 
     * @parameter expression="" default-value="true"
     * @required
     */
    private boolean failOnError;

    /**
     * Sets the type of runtime library, possible values "dynamic", "static".
     * 
     * @parameter expression="" default-value="dynamic"
     * @required
     */
    private String runtime;

    /**
     * Set use of libtool. If set to true, the "libtool " will be prepended to the command line for compatible
     * processors.
     * 
     * @parameter expression="" default-value="false"
     * @required
     */
    private boolean libtool;

    /**
     * List of tests to create
     * 
     * @parameter expression=""
     */
    private List tests;

    /**
     * Java info for includes and linking
     * 
     * @parameter expression=""
     */
    private Java java;

    /**
     * Flag to cpptasks to indicate whether linker options should be decorated or not
     *
     * @parameter expression=""
     */
    protected boolean decorateLinkerOptions;

    private NarInfo narInfo;

    private List/* <String> */dependencyLibOrder;

    private Project antProject;

    protected final Project getAntProject()
    {
        if ( antProject == null )
        {
            // configure ant project
            antProject = new Project();
            antProject.setName( "NARProject" );
            antProject.addBuildListener( new NarLogger( getLog() ) );
        }
        return antProject;
    }

    public void setCpp(Cpp cpp) {
        this.cpp = cpp;
        cpp.setAbstractCompileMojo( this );
    }

    public void setC(C c) {
        this.c = c;
        c.setAbstractCompileMojo( this );
    }

    public void setFortran(Fortran fortran) {
        this.fortran = fortran;
        fortran.setAbstractCompileMojo( this );
    }

    protected final C getC()
    {
    	if ( onlySpecifiedCompilers && c == null )
    	{
    		setC( new C() );
    	}
        return c;
    }

    protected final Cpp getCpp()
    {
    	if ( onlySpecifiedCompilers && cpp == null )
    	{
    		setCpp( new Cpp() );
    	}
        return cpp;
    }

    protected final Fortran getFortran()
    {
    	if ( onlySpecifiedCompilers && fortran == null )
    	{
    		setFortran( new Fortran() );
    	}
        return fortran;
    }

    protected final int getMaxCores( AOL aol )
        throws MojoExecutionException
    {
        return getNarInfo().getProperty( aol, "maxCores", maxCores );
    }

    protected final boolean useLibtool( AOL aol )
        throws MojoExecutionException
    {
        return getNarInfo().getProperty( aol, "libtool", libtool );
    }

    protected final boolean failOnError( AOL aol )
        throws MojoExecutionException
    {
        return getNarInfo().getProperty( aol, "failOnError", failOnError );
    }

    protected final String getRuntime( AOL aol )
        throws MojoExecutionException
    {
        return getNarInfo().getProperty( aol, "runtime", runtime );
    }

    protected final String getOutput( AOL aol, String type )
        throws MojoExecutionException
    {
        return getNarInfo().getOutput( aol, getOutput( ! aol.getOS().equals( OS.WINDOWS ) && !  Library.EXECUTABLE.equals( type ) ) );
    }

    protected final List getTests()
    {
        if ( tests == null )
        {
            tests = Collections.EMPTY_LIST;
        }
        return tests;
    }

    protected final Java getJava()
    {
        if ( java == null )
        {
            java = new Java();
        }
        java.setAbstractCompileMojo( this );
        return java;
    }

    public final void setDependencyLibOrder( List/* <String> */order )
    {
        dependencyLibOrder = order;
    }

    protected final List/* <String> */getDependencyLibOrder()
    {
        return dependencyLibOrder;
    }

    protected final NarInfo getNarInfo()
        throws MojoExecutionException
    {
        if ( narInfo == null )
        {
        	String groupId = getMavenProject().getGroupId();
        	String artifactId = getMavenProject().getArtifactId();
        	
            File propertiesDir = new File( getMavenProject().getBasedir(), "src/main/resources/META-INF/nar/" + groupId + "/" + artifactId );
            File propertiesFile = new File( propertiesDir, NarInfo.NAR_PROPERTIES );

            narInfo = new NarInfo( 
                groupId, artifactId,
                getMavenProject().getVersion(), 
                getLog(),
                propertiesFile );
        }
        return narInfo;
    }
}
