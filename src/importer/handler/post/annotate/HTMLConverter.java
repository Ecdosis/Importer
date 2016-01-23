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
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;
import java.util.Stack;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.*;
import org.xml.sax.*;
import importer.exception.HTMLException;
import java.io.CharArrayReader;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.util.HashSet;

/**
 * Convert standard Harpur XML to HTML
 * @author desmond
 */
public class HTMLConverter extends DefaultHandler
{
    StringBuilder body;
    HTMLPattern current;
    JSONArray patterns;
    HashMap<Element,HTMLPattern> map;
    HashSet<String> ignore;
    Stack<HTMLPattern> stack;
    /**
     * Create  a conversion pattern for a particular xml tag
     * @param xmlTag the xml tag name
     * @param htmlTag the corresponding html tag name
     * @param htmlAttrName the html attribute or null
     * @param htmlAttrValue the html attribute value or null
     * @param xmlAttrName the matching xml attribute name or null
     * @param xmlAttrValue the matching xml attribute value or null
     */
    void addPattern( String xmlTag, String htmlTag, 
        String htmlAttrName, String htmlAttrValue, String xmlAttrName, 
        String xmlAttrValue )
    {
        HTMLPattern hp = new HTMLPattern(htmlTag);
        // ignore null html or xml attribute names
        // but values can be null
        if ( htmlAttrName != null )
            hp.addAttribute(htmlAttrName,htmlAttrValue);
        // only one matching xml-attribute for now should suffice
        Element elem = new Element(xmlTag);
        if ( xmlAttrName != null )
            elem.addAttribute(xmlAttrName,xmlAttrValue);
        map.put(elem,hp);
    }
    /**
     * Convert an xml->html converter with limited functionality
     * @param config the config specifying how to convert the tags+attributes
     */
    HTMLConverter( JSONArray config )
    {
        this.body = new StringBuilder();
        this.patterns = config;
        map = new HashMap<Element,HTMLPattern>();
        this.stack = new Stack<HTMLPattern>();
        this.ignore = new HashSet<String>();
        for ( int i=0;i<config.size();i++ )
        {
            JSONObject jObj = (JSONObject)config.get(i);
            String htmlTag = (String)jObj.get("htmlTag");
            String xmlTag = (String)jObj.get("xmlTag");
            String htmlAttrName = (String)jObj.get("htmlAttrName");
            String htmlAttrValue = (String)jObj.get("htmlAttrValue");
            String xmlAttrName = (String)jObj.get("xmlAttrName");
            String xmlAttrValue = (String)jObj.get("xmlAttrValue");
            addPattern(xmlTag,htmlTag,htmlAttrName,htmlAttrValue,
                xmlAttrName,xmlAttrValue);
        }
    }
    /**
     * Turn XML note content for Harpur to HTML
     * @param xml the xml note
     * @param config the JSON config to control the conversion
     * @return a HTML string
     */
    String convert( String xml ) throws HTMLException
    {
        try
        {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            HTMLConverter conv = new HTMLConverter(patterns);
            spf.setNamespaceAware(true);
            SAXParser parser = spf.newSAXParser();
            XMLReader xmlReader = parser.getXMLReader();
            xmlReader.setContentHandler(conv);
            xmlReader.setErrorHandler(new MyErrorHandler(System.err));
            CharArrayReader car = new CharArrayReader(xml.toCharArray());
            InputSource input = new InputSource(car);
            xmlReader.parse(input);
            return conv.body.toString();
        }
        catch ( Exception e )
        {
            throw new HTMLException(e);
        }
    }
    public void startElement( String namespaceURI, String localName,
        String qName, Attributes atts ) throws SAXException 
    {
        Set<Element> keys = map.keySet();
        Iterator<Element> iter = keys.iterator();
        Element best = null;
        //System.out.println(localName);
        while ( iter.hasNext() )
        {
            Element e = iter.next();
            if ( e.matches(localName,atts) )
            {
                if ( best == null || e.isBetterThan(best) )
                    best = e;
            }
        }
        // use longest match
        if ( best != null )
        {
            HTMLPattern hp = map.get(best);
            body.append( hp.getStartTag(atts) );
            stack.push(hp);
            current = hp;
        }
        else
            ignore.add(localName);
    }
    public void characters(char[] ch, int start, int length)
    {
        body.append( ch, start, length );
    }
    public void endElement(String uri, String localName, String qName)
    {
        if ( current != null )
        {
            body.append( current.getEndTag() );
            if ( !ignore.contains(localName) )
                current = stack.pop();
        }
    }
}
