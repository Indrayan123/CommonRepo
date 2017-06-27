#!groovy
@NonCPS
def parseFile(String fName,String env,String buildTag,String Status) 
{
    ArrayList pids = null
    PrintWriter writer = null
    File f=new File("${fName}")
    echo "${f}"
	echo "${env}"
	echo "${buildTag}"
	if (f.length() > 0)
	{
		echo "inside if"
		pids = new ArrayList()
		f.eachLine {  line ->pids.add(line) }
		i = pids.iterator()
		while (i.hasNext()) 
		{
			if (i.next().toUpperCase().contains("${env}")) 
			{
				i.remove()
				echo "inside remove"
			}
		}
		pids.add(0,"${env}"+","+"${buildTag}"+","+"${Status}")
      		println pids
		writer = new PrintWriter(f)
		finali = pids.iterator()
		/*pids.each 	{
			id -> writer.println(id) 
			println id
		}*/
		/*while (finali.hasNext())
		{
		writer.println(finali.next())
		println finali.next()	
		}*/
		for (String item : pids)
		{
			println item
			writer.println(item)
		}
		writer.close()
	}
else{
	echo "inside else"
   println "File is empty!"
   boolean dirCreated = f.getParentFile().mkdirs();
   echo "${dirCreated}"
   f.setExecutable(true, false);
    boolean bool = f.createNewFile();
     f.setExecutable(true, false);
    pids = new ArrayList()
	pids.add(0,"${env}"+","+"${buildTag}"+","+"${Status}")
    writer = new PrintWriter(f)
    pids.each 	{ id -> writer.println(id) }
    writer.close()
	}
	return "file generated"
}
@NonCPS
def readFile(String fName,String env) 
{
    ArrayList pids = null
    PrintWriter writer = null
    File f=new File("${fName}")
//	echo "${env}"
//	echo "${fName}"
	if (f.length() > 0)
	{
		pids = new ArrayList()
		f.eachLine {  line ->pids.add(line) }
		i = pids.iterator()
		while (i.hasNext()) 
		{
		    def val=i.next().toUpperCase()
		    //	echo "${val}"
			if (val.contains("${env}")) 
			{return val}
		}
	}
else{
   println "File is empty!"
	return "File is empty!"
	}
}
return this
