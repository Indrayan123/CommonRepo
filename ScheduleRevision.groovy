#!groovy
def parseFile(String fName,String env,String buildTag) 
{
    ArrayList pids = null
    PrintWriter writer = null
    File f=new File("${fName}")
    echo "${f}"
	echo "${env}"
	echo "${buildTag}"
	if (f.length() > 0)
	{
		pids = new ArrayList()
		f.eachLine {  line ->pids.add(line) }
		i = pids.iterator()
		while (i.hasNext()) 
		{
			if (i.next().toUpperCase().contains("${env}")) 
			{i.remove()}
		}
		pids.add(0,"${env}"+","+"${buildTag}"+","+"P")
      	println pids
		writer = new PrintWriter(f)
		pids.each 	{ id -> writer.println(id) }
		writer.close()
	}
else{
   println "File is empty!"
   boolean dirCreated = f.getParentFile().mkdirs();
   echo "${dirCreated}"
   f.setExecutable(true, false);
    boolean bool = f.createNewFile();
     f.setExecutable(true, false);
    pids = new ArrayList()
    pids.add(0,"${env}"+","+"${buildTag}"+","+"P")
    writer = new PrintWriter(f)
    pids.each 	{ id -> writer.println(id) }
    writer.close()
	}