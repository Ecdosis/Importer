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
package importer.handler.get;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import importer.constants.*;
import importer.exception.*;
import calliope.core.Utils;
/**
 * Handle requests for JSON formatted data
 * @author desmond
 */
public class ImporterJSONHandler extends ImporterGetHandler
{
    /**
     * Get the JSON for the given path
     * @param request the request to read from
     * @param urn the original URN
     * @return a formatted html String
     */
    @Override
    public void handle( HttpServletRequest request, 
        HttpServletResponse response, String urn ) throws ImporterException
    {
        String first = Utils.first(urn);
        urn = Utils.pop(urn);
        if ( first.equals(Service.LIST))
            new ImporterJSONListHandler().handle(request,response,urn);
        else if ( first.equals(Service.DICTS) )
            new ImporterJSONDictsHandler().handle(request,response,urn);
        else
            throw new ImporterException("Unknown importer service: "+urn);
    }
}
