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
import java.util.ArrayList;
import java.util.HashSet;
import importer.Archive;
import importer.constants.Globals;
import importer.exception.ImporterException;
import java.io.ByteArrayInputStream;
import javax.swing.text.rtf.RTFEditorKit;
import javax.swing.text.Document;
/**
 * A stage to eliminate over-size and non-text, non-XML, non-rtf, 
 * wrongly encoded files
 * @author desmond
 */
public class StageOne extends Stage
{
    static String XML = "xml";
    static String TEXT = "txt";
    static String RTF = "rtf";
    static String HTML = "html";
    static String HTM = "htm";
    HashSet<String> suffixes;
    public StageOne( ArrayList<File> files )
    {
        super();
        this.files = files;
        suffixes = new HashSet<String>();
        suffixes.add( XML );
        suffixes.add( TEXT );
        suffixes.add( HTML );
        suffixes.add( RTF );
        suffixes.add( HTM );
    }
    /**
     * Extract the suffix if any from a file name
     * @param fileName the file name
     * @return the suffix or the empty string
     */
    private String suffix( String fileName )
    {
        int dotPos = fileName.lastIndexOf(".");
        if ( dotPos == -1 )
            return "";
        else
            return fileName.substring(dotPos+1).toLowerCase();
    }   
    /**
     * Eliminate files that are too big
     * @param cortex a MVD archive for the plain text
     * @param corcode an MVD archive for the versioned markup
     * @return the log record of the elimination process
     */
    @Override
    public String process( Archive cortex, Archive corcode ) 
        throws ImporterException
    {
        ArrayList<File> newFiles = new ArrayList<File>();
        for ( int i=0;i<files.size();i++ )
        {
            File item = files.get(i);
            String suffix = suffix(item.name);
            if ( item.data.length()>Globals.MAX_UPLOAD_LEN )
            {
                log.append("File ");
                log.append( item.name );
                log.append( " rejected because it is too long (" );
                log.append( item.data.length() );
                log.append( "). Maximum is " );
                log.append( Globals.MAX_UPLOAD_LEN );
                log.append( ".\n" );
            }
            else if ( suffix.length() == 0 || suffixes.contains(suffix) )
                newFiles.add( item );
            else if ( suffix.equals(RTF) )
            {
                // convert to text
                try
                {
                    RTFEditorKit rtfParser = new RTFEditorKit();
                    Document document = rtfParser.createDefaultDocument();
                    ByteArrayInputStream bis = new ByteArrayInputStream(
                        item.data.getBytes("UTF-8"));
                    rtfParser.read( bis, document, 0 );
                    String text = document.getText(0, document.getLength());
                    String name = item.simpleName()+"."+TEXT;
                    newFiles.add(new File(name,text) );
                }
                catch ( Exception e )
                {
                    throw new ImporterException( e );
                }
            }
            else
            {
                log.append( "File " );
                log.append( item.name );
                log.append( " rejected because suffix is .");
                log.append(suffix);
                log.append( " not .xml or .txt or .rtf or .html/htm or empty\n");
            }
        }
        files = newFiles;
        return log.toString();
    }
}
