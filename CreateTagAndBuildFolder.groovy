node('master'){
stage('Clean Directory') {
    dir(path: "${env.ApplicationName}"){
         echo "Cleaning Directory: ${pwd()}"
         sh ("rm -rf *")
    }
    
         sh ("rm -rf *.groovy")
    
     } 
    stage('Checking Out Github source code') { 
        git credentialsId: "${env.GitHubCrdToken}", url: "${env.ProjectGitHub_Location}"
    }
    stage('Creating Tag Folder in Git') { 
    env.tagName ='R1'+'_'+env.BUILD_TIMESTAMP+'_'+env.BUILD_ID
    echo "${env.tagName}"
    env.BUILD_LOCATION=env.WORKSPACE_LOCATION+File.separator+env.JOB_NAME+File.separator+"Builds"+File.separator
    
    env.unstashpath=env.BUILD_LOCATION+env.tagName+File.separator+env.ApplicationName
	env.GitHubConnectionURL=env.ProjectGitHub_Location.replaceAll("https://","")
    
    withCredentials([usernamePassword(credentialsId: "${env.GitHubCrdToken}", passwordVariable: 'GIT_PASSWORD', usernameVariable: 'GIT_USERNAME')]) {
    sh("git tag -a ${env.tagName} -m 'Tag Created for Build Id: ${env.BUILD_ID}'")
	
    
    sh("git push https://${GIT_USERNAME}:${GIT_PASSWORD}@'${env.GitHubConnectionURL}' tag ${env.tagName}")
    
    }
    }
    stage('Fetching last commit comment') {
    def Message=sh(returnStdout: true, script: "git log -1").split("\r?\n")
    Message=Message.toString()
    env.TagComment="${Message}".substring(("${Message}".lastIndexOf(","))+1,"${Message}".lastIndexOf("]")).replaceAll("^\\s+","")
    if ("${env.TagComment}".size()>30)
    {
    env.TagComment="${env.TagComment}".substring(0,30)
    }
    else {
    env.TagComment="${env.TagComment}".substring(0,"${env.TagComment}".size())
    }
    }
    stage('Create Tag folder in Master')
    {
      dir(path: "${pwd()}"+File.separator+"${env.ApplicationName}") 
		{
		    
		stash includes: '**', name: 'TagResources'
		}
		dir(path: "${env.unstashpath}") 
      {
           
		unstash 'TagResources' 
        }
    }
    
    stage('Refresh Master Artifacts')
{
    build 'RefreshArtifactsForParameterizedBuild'
}    
        
    stage('Invoke Build&Deploy')
    {
        def tag=env.tagName
		def RevisionMethods = ""
    
	    git env.Git_Groovy_CommonLocation
         RevisionMethods = load("ProcessNewRevisionFile.groovy")
         
        env.RevisionPath=env.WORKSPACE_LOCATION+File.separator+env.JOB_NAME+File.separator+".."+File.separator+"Revision"
    def revisionfile=env.RevisionPath+File.separator+"Revision.txt"     
    
		
	RevisionMethods.parseRevisionFile(revisionfile,env.tagName,'SOA_Dev',env.BUILD_TIMESTAMP,'In Progress',env.TagComment)
    build job: 'BuildAndDeploy', parameters: [string(name: 'Build_ID', value: "${tag}"), string(name: 'ENV_Name', value: 'SOA_Dev'), [$class: 'ExtendedChoiceParameterValue', name: 'Deployment_History', value: '']]
    
        
    }
    

}
