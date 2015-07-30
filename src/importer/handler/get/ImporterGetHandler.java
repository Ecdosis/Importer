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
import calliope.core.Utils;
import calliope.core.database.Connector;
import importer.constants.Service;
import calliope.core.constants.Database;
import calliope.core.constants.Formats;
import calliope.core.constants.JSONKeys;
import importer.handler.EcdosisMVD;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

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
            String prefix = Utils.first( urn );
            urn = Utils.pop(urn);
            if ( prefix != null )
            {
                if ( prefix.equals(Service.JSON) )
                    new ImporterJSONHandler().handle( request, response, urn );
                else if ( prefix.equals(Service.COLLECTION) )
                    new ImporterListCollectionHandler().handle( request, response, 
                        urn );
            }
            else
                throw new ImporterException("Invalid urn (prefix was null) "+urn );
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
    /**
     * Fetch and load an MVD
     * @param db the database 
     * @param docID
     * @return the loaded MVD
     * @throws an ImporterException if not found
     */
    protected EcdosisMVD loadMVD( String db, String docID ) 
        throws ImporterException
    {
        try
        {
            String data = Connector.getConnection().getFromDb(db,docID);
            if ( data.length() > 0 )
            {
                JSONObject doc = (JSONObject)JSONValue.parse(data);
                if ( doc != null )
                    return new EcdosisMVD( doc );
            }
            throw new ImporterException( "MVD not found "+docID );
        }
        catch ( Exception e )
        {
            throw new ImporterException( e );
        }
    }
    protected String getVersionTableForUrn( String urn ) throws ImporterException
    {
        try
        {
            JSONObject doc = loadJSONDocument( Database.CORTEX, urn );
            String fmt = (String)doc.get(JSONKeys.FORMAT);
            if ( fmt != null && fmt.startsWith(Formats.MVD) )
            {
                EcdosisMVD mvd = loadMVD( Database.CORTEX, urn );
                return mvd.mvd.getVersionTable();
            }
            else if ( fmt !=null && fmt.equals(Formats.TEXT) )
            {
                // concoct a version list of length 1
                StringBuilder sb = new StringBuilder();
                String version1 = (String)doc.get(JSONKeys.VERSION1);
                if ( version1 == null )
                    throw new ImporterException("Lacks version1 default");
                sb.append("Single version\n");
                String[] parts = version1.split("/");
                for ( int i=0;i<parts.length;i++ )
                {
                    sb.append(parts[i]);
                    sb.append("\t");
                }
                sb.append(parts[parts.length-1]+" version");
                sb.append("\n");
                return sb.toString();
            }
            else
                throw new ImporterException("Unknown of null Format");
        }
        catch ( Exception e )
        {
            throw new ImporterException(e);
        }   
    }
    /**
     * Use this method to retrieve the doc just to see its format
     * @param db the database to fetch from
     * @param docID the doc's ID
     * @return a JSON doc as returned by Mongo
     * @throws ImporterException 
     */
    JSONObject loadJSONDocument( String db, String docID ) 
        throws ImporterException
    {
        try
        {
            String data = Connector.getConnection().getFromDb(db,docID);
            if ( data.length() > 0 )
            {
                JSONObject doc = (JSONObject)JSONValue.parse(data);
                if ( doc != null )
                    return doc;
            }
            throw new ImporterException( "Doc not found "+docID );
        }
        catch ( Exception e )
        {
            throw new ImporterException( e );
        }
    }     
}
