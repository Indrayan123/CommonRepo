node(env.label){
	
    stage('GIT Source Code Download') {
         git env.GitURL
    }       
    stage('JAVA WAR Creation') {
	script{
	echo "Running Maven"
		//buildloc =env.buildlocation
		buildloc = "../Builds/"+"R1_"+env.BUILD_TIMESTAMP+"_"+env.BUILD_ID
		echo "currentpath:${pwd()}"
		echo "buildlocation:${buildloc}"
    dir(path: env.Subdirectory) {
  //  sh ('#!/bin/sh -e\n'+ "/opt/oracle/middleware/oracle_common/modules/org.apache.maven_3.2.5/bin/mvn package")
	    echo "path:${pwd()}"
	    sh ('#!/bin/sh -e\n'+ "/opt/jenkins/agent/scm/scripts/WarCompilation.sh"+" "+pwd()+" "+"${buildloc}")
	}
	}
	}
	stage('Stashing Build Folder'){
        dir (path: env.StashFolderName)	{
	    stash includes: '**', name: 'BuildResources'
		}
	}
	
}
node('master'){
    stage('UnStashing Build Folder') {
        dir(path: env.MasterBuildLocation){
		unstash 'BuildResources'
    }
}
}
/*node(env.label){
stage('Clean Directory') {
		 echo "Cleaning Directory: ${pwd()}"
         sh ("rm -rf *")
    }
}*/
