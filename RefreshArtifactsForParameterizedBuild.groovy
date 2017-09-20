node('master'){
    
    stage ('Git checkout'){
        sh("rm -rf *")
        git 'https://github.com/Indrayan123/CommonRepo.git'
    }

    stage('Refresh Groovy Script To Master'){
        
        dir(path: "${env.GroovyArtifactGitFolder}")
		{
		    
		stash includes: '**', name: 'GroovyScripts'
		}
		dir(path: "${env.MasterGroovyScriptLoc}") 
      {
           
		unstash 'GroovyScripts' 
        }
        
    }
    stage('Refresh Utilites To Master'){
        
        dir(path: "${env.UtilitiesGitFolder}")
		{
		    
		stash includes: '**', name: 'Utilites'
		}
		dir(path: "${env.MasterUtilitesLoc}") 
      {
           
		unstash 'Utilites' 
        }
        
    }
}