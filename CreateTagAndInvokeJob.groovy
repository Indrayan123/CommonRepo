node('master'){
stage('Clean Directory') {
    dir(path: "${env.SUBDIRECTORY}"){
         echo "Cleaning Directory: ${pwd()}"
         sh ("rm -rf *")
    }
     } 
    stage('Checking Out Github source code') { 
        git "${env.ProjectGitHub_Location}"
    }
    stage('Creating Tag Folder in Git') { 
    env.tagName ='R1'+'_'+env.BUILD_TIMESTAMP+'_'+env.BUILD_ID
    env.unstashpath="${env.BUILD_LOCATION}"+env.tagName+"\\"+"${env.SUBDIRECTORY}"
    echo "unstashpath: ${env.unstashpath}"
    withCredentials([usernamePassword(credentialsId: 'GIT_credential', passwordVariable: 'GIT_PASSWORD', usernameVariable: 'GIT_USERNAME')]) {
    sh("git tag -a ${env.tagName} -m 'Tag Created for Build Id: ${env.BUILD_ID}'")
    
    sh('git push https://${GIT_USERNAME}:${GIT_PASSWORD}@github.com/Indrayan123/SOAApplicationRepository --tags')
    
    }
    def Message=sh(returnStdout: true, script: "git log -1").split("\r?\n")
    Message=Message.toString()
    
    echo "Comment: ${Message}"
    }
    stage('Create Tag folder in Master')
    {
      dir(path: "${pwd()}"+"\\"+"${env.SUBDIRECTORY}") 
		{
		    
		stash includes: '**', name: 'TagResources'
		}
		dir(path: "${env.unstashpath}") 
      {
           
		unstash 'TagResources' 
        }
    }
    
     stage('Invoke BuidAndDeploy')
     
     {
         def tag = env.tagName
		 echo "TagName: ${tag}"
		def buildparams="Build_ID=${tag}&ENV_Name=SOA_Dev"
		sh "curl -X POST 'http://6ztmqg2:8080/job/SOA12cDeployment/job/BuildAndDeploy/buildWithParameters'\
  -H 'Jenkins-Crumb:34219f9e475949d2807e127be956f051'\
   --user 'admin:34219f9e475949d2807e127be956f051'\
   --data  '${buildparams}'"
      
     }

}