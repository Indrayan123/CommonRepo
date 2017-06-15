#!groovy
node(env.label){
    stage('SVN Checkout') {        
        git env.GitHubRepoURL

    }       


    stage('Compile&Deploy') {
	script{
	echo "Running Maven"
    dir(path: env.Subdirectory) {
    sh ('#!/bin/sh -e\n'+ "/opt/oracle/middleware/oracle_common/modules/org.apache.maven_3.2.5/bin/mvn pre-integration-test -P dev")
	    }
	}
}

}