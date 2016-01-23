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
package importer.handler.post.annotate;
import java.util.ArrayList;
import org.xml.sax.*;
/**
 * Translate one XML element to one HTML element
 * @author desmond
 */
public class HTMLPattern 
{
    String tagName;
    ArrayList<Attribute> attributes;
    /**
     * Set up a single XML element to HTML element translation
     * @param tagName the name of the HTML element
     */
    HTMLPattern( String tagName )
    {
        this.tagName = tagName;
    }
    /**
     * Add an attribute pair to translate from XML->HTML
     * @param old the XML attribute name
     * @param rep the HTML attribute name
     */
    void addAttribute( String old, String rep )
    {
        if ( attributes == null )
            attributes = new ArrayList<Attribute>();
        attributes.add( new Attribute(old, rep) );
    }
    /**
     * Get a start tag
     * @param atts the XMl attributes
     * @return a HTML string representing the start tag in HTML
     */
    String getStartTag( Attributes atts )
    {
        StringBuilder sb = new StringBuilder();
        sb.append("<");
        sb.append(tagName);
        if ( attributes != null )
        {
            for ( int i=0;i<attributes.size();i++ )
            {
                Attribute attr = attributes.get(i);
                sb.append(" ");
                sb.append(attr.name);
                sb.append("=\"");
                // copy xml-attribute value to htl-attribute
                // if attr.value is null
                if ( attr.value==null )
                {
                    for ( int j=0;j<atts.getLength();j++ )
                        if ( atts.getLocalName(j).equals(attr.name) )
                            sb.append(atts.getValue(j) );
                }
                else
                    sb.append(attr.value);
                sb.append("\"");
            }
        }
        sb.append(">");
        return sb.toString();
    }
    /**
     * Get the end-tag in HTML. Assume that we are not dropped
     * @return the HTML tag corresponding to this XML element
     */
    String getEndTag()
    {
        return "</"+tagName+">";
    }
}
