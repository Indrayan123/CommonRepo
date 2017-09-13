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
	//Getting the projetcpath name from enviroment
	def projectpath=env.env.JOB_NAME
	
	
	env.UnstashSlaveBuildPath=env.SlaveBuildPath+env.Build_ID
//	env.UnstashSlaveBuildPath="/export/home/f23963/jar/R1_2017-09-07_1"
	env.Master_stashpath=env.Master_BUILD_PATH+"\\"+env.Build_ID
    
}
	
    stage('Stashing Build in Master')
    {
        
		dir(path: "${env.Master_stashpath}") 
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
      echo "slavebuildpath:${env.SlaveBuildPath}"
       dir(path: "${env.UnstashSlaveBuildPath}") 
       {
          echo "unstash path:${pwd()}"
 		unstash 'BuildResources'}  
        }
        
        try{
        
    stage ('Compile And Deploy To The Environment')
    {
        echo "compile path:${pwd()}"
        pomLoc=env.UnstashSlaveBuildPath+"/SOAAppDeployment"
        pomLoc='/opt/jenkins/agent/workspace/SOA12cDeployment/BuildAndDeploy/Builds/R1_2017-09-07_9/SOAAppDeployment'
        dir(path:"${pomLoc}")
        {
            env.scriptOp =sh ('#!/bin/sh -e\n'+ "/opt/oracle/middleware/oracle_common/modules/org.apache.maven_3.2.5/bin/mvn pre-integration-test -Dsoapassword=${env.soapass} -DserverURL=${env.soaurl} -Duser=${env.soauser}")
        }
        
        echo "script o/p:${env.scriptOp}"
        
        
    }
    echo "writing success status in revision file"
    node('master')
{
    stage('Write or prepend to Revision File')
    {
    def RevisionMethods = ""
    
	    git 'https://github.com/Indrayan123/CommonRepo'
         RevisionMethods = load("ProcessNewRevisionFile.groovy")

        
    def revisionfile=env.RevisionFilepath+File.separator+"Revision.txt"
     
    
		
	RevisionMethods.parseRevisionFile(revisionfile,,env.BUILD_ID,env.ENV_Name,env.BUILD_TIMESTAMP,'Deployed','true')
    }
    
}
}

catch (e)
{
    echo "${e}"
  echo "script o/p:${env.scriptOp}"
  echo "wrting failed status"
  node('master')
{
    stage('Write or prepend to Revision File')
    {
    def RevisionMethods = ""
    
	    git 'https://github.com/Indrayan123/CommonRepo'
         RevisionMethods = load("ProcessNewRevisionFile.groovy")

        
    def revisionfile=env.RevisionFilepath+File.separator+"Revision.txt"
     
    
		
	RevisionMethods.parseRevisionFile(revisionfile,,env.BUILD_ID,env.ENV_Name,env.BUILD_TIMESTAMP,'Deployed','Failed')
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
