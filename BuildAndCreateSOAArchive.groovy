#!groovy
node(env.label){
	
    stage('GIT Source Code Download') {
         git env.GitURL
    }       
    stage('SOA Archive Creation') {
	script{
	echo "Running Maven"	 
    dir(path: env.Subdirectory) {
    sh ('#!/bin/sh -e\n'+ "/opt/oracle/middleware/oracle_common/modules/org.apache.maven_3.2.5/bin/mvn package")		
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
node(env.label){
stage('Clean Directory') {
		 echo "Cleaning Directory: ${pwd()}"
         sh ("rm -rf *")
    }
}	
