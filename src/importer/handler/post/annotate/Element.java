/* This file is part of Importer.
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
 */
package importer.handler.post.annotate;
import java.util.ArrayList;
import org.xml.sax.*;


/**
 * Mimic an XML element
 * @author desmond
 */
public class Element 
{
    ArrayList<Attribute> attributes;
    String tagName;
    Element(String tagName)
    {
        this.tagName = tagName;
    }
    void addAttribute( String name, String value )
    {
        Attribute attr = new Attribute(name,value);
        if ( attributes == null )
            attributes = new ArrayList<Attribute>();
        attributes.add( attr );
    }
    /**
     * A match occurs if all our attributes are found in the XML element
     * @param localName the local name of the xml tag
     * @param atts its attributes
     * @return true if it matched
     */
    public boolean matches( String localName, Attributes atts )
    {
        boolean res = localName.equals(this.tagName);
        if ( res && attributes != null )
        {
            for ( int i=0;i<attributes.size();i++ )
            {
                Attribute attr = attributes.get(i);
                int j;
                for ( j=0;j<atts.getLength();j++ )
                {
                    if ( attr.name.equals(atts.getLocalName(j))
                        &&attr.value!=null
                        &&attr.value.equals(atts.getValue(j)) )
                        break;
                }
                if ( j==atts.getLength() )
                {
                    res = false;
                    break;
                }
            }
        }
        return res;
    }
    boolean isBetterThan( Element elem )
    {
        int nAtts = (attributes==null)?0:attributes.size();
        int elemAtts = (elem.attributes==null)?0:elem.attributes.size();
        return elemAtts > nAtts;
    }
}
