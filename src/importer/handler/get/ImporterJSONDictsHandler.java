/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package importer.handler.get;
import importer.constants.Globals;
import calliope.AeseSpeller;
import calliope.core.Utils;
import importer.exception.ImporterException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Get a list of available dicts
 * @author desmond
 */
public class ImporterJSONDictsHandler extends ImporterGetHandler
{
    @Override
    public void handle( HttpServletRequest request, 
        HttpServletResponse response, String urn ) throws ImporterException
    {
        try
        {
            StringBuilder jdoc = new StringBuilder();
            jdoc.append("[ ");
            AeseSpeller aes = new AeseSpeller(Globals.DEFAULT_DICT);
            String[] dicts = aes.listDicts();
            for ( int i=0;i<dicts.length;i++ )
            {
                String[] parts = dicts[i].split("\t");
                if ( parts.length==2 )
                {
                    jdoc.append("  { ");
                    jdoc.append("\"language\": ");
                    jdoc.append("\"");
                    jdoc.append(Utils.languageName(parts[0]));
                    jdoc.append("\"");
                    jdoc.append(", ");
                    jdoc.append("\"code\": ");
                    jdoc.append("\"");
                    jdoc.append(parts[1]);
                    jdoc.append("\"");
                    jdoc.append(" }");
                    if ( i<dicts.length-1 )
                        jdoc.append(",\n");
                }
            }
            jdoc.append(" ]");
            aes.cleanup();
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().println( jdoc.toString() );
        }
        catch ( Exception e )
        {
            throw new ImporterException(e);
        }
    }
}
