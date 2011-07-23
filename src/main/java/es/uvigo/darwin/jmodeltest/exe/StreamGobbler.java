/** 
 * RunTed.java
 *
 * Description:		Streams to a job
 * @author			David Posada, University of Vigo, Spain  
 *					dposada@uvigo.es | darwin.uvigo.es
 * @version			1.0 (May 2006)
 */

package es.uvigo.darwin.jmodeltest.exe;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;



class StreamGobbler extends Thread
{
	InputStream is;
	String type;
	OutputStream os;


    StreamGobbler(InputStream is, String type)
    {
        this(is, type, null);
    }

    StreamGobbler(InputStream is, String type, OutputStream redirect)
    {
        this.is = is;
        this.type = type;
        this.os = redirect;
    }
  
  
    public void run()
    {
        try
        {
            PrintWriter pw = null;
            if (os != null)
                pw = new PrintWriter(os);
                
			InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line=null;
            while ( (line = br.readLine()) != null)
				{
                if (pw != null)
                    pw.println(line);
                else
                	System.out.println(type + ">" + line);    
				}
            if (pw != null)
                pw.flush();
        } 
		catch (IOException ioe)
           {
            ioe.printStackTrace();  
            }
    }
}


	

	