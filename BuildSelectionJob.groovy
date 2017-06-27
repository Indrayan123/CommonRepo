import java.io.File 
node('master')
{
    
        def RevisionMethods = load("ScheduleRevision.groovy")
        
    def file12=env.MasterDeploymentDetailsLocation+File.separator+"Revision.txt"
     echo "${file12}"
    if (fileExists("${file12}")) 
		{
		echo 'File Exists'
		} else
		{
		echo 'File Does not Exists'
		}
		
	RevisionMethods.parseFile(file12,EnvConfig,BuildFolder,"P")    
}
