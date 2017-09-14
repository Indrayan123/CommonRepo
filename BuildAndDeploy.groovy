node(env.ENV_Name)
{
    stage('Getting Environmental Properties')
	{
		
	env.soapass = env.SOASERVER_PASSWORD
	env.soauser = env.SOASERVER_USERNAME
	env.soaurl=env.SOASERVER_URL
	env.master_workSpace=env.Master_WorkSpace
	env.slave_workspace=env.Slave_WorkSpace
        
    }
    
    
}



node('master')

{
	
    stage ('Setting Stash And Unsatsh Path')
{
	//Getting the projectpath name from environment
	def projectpath=env.JOB_NAME
	echo "projectpath:${projectpath}"
	env.masterprojectpath=projectpath.replaceAll("/","\\\\")
	
	env.Unstash_SlaveBuildPath=env.slave_workspace+"/"+projectpath+"/Builds/"+env.Build_ID
	echo "unstashpath:${env.Unstash_SlaveBuildPath}"
	env.Stash_MasterBuildPath=env.Master_Build_Path+"\\"+env.Build_ID
	echo "stashpath:${env.Stash_MasterBuildPath}"
	env.RevisionPath=env.master_workSpace+"\\"+env.masterprojectpath+"\\Revision"
	echo "revisionpath:${env.RevisionPath}"
    
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
        pomLoc=env.Unstash_SlaveBuildPath+"/SOAAppDeployment"
        dir(path:"${pomLoc}")
        {
            env.scriptOp =sh ('#!/bin/sh -e\n'+ "/opt/oracle/middleware/oracle_common/modules/org.apache.maven_3.2.5/bin/mvn -e pre-integration-test -Dsoapassword=${env.soapass} -DserverURL=${env.soaurl} -Duser=${env.soauser}")
        }
        
        echo "script o/p:${env.scriptOp}"
        
        
    }
    echo "writing success status in revision file"
    node('master')
{
    stage('Write or prepend to Revision File')
    {
    def RevisionMethods = ""
    
	    git env.Git_Groovy_CommonLocation
         RevisionMethods = load("ProcessNewRevisionFile.groovy")

        
    def revisionfile=env.RevisionPath+File.separator+"Revision.txt"
     
    
		
	RevisionMethods.parseRevisionFile(revisionfile,,env.BUILD_ID,env.ENV_Name,env.BUILD_TIMESTAMP,'Deployed','true')
    }
    
}
}

catch (e)
{
    echo "${e}"
  echo "wrting failed status"
  node('master')
{
    stage('Write or prepend to Revision File')
    {
    def RevisionMethods = ""
    
	    git env.Git_Groovy_CommonLocation
         RevisionMethods = load("ProcessNewRevisionFile.groovy")

        
    def revisionfile=env.RevisionPath+File.separator+"Revision.txt"
     
    
		
	RevisionMethods.parseRevisionFile(revisionfile,,env.BUILD_ID,env.ENV_Name,env.BUILD_TIMESTAMP,'NotDeployed','Failed')
    }
    
}
}
    
}

/*node('master')
{
    stage('Write or prepend to Revision File')
    {
    def RevisionMethods = ""
    
	    git 'https://github.com/Indrayan123/CommonRepo'
         RevisionMethods = load("ProcessNewRevisionFile.groovy")

        
    def revisionfile=env.RevisionFilepath+File.separator+"Revision.txt"
     
    
		
	RevisionMethods.parseRevisionFile(revisionfile,,env.BUILD_ID,env.ENV_Name,env.BUILD_TIMESTAMP,'Deployed','true')
    }
    
}*/
