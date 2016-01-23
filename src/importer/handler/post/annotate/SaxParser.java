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
import java.io.CharArrayReader;
import java.util.ArrayList;
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

/**
 * SAX parser for main body of xml. Extracts notes.
 * @author desmond
 */
public class SaxParser extends DefaultHandler
{
    SAXParser parser;
    XMLReader xmlReader;
    private ArrayList<Annotation> notes;
    private Annotation note;
    String vid;
    String docid;
    StringBuilder body;
    /** current position in file */
    private int pos;
    SaxParser( String docid, String vid, ArrayList<Annotation> notes ) throws Exception
    {
        try
        {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            //spf.;
            spf.setNamespaceAware(true);
            this.vid = vid;
            this.docid = docid;
            this.notes = notes;
            this.body = new StringBuilder();
            parser = spf.newSAXParser();
            xmlReader = parser.getXMLReader();
            xmlReader.setContentHandler(this);
            xmlReader.setErrorHandler(new MyErrorHandler(System.err));
        }
        catch ( Exception e )
        {
            throw new Exception(e);
        }
    }
    /**
     * Digest a single XML file
     * @param data the data read from the XML file
     */
    void digest( char[] data ) throws Exception
    {
        CharArrayReader car = new CharArrayReader(data);
        InputSource input = new InputSource(car);
        xmlReader.parse(input);     
    }
    public void startElement( String namespaceURI, String localName,
        String qName, Attributes atts ) throws SAXException 
    {
        if ( note != null )
        {
            note.addStartTag(localName,atts);
        }
        else if ( localName.equals("note") )
        {
            // String vid, String docid, int offset, String resp, 
            // boolean link, boolean propagateDown
            note = new Annotation( pos, atts.getValue("resp"), false, true, 0 );
        }
        else
        {
            body.append("<");
            body.append(localName);
            for ( int i=0;i<atts.getLength();i++ )
            {
                body.append(" ");
                body.append( atts.getLocalName(i) );
                body.append("=\"");
                body.append( atts.getValue(i) );
                body.append("\"");
            }
            body.append(">");
        }
    }
    /**
     * Ensure that the string doesn't contain unescaped " chars
     * @param src the source string
     * @return the same string with all '"' chars escaped
     */
    private String escape( String src )
    {
        char[] ch = src.toCharArray();
        StringBuilder sb = new StringBuilder();
        //int state = 0;
        for ( int i=0;i<ch.length;i++ )
        {
            if ( ch[i]=='&' )
                sb.append("&amp;");
            else
                sb.append(ch[i]);
        }
//            switch ( state )
//            {
//                case 0: // looking for " or '\\'
//                    if ( ch[i]=='&' )
//                        sb.append("&amp;");
//                    else if ( ch[i] == '"' )
//                        sb.append("\\\"");
//                    else if ( ch[i]=='\\' )
//                        state = 1;
//                    else 
//                        sb.append(ch[i]);
//                    break;
//                case 1: // seen '\\'
//                    if ( ch[i] == '"' )
//                        sb.append("\\\"");
//                    else
//                    {
//                        sb.append("\\");
//                        sb.append(ch[i]);
//                    }
//                    state = 0;
//                    break;
//            }
//        }
        // if last character was a backslash...
//        if ( state == 1 )
//            sb.append("\\");
        return sb.toString();
    }
    public void characters(char[] ch, int start, int length)
    {
        if ( note != null )
            note.addToBody( escape(new String(ch,start,length)) );
        else if ( length == 1 && ch[start]=='&' )
        {   
            body.append("&amp;");
            pos += 5;
        }
        else 
        {
            body.append( ch, start, length );
            pos += length;
        }
    }
    /**
     * End tag handler
     * @param uri
     * @param localName
     * @param qName 
     */
    public void endElement(String uri, String localName, String qName)
    {
        if ( note != null )
        {
            if ( localName.equals("note") )
            {
                notes.add( note );
                note = null;
            }
            else
            {
                note.addEndTag(localName);
            }
        }
        else
        {
            body.append("</");
            body.append(localName);
            body.append(">");
        }
    }
    public String getBody()
    {
        return body.toString();
    }
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("[ ");
        for ( int i=0;i<notes.size();i++ )
        {
            sb.append(notes.get(i).toString());
            if ( i<notes.size()-1 )
                sb.append(",\n");
        }
        sb.append(" ]");
        return sb.toString();
    }
}
