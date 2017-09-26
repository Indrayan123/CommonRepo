import org.boon.Boon;
import groovy.json.*;
import groovy.io.*;
import sni.foundation.soa.jenkins.FetchApplTagDetails

def revfilepath= binding.variables.RevFilePath
def output = new FetchApplTagDetails().getTagDetailsFromRvsnFile("${revfilepath}","ALL");

def json = JsonOutput.toJson(output);

def jsonEditorOptions = Boon.fromJson(/{
        disable_edit_json: true,
        disable_properties: true,
        no_additional_properties: true,
        disable_collapse: true,
        disable_array_add: true,
        disable_array_delete: true,
        disable_array_reorder: true,
        theme: "bootstrap2",
        iconlib:"fontawesome4",
       "schema":{
 {
 
 "type": "array", 
 "format": "table",
"title":"DeploymentHistory",
    "items": {
      
        "type": "object",
        "title":"Details",
		"properties": {
      "Details": {
	    "title": "DeploymentHistory",
        "type": "array",
        "format": "table",
        "items": {
          "type": "object",
          "properties": {
		  "applicationtagname": {
			"title": "Application Tag Name",
              "type": "string",
"readOnly": "true"			  
            },
			"buildenv": {
			"title": "Env",
              "type": "string".
"readOnly": "true"			  
            },
            "deploytimestamp": {
			"title": "TimeStamp",
              "type": "string",
			  "readOnly": "true"
            },
"status": {
			"title": "Status",
              "type": "string",
			  "readOnly": "true"
            },
"tagcomment": {
			"title": "Tag Comment",
              "type": "string",
			  "readOnly": "true"
            }		
            
          }
        }
      }
    }
  }
  
},

  
startval: [
    {    
	
      "Details": ${json}
    }
  ] 
 
}/);

return jsonEditorOptions;
