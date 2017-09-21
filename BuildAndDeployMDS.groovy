node(env.ENV_Name)
{
    stage('Getting Environmental Properties')
	{
		
	env.soapass = env.SOASERVER_PASSWORD
	env.soauser = env.SOASERVER_USERNAME
	env.soaurl=env.SOASERVER_URL
	env.master_workSpace=env.WORKSPACE_LOCATION
	env.slave_workspace=env.Slave_WorkSpace
        
    }
    
    
}



node('master')

{
	
    stage ('Setting Stash And Unstash Path')
{
	//Getting the projectpath name from environment
	def projectpath=env.JOB_NAME
	echo "projectpath:${projectpath}"
	env.masterprojectpath=projectpath.replaceAll("/","\\\\")
	
	env.Unstash_SlaveBuildPath=env.slave_workspace+"/"+projectpath+"/Builds/"+env.Build_ID
	env.SlaveBuildPath=env.slave_workspace+"/"+projectpath+"/Builds"
	echo "unstashpath:${env.Unstash_SlaveBuildPath}"
	env.Stash_MasterBuildPath=env.Master_Build_Path+File.separator+env.Build_ID
	echo "stashpath:${env.Stash_MasterBuildPath}"
	//env.RevisionPath=env.master_workSpace+File.separator+env.masterprojectpath+File.separator+"Revision"
	env.RevisionPath=env.master_workSpace+File.separator+env.masterprojectpath+File.separator+".."+File.separator+"Revision"
	echo "revisionpath:${env.RevisionPath}"
	env.masterprojworkspacepath=env.master_workSpace+File.separator+env.masterprojectpath
	
    
}
	
    stage('Stashing Build in Master')
    {
        
		dir(path: "${env.Stash_MasterBuildPath}") 
		{
		    echo "path:${pwd()}"
		stash includes: '**', name: 'BuildResources'
		}
        
    }
    
}

node(env.ENV_Name)
{
    echo "connecting to the deployment environment"
    stage ('Unstash Build')
    {
     
       dir(path: "${env.Unstash_SlaveBuildPath}") 
       {
          echo "unstash path:${pwd()}"
 		unstash 'BuildResources'}  
        }
        
        try{
        
    stage ('Compile And Deploy To The Environment')
    {
        echo "compile path:${pwd()}"		
        pomLoc=env.Unstash_SlaveBuildPath+"/"+env.ApplicationName
        dir(path:"${pomLoc}")
        {
            def scriptOp = sh(script: '#!/bin/sh -e\n'+ "${env.Maven_Path}/mvn package", returnStatus: true)
			def scriptDtlOp = sh(script: '#!/bin/sh -e\n'+ "${env.Maven_Path}/mvn org.apache.maven.plugins:maven-antrun-plugin:run", returnStdout: true).split("\r?\n")
			if ("${scriptDtlOp}".contains('FAILED')) {  	 																		             	    
			env.Status = "Failure"			
		} else {
			env.Status = "Success"  																		           
		}
		echo "Deploy Details: ${env.Status}"
        }
        
        
    }
		
		stage('Cleaning the Slave Workspace')
		{
		 
			dir(env.SlaveBuildPath)
			{
        			 echo "Cleaning Directory: ${pwd()}"
				sh ("rm -rf ${env.Build_ID}")
    			}
		
		}
    echo "writing success status in revision file"
    node('master')
{
    stage('Write or prepend to Revision File')
    {
    def RevisionMethods = ""
    
	    dir(path:"${env.masterprojworkspacepath}")
	    {
         RevisionMethods = load("ProcessNewRevisionFile.groovy")
	    }
        
    def revisionfile=env.RevisionPath+File.separator+"Revision.txt"
     
    
		
	RevisionMethods.parseRevisionFile(revisionfile,,env.BUILD_ID,env.ENV_Name,env.BUILD_TIMESTAMP,'Deployed','NA')
    }
	stage('Clean Directory')
	{
	dir(env.masterprojworkspacepath)
	{
         echo "Cleaning Directory: ${pwd()}"
         sh ("rm -rf *.groovy")
    }
	}
    
}
}
catch (e)
{
    echo "${e}"
  echo "wrting failed status"
	node(env.ENV_Name)
	{
	stage('Cleaning the Slave Workspace')
		{
		 
			dir(env.SlaveBuildPath)
			{
        			 echo "Cleaning Directory: ${pwd()}"
				sh ("rm -rf ${env.Build_ID}")
    			}
		
		}
	}
  node('master')
{
    stage('Write or prepend to Revision File')
    {
    def RevisionMethods = ""
    
	    dir(path:"${env.masterprojworkspacepath}")
	    {
         RevisionMethods = load("ProcessNewRevisionFile.groovy")

	    }
    def revisionfile=env.RevisionPath+File.separator+"Revision.txt"
     
    
		
	RevisionMethods.parseRevisionFile(revisionfile,,env.BUILD_ID,env.ENV_Name,env.BUILD_TIMESTAMP,'DeploymentFailed','NA')
    }
    
    stage('Clean Directory')
	{
	dir(env.masterprojworkspacepath)
	{
         echo "Cleaning Directory: ${pwd()}"
         sh ("rm -rf *.groovy")
    }
	}
    
}
}
    
}
