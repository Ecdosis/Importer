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

package importer.handler.post.stages;

import importer.exception.ImporterException;
import java.io.File;
import java.io.FileInputStream;
import java.util.Map;
import java.util.HashMap; 
import java.util.Stack;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import java.io.PrintStream;
import java.io.CharArrayReader;

/**
 * SAX splitter is a simplified version-splitter for TEI-XML
 * Instead of DOM we use a linear SAX parse and record the <em>paths</em>
 * leading into each variable bit. We maintain an index where these
 * paths are mapped to layer-names, e.g. app-rdg@wit=X or subst-del
 * @author desmond
 */
public class SAXSplitter extends DefaultHandler {
    SAXParser parser;
    XMLReader xmlReader;
    int lineNo;
    StringBuilder path;
    HashSet<String> splits;
    /** equivalent elements */
    HashMap<String,String> siblings;
    /** discriminators on siblings*/
    HashMap<String,String> attributes;
    /** layer map to content */
    HashMap<String,Integer> layers;
    /** the last popped element name */
    String last;
    /** number of sibling in current set */
    int siblingCount;
    Stack<Integer> states;
    /**
     * Split a TEI-XML file into versions of XML
     * @param tei the TEI content containing versions
     * @return an analysis of the variant markup in a file
     * @throws ImportException if something went wrong
     */
    public JSONArray scan( String tei ) throws ImporterException
    {
        this.layers = new HashMap<String,Integer>();
        this.lineNo = 1;
        this.splits = new HashSet<String>();
        this.attributes = new HashMap<String,String>();
        this.siblings = new HashMap<String,String>();
        this.states = new Stack<Integer>();
        this.states.push(0);
        this.siblingCount = 0;
        this.path = new StringBuilder();
        // hard-wire config for now
        attributes.put("add","n");
        attributes.put("rdg","wit");
        attributes.put("lem","wit");
        siblings.put("add","del");
        siblings.put("del","add");
        siblings.put("lem","rdg");
        siblings.put("rdg","lem");
        splits.add("add");
        splits.add("del");
        splits.add("sic");
        splits.add("corr");
        splits.add("abbrev");
        splits.add("expan");
        splits.add("rdg");
        splits.add("lem");
        splits.add("app");
        splits.add("mod");
        splits.add("choice");
        splits.add("subst");
        try
        {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setNamespaceAware(true);
            parser = spf.newSAXParser();
            xmlReader = parser.getXMLReader();
            xmlReader.setContentHandler(this);
            xmlReader.setErrorHandler(new MyErrorHandler(System.err));
            CharArrayReader car = new CharArrayReader(tei.toCharArray());
            xmlReader.parse(new InputSource(car));
            return layersToJson();
        }
        catch ( Exception e )
        {
            throw new ImporterException(e);
        }
    }
    int compare( JSONObject a, JSONObject b )
    {
        String stra = (String)a.get("path");
        String strb = (String)b.get("path");
        return stra.compareTo(strb);
    }
    /**
     * Sort an array of path objects
     * @param jArr the array of path json objects
     * @return the sorted array
     */
    JSONArray sort( JSONArray jArr )
    {
        int increment = jArr.size() / 2;
	while (increment > 0) {
            for (int i = increment; i < jArr.size(); i++) 
            {
                int j = i;
                JSONObject temp = (JSONObject)jArr.get(i);
                while (j >= increment 
                    && compare((JSONObject)jArr.get(j-increment),temp)>0 ) 
                {
                    jArr.set(j,jArr.get(j-increment));
                    j = j - increment;
                }
                jArr.set(j,temp);
            }
            if (increment == 2) 
                increment = 1;
            else
                increment *= (5.0 / 11);
	}
        return jArr;
    }
    /**
     * Convert the layers hashmap to JSON
     * @return a JSONArray of path+line values
     */
    private JSONArray layersToJson()
    {
        Set<String> keys = layers.keySet();
        Iterator<String> iter = keys.iterator();
        JSONArray arr = new JSONArray();
        while ( iter.hasNext() )
        {
            String path = iter.next();
            Integer line = layers.get(path);
            JSONObject jObj = new JSONObject();
            jObj.put("path",path);
            jObj.put("line",line);
            arr.add(jObj);
        }
        return sort(arr);
    }
    /**
     * Do we have a new element that forms part of a series?
     * @param localName the new element name
     * @return true if it is part of a series
     */
    private boolean isSibling( String localName )
    {
        if ( last == null|| !siblings.containsKey(last) )
        {
            if ( siblings.containsKey(localName) )
                return true;
        }
        else 
        {
            if ( localName.equals(last) )
                return true;
            else 
            {
                String lastSib = siblings.get(last);
                if ( lastSib !=null && lastSib.equals(localName) )
                    return true;
            }
        }
        return false;
    }
    public void processingInstruction(String target, String data) throws SAXException
    {
        System.out.println(data);
    }
    public void startElement(String namespaceURI, String localName,
        String qName, Attributes atts) throws SAXException 
    {
        if ( splits.contains(localName) )
        {
            StringBuilder component = new StringBuilder(localName);
            if ( isSibling(localName) )
                this.siblingCount++;
            else
                siblingCount = 0;
            if ( siblingCount > 1 )
            {
                component.append(":");
                component.append(siblingCount);
            }
            if ( attributes.containsKey(localName) )
            {
                String attr = attributes.get(localName);
                if ( atts.getValue(attr)!= null )
                {
                    component.append("@");
                    component.append(attr);
                    component.append("=");
                    component.append(atts.getValue(attr));
                }
            }
            pushPath(component.toString());
        }
        else
             siblingCount = 0;
    }
    public void endElement(String uri, String localName, String qName)
    {
        if ( splits.contains(localName) )
            popPath();
        else
             siblingCount = 0;
    }
    private boolean isWhitespace( String str )
    {
        boolean answer = true;
        for ( int i=0;i<str.length();i++ )
        {
            if ( !Character.isWhitespace(str.charAt(i)) )
                answer = false;
            else if ( str.charAt(i)=='\n' )
            {
                 lineNo++;
                 if ( lineNo==13153 )
                     System.out.println("13153");
            }
        }
        return answer;
    }
    public void characters(char[] ch, int start, int length) throws SAXException
    {
        String str = new String(ch,start,length);
        boolean ws = isWhitespace(str);
        String pStr = path.toString();
        if ( path.length()>0 && !ws &&!layers.containsKey(pStr) )
        {
            layers.put(pStr,lineNo);
        }
        else if ( !ws )
            siblingCount = 0;
    }
    /**
     * Keep track of ALL newlines
     * @param ch characters data from parser
     * @param start offset into ch
     * @param length length of whitespace 
     */
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException
    {
        String str = new String(ch,start,length);
        isWhitespace(str);
    }
    /**
     * A new split element has been read
     * @param segment the path component
     */
    private void pushPath( String segment )
    {
        path.append("/");
        path.append(segment);
        states.push(siblingCount);
        siblingCount = 0;
    }
    private String cleanComponent( String comp )
    {
        int index = comp.indexOf(":");
        if ( index != -1 )
            comp = comp.substring(0,index);
        index = comp.indexOf("@" );
        if ( index != -1 )
            comp = comp.substring(0,index);
        return comp;
    }
    /**
     * At split-element end pop the current path
     */
    private void popPath()
    {
        int index = path.lastIndexOf("/");
        if ( index != -1 )
        {
            this.last = cleanComponent(path.substring(index+1));
            path.setLength(index);
        }
        if ( states.size() > 0 )
            siblingCount = states.pop();
    }
    /**
     * Basic error handler with line-number counter
     */
    private static class MyErrorHandler implements ErrorHandler 
    {
        private PrintStream out;

