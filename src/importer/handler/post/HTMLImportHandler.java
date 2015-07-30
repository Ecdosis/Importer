/*
 * This file is part of Importer.
 *
 *  Importer is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  Importer is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Importer.  If not, see <http://www.gnu.org/licenses/>.
 *  (c) copyright Desmond Schmidt 2015
 */

package importer.handler.post;

import importer.constants.Config;
import calliope.core.constants.Formats;
import importer.exception.ImporterException;
import importer.handler.ImporterHandler;
import importer.handler.post.stages.StageOne;
import importer.handler.post.stages.Stage3HTML;
import importer.handler.post.stages.StageTwo;
import importer.Archive;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

/**
 * Handle importation of a set of plain text files from a tool like psef-tool.
 * @author desmond 23-7-2012
 */
public class HTMLImportHandler extends ImporterPostHandler
{
    String style;
    HTMLImportHandler()
    {
        super();
        style = "TEI/default";
    }
    public void handle( HttpServletRequest request, 
        HttpServletResponse response, String urn ) throws ImporterException
    {
        try
        {
            if (ServletFileUpload.isMultipartContent(request) )
            {
                parseImportParams( request );
                Archive cortex = new Archive(getWork(), 
                    getAuthor(),Formats.TEXT,encoding);
                Archive corcode = new Archive(getWork(), 
                    getAuthor(),Formats.STIL,encoding);
                cortex.setStyle( style );
                corcode.setStyle( style );
                StageOne stage1 = new StageOne( files );
                log.append( stage1.process(cortex,corcode) );
                if ( stage1.hasFiles() )
                {
                    String suffix = "";
                    StageTwo stage2 = new StageTwo( stage1, false );
                    stage2.setEncoding( encoding );
                    log.append( stage2.process(cortex,corcode) );
                    Stage3HTML stage3Html = new Stage3HTML( stage2,
                        style, dict, hhExceptions, encoding );
                    if ( stripperName==null || stripperName.equals("default") )
                        stripperName = "html";
                    stage3Html.setStripConfig( 
                        getConfig(Config.stripper,stripperName) );
                    log.append( stage3Html.process(cortex,corcode) );
                    addToDBase( cortex, "cortex", suffix );
                    addToDBase( corcode, "corcode", suffix );
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
