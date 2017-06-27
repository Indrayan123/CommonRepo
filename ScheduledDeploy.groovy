import java.io.File 
node('master')
{
    def file12=env.MasterRevLocation+File.separator+"Revision.txt"
	def RevisionMethods = ""
    // echo "${file12}"
    if (fileExists("${file12}")) 
		{
		echo 'File Exists'
		} else
		{
		echo 'File Does not Exists'
		}
 dir(path: env.MasterProjectLocation)
	 {
 		RevisionMethods = load("ScheduleRevision.groovy")
	 }
def buildname =	RevisionMethods.readFile("${file12}","${EnvConfig}")
echo "${buildname}"
def intrmBuildFolder="${buildname}".substring(0,"${buildname}".lastIndexOf(","))
def BuildFolder ="${intrmBuildFolder}".substring("${intrmBuildFolder}".lastIndexOf(",")+1)
echo "${BuildFolder}"
    def stashbuildpath = env.MasterBuildLocation +'\\'+ "${BuildFolder}"
    echo "${stashbuildpath}"
    env.BuildFolder1="${BuildFolder}"
    stage('Stashing Build Folder')
    {
        dir (path: "${stashbuildpath}")	
        {
	    stash includes: '**', name: 'BuildResources'
		}
	 }
}
node(env.label)
{
 def stashbuildfolder = env.StashFolderName+'/'+env.BuildFolder1
	stage('UnStashing Build Folder') 
	{
        dir(path: "${stashbuildfolder}")
        {
		unstash 'BuildResources'
        }
	}
	stage('Get Env properties And Deploy the Build')
	{
	echo env.BuildFolder1
    echo "${env.EnvConfig}"
    echo "${env.WARDeployscript}"
    
    soat3urlprop ='SOAT3URL_'+env.EnvConfig
    soauserprop ='SOAUSER_'+env.EnvConfig
    soapassprop ='SOAPASS_'+env.EnvConfig
  
    def soat3url = env."${soat3urlprop}"
    def soapass = env."${soapassprop}"
    def soauser = env."${soauserprop}"
	def deployscript = env.WARDeployscript
	def buildworkspace = pwd()+'/'+stashbuildfolder
//	echo soat3url:+"${soat3url}"
	def scriptOp =(sh script: 'java -classpath "/opt/oracle/middleware/wlserver/server/lib/weblogic.jar:/opt/oracle/middleware/wlserver/modules/features/wlst.wls.classpath.jar" weblogic.WLST'+' '+deployscript+' '+soauser+' '+soapass+' '+soat3url+' '+buildworkspace , returnStatus: true)
		echo "${scriptOp}"	
    }  
	

}

node('master')
{
    	stage("Mark Completion of Deployment")
	{
	    def file12=env.MasterRevLocation+File.separator+"Revision.txt"
		 def RevisionMethods = ""
	   dir(path: env.MasterProjectLocation)
	   {
         RevisionMethods = load("ScheduleRevision.groovy")
	   }
	    def buildname =	RevisionMethods.readFile("${file12}","${EnvConfig}")
        echo "${buildname}"
        def intrmBuildFolder="${buildname}".substring(0,"${buildname}".lastIndexOf(","))
        def BuildFolder ="${intrmBuildFolder}".substring("${intrmBuildFolder}".lastIndexOf(",")+1)
	   
	    RevisionMethods.parseFile(file12,EnvConfig,BuildFolder,"C")
	}
}
