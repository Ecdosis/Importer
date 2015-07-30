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
package importer.handler.post.importer;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import calliope.core.Utils;
import calliope.core.constants.JSONKeys;
import org.json.simple.JSONObject;

/**
 * Wrap a raw file in a JSON file with key-value pairs
 * @author desmond
 */
public class JDocWrapper 
{
    String[] okKeys = {JSONKeys.FORMAT,JSONKeys.VERSION1,JSONKeys.AUTHOR,
        JSONKeys.TITLE,JSONKeys.STYLE,JSONKeys.SECTION };
    JSONObject jdoc;
    /**
     * Is the json document key acceptable?
     * @param key the key to test
     * @return 1 if it is
     */
    private boolean isValidKey( String key )
    {
        for ( int i=0;i<okKeys.length;i++ )
            if ( okKeys[i].equals(key) )
                return true;
        return false;
    }
    /**
     * Construct the wrapper
     * @param body the raw data 
     * @param params a map of key-value pairs
     */
    public JDocWrapper( String body, Map<String,String> params )
    {
        jdoc = new JSONObject();
        jdoc.put( JSONKeys.BODY, Utils.cleanCR(body,false) );
        Set<String> keys = params.keySet();
        Iterator<String> iter = keys.iterator();
        while ( iter.hasNext() )
        {
            String key = iter.next();
            String value = params.get(key);
            if ( isValidKey(key) )
            {
                jdoc.put( key, Utils.cleanCR(value,false) );
            }
        }
    }
    /**
     * Convert the document to a string
     * @return a string
     */
    @Override
    public String toString()
    {
        return jdoc.toString();
    }
}
