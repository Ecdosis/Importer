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

/**
 * Represent an annotation in an MVD
 * @author desmond
 */
public class Annotation {
    int offset;
    int length;
    String vid;
    String resp;
    boolean propagateDown;
    boolean link;
    String body;
    /**
     * Create a basic annotation - fill in details later
     * @param vid the version id starting with "/"
     * @param offset the offset in the underlying text
     * @param resp the person who wrote it (abbreviated)
     * @param link true if this is a link, else it's a plain annotation
     * @param propagateDown if true apply this annotation to 
     * @param length the length of the annotated text
     */
    public Annotation( String vid, int offset, String resp, 
        boolean link, boolean propagateDown, int length )
    {
        this.offset = offset;
        this.resp = resp;
        this.vid = vid;
        this.propagateDown = propagateDown;
        this.link = link;
        this.length = length;
    }
    /**
     * Add some text to the annotation body
     * @param text 
     */
    public void addToBody( String text )
    {
        if ( this.body == null )
            this.body = text;
        else
            this.body += text;
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
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("{ ");
        appendInt(sb, "offset", this.offset );
        sb.append(", ");
        appendInt(sb,"length", this.length );
        sb.append(", ");
        appendString(sb,"vid", vid );
        sb.append(", ");
        appendString( sb, "resp", resp );
        sb.append(", ");
        appendBoolean( sb, "propagateDown", propagateDown );
        sb.append(", ");
        appendBoolean( sb, "link", link );
        sb.append(", ");
        appendString(sb,"body", body);
        sb.append(" }");
        return sb.toString();
    }
}
