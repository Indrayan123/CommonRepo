import java.io.File 
node('master')
{
    def RevisionFile=env.MasterRevLocation+File.separator+"Revision.txt"
	def RevisionMethods = ""
   
 dir(path: env.MasterProjectLocation)
	 {
 		RevisionMethods = load("ProcessRevisionFile.groovy")
	 }
def buildname =	RevisionMethods.readRevisionFile("${RevisionFile}","${EnvConfig}")
echo "${buildname}"
def intrmBuildFolder="${buildname}".substring(0,"${buildname}".lastIndexOf(","))
def BuildFolder ="${intrmBuildFolder}".substring("${intrmBuildFolder}".lastIndexOf(",")+1)
echo "${BuildFolder}"
    def stashbuildpath = env.MasterBuildLocation +'\\'+ "${BuildFolder}"
    echo "${stashbuildpath}"
    env.EnvBuildFolder="${BuildFolder}"
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
 def stashbuildfolder = env.StashFolderName+'/'+env.EnvBuildFolder
	stage('UnStashing Build Folder') 
	{
        dir(path: "${stashbuildfolder}")
        {
		unstash 'BuildResources'
        }
	}
	stage('Get Env properties And Deploy the Build')
	{
	echo env.EnvBuildFolder
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
		
	def scriptOp =sh (script: 'java -classpath "/opt/oracle/middleware/wlserver/server/lib/weblogic.jar:/opt/oracle/middleware/wlserver/modules/features/wlst.wls.classpath.jar" weblogic.WLST'+' '+deployscript+' '+soauser+' '+soapass+' '+soat3url+' '+buildworkspace , returnStatus: true)
		
		
		echo "scriptOp:${scriptOp}"
		
		if(scriptOp!= 0)
		{
			
			node('master')
{
    	stage("Mark Failure of Deployment")
	{
	    def RevisionFile=env.MasterRevLocation+File.separator+"Revision.txt"
		 def RevisionMethods = ""
	   dir(path: env.MasterProjectLocation)
	   {
         RevisionMethods = load("ProcessRevisionFile.groovy")
	   }
	  /*  def buildname =	RevisionMethods.readRevisionFile("${RevisionFile}","${EnvConfig}")
        echo "${buildname}"
        def intrmBuildFolder="${buildname}".substring(0,"${buildname}".lastIndexOf(","))
        def BuildFolder ="${intrmBuildFolder}".substring("${intrmBuildFolder}".lastIndexOf(",")+1)*/
		
	def  BuildFolder=env.EnvBuildFolder
	   
	    RevisionMethods.parseRevisionFile(RevisionFile,EnvConfig,BuildFolder,"F")
	}
}
			error "Deployment Failed Please Check Logs..."
		}
    }  
	

}

node('master')
{
    	stage("Mark Completion of Deployment")
	{
	    def RevisionFile=env.MasterRevLocation+File.separator+"Revision.txt"
		 def RevisionMethods = ""
	   dir(path: env.MasterProjectLocation)
	   {
         RevisionMethods = load("ProcessRevisionFile.groovy")
	   }
	    def buildname =RevisionMethods.readRevisionFile("${RevisionFile}","${EnvConfig}")
        echo "${buildname}"
        def intrmBuildFolder="${buildname}".substring(0,"${buildname}".lastIndexOf(","))
        def BuildFolder ="${intrmBuildFolder}".substring("${intrmBuildFolder}".lastIndexOf(",")+1)
	   
	    RevisionMethods.parseRevisionFile(RevisionFile,EnvConfig,BuildFolder,"C")
	}
}
