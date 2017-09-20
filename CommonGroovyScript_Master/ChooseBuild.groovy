import groovy.io.FileType
def list = []
def path= binding.variables.RevFilePath
def file = new File("${path}")
file.eachLine { String line ->
 list << line.tokenize(',')[0]
}
list=list.unique()
return list