        MyErrorHandler(PrintStream out) 
        {
            this.out = out;
        }

        private String getParseExceptionInfo(SAXParseException spe) {
            String systemId = spe.getSystemId();

            if (systemId == null) {
                systemId = "null";
            }

            String info = "URI=" + systemId + " Line=" 
                + spe.getLineNumber() + ": " + spe.getMessage();

            return info;
        }
        public void warning(SAXParseException spe) throws SAXException {
            out.println("Warning: " + getParseExceptionInfo(spe));
        }
        public void error(SAXParseException spe) throws SAXException {
            String message = "Error: " + getParseExceptionInfo(spe);
            throw new SAXException(message);
        }
        public void fatalError(SAXParseException spe) throws SAXException {
            String message = "Fatal Error: " + getParseExceptionInfo(spe);
            throw new SAXException(message);
        }
    }
    public static void main(String[] args )
    {
        if ( args.length==1 )
        {
            File f = new File(args[0]);
            byte[] data = new byte[(int)f.length()];
            try
            {
                FileInputStream fis = new FileInputStream(f);
                fis.read(data);
                SAXSplitter ss = new SAXSplitter();
                JSONArray jArr = ss.scan(new String(data,"UTF-8"));
                String jStr = jArr.toJSONString();
                System.out.println(jStr.replaceAll("\\\\/", "/"));
            }
            catch ( Exception e )
            {
                System.out.println(e.getMessage());
            }
        }
    }
}
