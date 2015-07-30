/* This file is part of calliope.
 *
 *  calliope is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  calliope is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with calliope.  If not, see <http://www.gnu.org/licenses/>.
 */

package importer.handler.post;

import calliope.core.database.Connector;
import calliope.core.image.Corpix;
import importer.exception.ImporterException;
import importer.handler.post.importer.JDocWrapper;
import importer.handler.post.stages.ImageFile;
import importer.handler.post.stages.File;
import calliope.core.constants.Database;
import importer.ImporterWebApp;
import calliope.core.Utils;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
/**
 * Handler for raw files
 * @author desmond 28/7/2012
 */
public class UploadHandler extends ImporterPostHandler
{
    @Override
    public void handle( HttpServletRequest request, 
        HttpServletResponse response, String urn ) throws ImporterException
    {
        try
        {
            database = Utils.first(urn);
            if (ServletFileUpload.isMultipartContent(request) )
            {
                parseImportParams( request );
                for ( int i=0;i<images.size();i++ )
                {
                    ImageFile iFile = images.get(i);
                    Corpix.addImage( ImporterWebApp.webRoot, docid, 
                        iFile.getType(), iFile.getData() );
                }
                if ( Connector.getConnection()== null)
                    System.out.println("connection was null");
                if ( this.docid == null )
                    System.out.println("docIDwasnull");
                for ( int i=0;i<files.size();i++ )
                {
                    String resp = "";
                    File file = files.get(i);
                    if ( file instanceof File )
                    {
                        // wrap cortex andkill -9 8220 corcodes with kosher params
                        String json = file.data;
                        if ( database.equals(Database.CORTEX)
                        || database.equals(Database.CORCODE) 
                        || database.equals(Database.MISC) )
                        {
                            JDocWrapper wrapper = new JDocWrapper( 
                                json, jsonKeys );
                            json = wrapper.toString();
                        }
                        else if ( database.equals(Database.CORFORM)
                            || database.equals(Database.CONFIG)
                            || database.equals(Database.PARATEXT))
                            json = Utils.cleanCR( json, true );
                        resp = Connector.getConnection().putToDb( 
                            database, docid, json );
                    }
                    log.append( resp );
                }
                response.setContentType("text/html;charset=UTF-8");
                response.getWriter().println( wrapLog() );
            } 
        }
        catch ( Exception e )
        {
            throw new ImporterException( e );
        }
    }
}
 