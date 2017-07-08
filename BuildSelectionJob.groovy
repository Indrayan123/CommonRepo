import java.io.File 
node('master')
{
	def RevisionMethods = ""
    dir(path: env.MasterProjectLocation)
	{
         RevisionMethods = load("ProcessRevisionFile.groovy")
	}
        
    def file12=env.MasterDeploymentDetailsLocation+File.separator+"Revision.txt"
     
    
		
	RevisionMethods.parseRevisionFile(file12,EnvConfig,BuildFolder,"P")    
}
