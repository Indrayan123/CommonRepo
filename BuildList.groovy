import groovy.io.FileType

def list = []

def dir = new File("C:\\Program Files (x86)\\Jenkins\\workspace\\MavenJavaDeploymentUsingGitHub\\BuildAndCreateWarArchive\\Builds")
dir.eachFile (FileType.DIRECTORIES) { file ->
  list << file
}
def result = list.sort{ a,b -> b.lastModified() <=> a.lastModified() }*.name
def resultlength =result.size
def result2 =[]
if( resultlength > 5)
result2 = result [0..4]
if (resultlength ==0)
{
result2 =["No Folders To Display"]
}
else
result2 = result [0..(resultlength-1)]

return result2
