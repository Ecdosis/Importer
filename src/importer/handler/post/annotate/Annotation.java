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
import calliope.core.constants.JSONKeys;
import org.json.simple.JSONObject;
import importer.exception.HTMLException;
import org.xml.sax.*;
import org.json.simple.*;
import importer.handler.post.stages.Splitter;
/**
 * Represent an annotation in an MVD
 * @author desmond
 */
public class Annotation {
    int offset;
    int length;
    String resp;
    boolean link;
    StringBuilder body;
    
    /**
     * Create a basic annotation - fill in details later
     * @param offset the offset in the underlying text
     * @param resp the person who wrote it (abbreviated)
     * @param link true if this is a link, else it's a plain annotation
     * @param length the length of the annotated text
     */
    public Annotation( int offset, String resp, 
        boolean link, boolean propagateDown, int length )
    {
        this.offset = offset;
        this.resp = resp;
        this.link = link;
        this.length = length;
    }
    /**
     * Add some text to the annotation body.
     * @param text probably HTML
     */
    public void addToBody( String text )
    {
        if ( this.body == null )
            this.body = new StringBuilder(text);
        else
            this.body.append( text );
    }
    private void appendInt( StringBuilder sb, String label, int value )
    {
        sb.append("\"");
        sb.append(label);
        sb.append("\": ");
        sb.append(value);     
    }
    private void appendBoolean( StringBuilder sb, String label, boolean value )
    {
        sb.append("\"");
        sb.append(label);
        sb.append("\": ");
        sb.append(value);
    }
    private void appendString( StringBuilder sb, String label, String value )
    {
        sb.append("\"");
        sb.append(label);
        sb.append("\": \"");
        sb.append(value);  
        sb.append("\"");
    }
    /**
     * Crude XML to HTML converter
     * @param xml the xml to convert
     * @return HTML
     */
    private String toHTML( String xml, JSONObject config ) throws HTMLException
    {
        HTMLConverter hc = new HTMLConverter((JSONArray)config.get("patterns"));
        //xml = xml.replaceAll("&","&amp;");
        return hc.convert(xml);
    }
    /**
     * Build an annotation for the database
     * @param config the loaded xml->html configuration from the database
     * @return a JSON string
     */
    public String toJSONString( JSONObject config ) throws HTMLException
    {
        JSONObject jObj = new JSONObject();
        jObj.put(JSONKeys.BODY,toHTML("<note>"+this.body+"</note>",config));
        jObj.put(JSONKeys.OFFSET, offset );
        jObj.put( JSONKeys.LEN, length );
        jObj.put( JSONKeys.AUTHOR, resp );
        jObj.put(JSONKeys.LINK, link );
        return jObj.toJSONString();
    }
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("{ ");
        appendInt(sb, "offset", this.offset );
        sb.append(", ");
        appendInt(sb,"length", this.length );
        sb.append(", ");
        appendString( sb, "resp", resp );
        sb.append(", ");
        appendBoolean( sb, "link", link );
        sb.append(", ");
        appendString(sb,"body", body.toString());
        sb.append(" }");
        return sb.toString();
    }
    /**
     * Add a start tag to the note body
     * @param localName the name of the element
     * @param atts its attributes
     */
    public void addStartTag( String localName, Attributes atts )
    {
        if ( this.body==null )
            this.body = new StringBuilder();
        this.body.append("<");
        this.body.append(localName);
        for ( int i=0;i<atts.getLength();i++ )
        {
            if ( !atts.getLocalName(i).equals(Splitter.DONE) )
            {
                this.body.append(" ");
                this.body.append( atts.getLocalName(i) );
                this.body.append( "=\"" );
                this.body.append( atts.getValue(i) );
                this.body.append( "\"" );
            }
        }
        this.body.append(">");
    }
    public void addEndTag( String localName )
    {
        this.body.append("</");
        this.body.append(localName);
        this.body.append(">");
    }
}
