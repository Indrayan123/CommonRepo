import java.io.File

node('master') 
{ 																													// Being executed in Master node
	def RevisionFile = env.MasterRevLocation + File.separator + "Revision.txt" 										// Set Revision File absolute path
	def RevisionMethods = "" 																						// Declare RevisionMethods to load groovy file
																		
	dir(path: env.MasterProjectLocation) { 																			// Traversing to the workspace  Master node
	RevisionMethods = load("ProcessRevisionFile.groovy")}												 			// Initialize RevisionMethods variable
	def buildname = RevisionMethods.readRevisionFile("${RevisionFile}", "${EnvConfig}") 							// Set the buildname variable
	def intrmBuildFolder = "${buildname}".substring(0, "${buildname}".lastIndexOf(",")) 							// Temp vaiable to create Build Folder
	env.BuildFolder = "${intrmBuildFolder}".substring("${intrmBuildFolder}".lastIndexOf(",") + 1) 					// Set the BuildFolder
	def stashbuildpath = env.MasterBuildLocation + '\\' + "${env.BuildFolder}" 										// Set the path for stashing project artifacts
	
	def soat3urlprop = 'SOAT3URL_' + "${EnvConfig}" 																// Set Property variable name based on Env selected
	def soauserprop = 'SOAUSER_' + "${EnvConfig}" 																	// Set Property variable name based on Env selected
	def soapassprop = 'SOAPASS_' + "${EnvConfig}" 																	// Set Property variable name based on Env selected
	env.soat3url = env. "${soat3urlprop}" 																			// Retrieve env specific App Server url
	env.soapass = env."${soapassprop}"  																		   	// Retrieve env specific Server password
	env.soauser = env."${soauserprop}" 																		   		// Retrieve env specific Server Username

	stage('Stashing Build Folder')
	{
		dir(path: "${stashbuildpath}") {
		stash includes: '**', name: 'BuildResources'} 																	// Stash all content under project folder
	}
}
node(env.label)  				 																						// Being executed in Slave
{ 
 	def unstashbuildfolder = env.StashFolderName + '/' + env.BuildFolder 					   							// Initialize stash Folder variable
	stage('UnStashing Build Folder')
	{
		dir(path: "${unstashbuildfolder}") {
		unstash 'BuildResources'}  																		                // unstashing the content to the slave node
	}
	
	stage('Get Env properties And Deploy the Build') 
	{
		def deployscript = env.WARDeployscript															                // Initialize Deployment script
		def buildworkspace = pwd() + '/' + unstashbuildfolder															// Initialize buildworkspace variable
		def scriptOp = sh(script: 'java -classpath "/opt/oracle/middleware/wlserver/server/lib/weblogic.jar:/opt/oracle/middleware/wlserver/modules/features/wlst.wls.classpath.jar" weblogic.WLST' + ' ' + deployscript + ' ' + env.soauser + ' ' + env.soapass + ' ' + env.soat3url + ' ' + buildworkspace, returnStatus: true) 
																														//Deploying the war to the application server & capturing response 

		if (scriptOp == 0) {  																		             	    //if ScriptOp is 0 , means deployment successful
			env.Status = "Success"
		} else {
			env.Status = "Failure"  																		           //else deployment is failure
		}
	}
}
node('master') 
{
	stage("Mark Deployment") 
	{
		def RevisionFile = env.MasterRevLocation + File.separator + "Revision.txt" 										// Set Revision File absolute path
		def RevisionMethods = "" 																						// Declare RevisionMethods to load groovy file
		dir(path: env.MasterProjectLocation) { 																			// Traversing to the workspace  Master node
		RevisionMethods = load("ProcessRevisionFile.groovy")}												 			// Initialize RevisionMethods variable

		echo "script output: ${env.Status} #"
		if (env.Status == "Success") 												 									//If status is Success
		{
			RevisionMethods.parseRevisionFile(RevisionFile, EnvConfig, env.BuildFolder, "C")							//updating the revision.txt file's entry status with "C" as completed
		} else {
			RevisionMethods.parseRevisionFile(RevisionFile, EnvConfig, env.BuildFolder, "F") 							//updating the revision.txt file's entry status with "F" as failure
			error "Deployment Failed Please Check Logs..."
		}
	}
}
