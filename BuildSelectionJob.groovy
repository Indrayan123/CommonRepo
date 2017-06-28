import java.io.File 
node('master')
{
	def RevisionMethods = ""
    dir(path: env.MasterProjectLocation)
	{
         RevisionMethods = load("ProcessRevisionFile.groovy")
	}
        
    def file12=env.MasterDeploymentDetailsLocation+File.separator+"Revision.txt"
     echo "${file12}"
    if (fileExists("${file12}")) 
		{
		echo 'File Exists'
		} else
		{
		echo 'File Does not Exists'
		}
		
	RevisionMethods.parseRevisionFile(file12,EnvConfig,BuildFolder,"P")    
}
