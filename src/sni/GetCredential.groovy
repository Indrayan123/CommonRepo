package sni;

import java.util.Base64

class GetCredential {

public static String decryptPassword(String paramString)      
    {	  
	  byte[] passByte = Base64.getDecoder().decode(paramString)   
      
      return new String(passByte)
    }

}
