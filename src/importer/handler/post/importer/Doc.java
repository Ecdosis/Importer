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

import java.io.StringWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 *
 * @author desmond
 */
public class Doc {
        /**
     * Print a single element node
     * @param elem the element in question
     * @param versions the set of versions to write to
     * @param map the map of versions to buffers
     */
    static void printElementBody( Element elem, StringWriter sw )
    {
        // recurse into its children
        Node child = elem.getFirstChild();
        while ( child != null )
        {
            printNode( child, sw );
            child = child.getNextSibling();
        }
    }
    /**
     * Print an element's end-code to the current buffer
     * @param elem the element in question
     */
    static void printElementEnd( Element elem, StringWriter sw )
    {
        if ( elem.getFirstChild()!=null )
        {
            sw.write( "</" );
            sw.write(elem.getNodeName() );
            sw.write( ">" );
        }
    }
    /**
     * Print a single element node to the current buffer
     * @param elem the element in question
     */
    static void printElementStart( Element elem, StringWriter sw )
    {
        sw.write("<");
        sw.write(elem.getNodeName());
        NamedNodeMap attrs = elem.getAttributes();
        for ( int j=0;j<attrs.getLength();j++ )
        {
            Node attr = attrs.item( j );
            sw.write( " ");
            sw.write( attr.getNodeName() );
            sw.write( "=\"" );
            sw.write( attr.getNodeValue() );
            sw.write( "\"" );
        }
        if ( elem.getFirstChild()==null )
            sw.write("/>");
        else
            sw.write(">");
    }
    /**
     * Write a node to the current output buffer
     * @param node the node to start from
     */
    static void printNode( Node node, StringWriter sw )
    {
        if ( node.getNodeType()==Node.TEXT_NODE )
        {
            String content = node.getTextContent();
            if ( content.contains("&") )
                content = content.replace("&","&amp;");
            sw.write( content );
        }
        else if ( node.getNodeType()==Node.ELEMENT_NODE )
        {
            printElementStart( (Element)node,sw );
            printElementBody( (Element)node,sw );
            printElementEnd( (Element)node, sw );
        }
        else if ( node.getNodeType()==Node.COMMENT_NODE )
        {
            sw.write("<!--");
            sw.write(node.getTextContent());
            sw.write("-->");
        }
    }
	
    /**
     * Convert a loaded DOM document to a String
     * @param doc the DOM document
     * @return a String being its content
     */
    public static String docToString( Document doc )
    {
        StringWriter sw = new StringWriter();
        printNode( doc.getDocumentElement(),sw );
        return sw.toString();
    }
}
