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

/**
 * Strip notes from a TEI-XML file 
 * @author desmond
 */
public class NoteStripper 
{
    ArrayList<Annotation> notes;
    public String strip( String xml, String docid, String vid ) throws Exception
    {
        notes = new ArrayList<Annotation>();
        SaxParser sp = new SaxParser( docid, vid, notes );
        sp.digest( xml.toCharArray() );
        return sp.getBody();
    }
    public ArrayList<Annotation> getNotes()
    {
        return notes;
    }
}