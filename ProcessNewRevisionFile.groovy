#!groovy
/*********
parseRevisionFile
Signature: 
fName	=> The absolute Path of Project specific 
env 	=> The env(DEV,SYS,UAT,PRD etc) where the application needs to be deployed
buildTag=> The Build Number which needs to be deployed
Status	=> Status initally will be updated as 'P' for to be Processed
Purpose:
This methods first checks the existence of Project specific Revision.txt file.
In case file exists, it will check for any env(passed as argument) specific entry in the file, if so will replace the entry.Else will make new entry.
In case file is non-existant, it will create the Revision.txt first . Then it will make new entry with required details.
Revision.txt entry format: $Env, $BuildNo, $StatusFlag (e.g. DEV,R1_2017-06-23-5,P)
*********/
@NonCPS
def parseRevisionFile(String fName,String buildTag,String env,String Last_Deploy_Timestamp,String Status,String Tag_Comment) 
{
    ArrayList fileContent = null  										  // Declare ArrayList to read Revision.txt
    PrintWriter writer = null     						                  // Declare File Write
    File f=new File("${fName}")   						                  // Instantiate Application specific Revision file
    println "${f}"+" : "+"${env}"+" : "+ "${buildTag}"
	if (f.length() > 0)									                  // Check if file exists
	{
		println "File exists"
		fileContent = new ArrayList()					                  // Initialize Arralist
		f.eachLine {  line ->fileContent.add(line) }	                  // Read eachline of Revision.txt & populate in Arralist
		i = fileContent.iterator()						// Initialize iterator for updated Arraylist
		String[] RevDetails = null
		while (i.hasNext()) 						                      // Iterate through ArrayList
		{
			String eachline=i.next()
			println "eachline"+":"+eachline
			RevDetails = eachline.split(",")
			if (eachline.toUpperCase().contains("${buildTag}"))                // Check if any existing entry for provided Env in ArrayList
			{
				Tag_Comment=RevDetails[4] 
				if(eachline.contains("${env}"))
				{
				println "inside if"
				i.remove()
				}
				                            // In case entry exist, delete the same
			}
		}
		fileContent.add(0,"${buildTag}"+","+"${env}"+","+"${Last_Deploy_Timestamp}"+","+"${Status}"+","+"${Tag_Comment}") // Add a fresh entry in Arraylist with Env, BuildNo details & Status as P(Pending)
		println fileContent
		writer = new PrintWriter(f)						                  // Initialize File Write

		for (String item : fileContent)				                      // Iterate through ArrayList(This time to write the content)
		{
			//println item
			writer.println(item)			                              // Write the updated content( with selected Build No & Env) in Revision.txt
		}
		writer.close()						                    		  // Close file Writer
	}
	else{										                  		  // In case File doesnot exist
		println "File does not exists"
		boolean dirCreated = f.getParentFile().mkdirs();                  // Create Directory( if required)
		println "${dirCreated}"
		f.setExecutable(true, false);					                  // Set permission of Directory
		boolean bool = f.createNewFile();				            	  // Create Revision.txt file( if required)
        f.setExecutable(true, false);						              // Set permission of File
		fileContent = new ArrayList()					            	  // Initialize Arralist
		fileContent.add(0,"${buildTag}"+","+"${env}"+","+"${Last_Deploy_Timestamp}"+","+"${Status}"+","+"${Tag_Comment}")    // Add a fresh entry in Arraylist with Env, BuildNo details & Status as P(Pending)
		writer = new PrintWriter(f)										  // Initialize File Write
		fileContent.each 	{ id -> writer.println(id) }            	  // Write the updated content( with selected Build No & Env) in Revision.txt
		writer.close()									            	  // Close file Writer
	}
	return "Entry made in Revision.txt"
}

/*********
readRevisionFile
Signature: 
fName	=> The absolute Path of Project specific 
env 	=> The env(DEV,SYS,UAT,PRD etc) where the application needs to be deployed
Purpose:
This methods first checks the existence of Project specific Revision.txt file.
In case file exists, it will check for any env(passed as argument) specific entry in the file, if so will it will return the entry.
if entry not found, same will be returned back to caller.
In case file is non-existant, it will create the Revision.txt first it will return same msg to caller process.
Revision.txt entry format: $Env, $BuildNo, $StatusFlag (e.g. DEV,R1_2017-06-23-5,P)
*********/
@NonCPS
def readRevisionFile(String fName,String env) 
{
    ArrayList fileContent = null  										  // Declare ArrayList to read Revision.txt
    PrintWriter writer = null     						                  // Declare File Write
    File f=new File("${fName}")   						                  // Instantiate Application specific Revision file
//	println "${env}"
//	println "${fName}"
	def j=0
	if (f.length() > 0)
	{
		fileContent = new ArrayList()					                  // Initialize Array list
		f.eachLine {  line ->fileContent.add(line) }	                  // Read each line of Revision.txt & populate in Array list
		i = fileContent.iterator()						                  // Initialize iterator for updated Array list
		while (i.hasNext()) 						                      // Iterate through ArrayList
		{
		    
			def lineContent=i.next().toUpperCase()			              // Define variable lineContent to read each line
		    //	println "${lineContent}"			                      
			if (lineContent.contains("${env}")) 			              // Check if env (passed as argument) specific entry exist or not in File
			{	j=1;
			 return lineContent}			                     		  // In case entry found, return the file line content
		}
		if(j==0)
		return "Error:No entry found corresponding to env: ${env}" 		      // In case entry not found 
	}
	else{
		println "File does not exists"
		return "Error:File does not exists" 								      // In case no entry found, return accordingly
	}
}
return this
