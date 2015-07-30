/*
 * This file is part of Project.
 *
 *  Project is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  Project is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Project.  If not, see <http://www.gnu.org/licenses/>.
 *  (c) copyright Desmond Schmidt 2014
 */

package importer.handler.get;

import importer.exception.ImporterException;
import importer.handler.ImporterHandler;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Get a project document from the database
 * @author desmond
 */
public class ImporterGetHandler extends ImporterHandler
{
    public void handle(HttpServletRequest request,
            HttpServletResponse response, String urn) throws ImporterException 
    {
        try 
        {
            
        } 
        catch (Exception e) 
        {
            try
            {
                response.setCharacterEncoding("UTF-8");
                response.getWriter().println(e.getMessage());
            }
            catch ( Exception ex )
            {
                throw new ImporterException(ex);
            }
        }
    }   
}
