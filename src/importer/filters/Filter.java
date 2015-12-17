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

package importer.filters;
import importer.Archive;
import importer.exception.ImporterException;
import calliope.AeseSpeller;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.util.HashSet;
import org.json.simple.JSONObject;

/**
 * Specify how filters interact with the outside world
 * @author desmond
 */
public abstract class Filter 
{
    protected MarkupSet markup;
    protected String dict;
    protected String hhExceptions;
    protected AeseSpeller speller;
    protected boolean lastEndsInHyphen;
    protected HashSet<String> compounds;
    protected int written;
    protected char[] CR = {'\n'};
    protected char[] HYPHEN = {'-'};
    protected char[] SPACE = {' '};
    protected char[] EMPTY = {};
    protected String ENC = "UTF-8";
    public Filter()
    {
        this.dict = "en_GB";
        this.markup = new MarkupSet();
        this.hhExceptions = "";
        try
        {
            this.speller = new AeseSpeller( dict );
            this.compounds = new HashSet<String>();
            if ( hhExceptions != null && hhExceptions.length()>0 )
            {
                String[] items = hhExceptions.split( "\n" );
                for ( int i=0;i<items.length;i++ )
                    compounds.add( items[i] );
            }
        }
        catch ( Exception e1 )
        {
            try
            {
                this.speller = new AeseSpeller("en_GB");
            }
            catch ( Exception e2 )
            {
            }
        }
    }
    /**
     * Set the encoding used for serialisation. This should be the MVD's 
     * internal encoding. Can be anything.     
     * @param encoding the encoding, defaults to UTF-8
     */
    public void setEncoding( String encoding )
    {
        ENC = encoding;
    }
    /**
     * We really should cleanup the speller before we go
     */
    protected void finalize()
    {
        if ( this.speller != null )
            this.speller.cleanup();
    }
    protected void writeCurrent( CharArrayWriter txt, char[] current )
        throws IOException
    {
        txt.write( current );
        written += current.length;    
    }
    /**
     * Should we hard-hyphenate two words or part-words?
     * @param last the previous 'word'
     * @param next the word on the next line
     * @return true for a hard hyphen else soft
     */
    public boolean isHardHyphen( String last, String next )
    {
        String compound = last+next;
        if ( last.equals("--") )
            return true;
        else if ( speller.hasWord(last,dict)
            &&speller.hasWord(next,dict)
            &&(!speller.hasWord(compound,dict)
                ||compounds.contains(compound)))
            return true;
        else
            return false;
    }
    public void setDict( String dict )
    {
        this.dict = dict;
    }
    public void setHHExceptions( String hhExceptions )
    {
        this.hhExceptions = hhExceptions;
    }
    /**
     * Get the raw name of this filter e.g. "play"
     * @return the filter name
     * @throws AeseException 
     */
    public String getName() throws ImporterException
    {
        String className = this.getClass().getSimpleName();
        int pos = className.indexOf("Filter");
        if ( pos != -1 )
            return className.substring(0,pos);
        else
            throw new ImporterException("invalid class name: "+className);
    }
    /**
     * Get the first word of a line
     * @param line the line in question
     * @return 
     */
    protected String getFirstWord( String line )
    {
        int i;
        int len = line.length();
        for ( i=0;i<line.length();i++ )
        {
            if ( !Character.isWhitespace(line.charAt(i)) )
                break;
        }
        int j = i;
        for ( ;i<len;i++ )
        {
            if ( !Character.isLetter(line.charAt(i))||line.charAt(i)=='-' )
                break;
        }
        return line.substring(j,i);
    }
    /**
     * Get the last word of a line excluding punctuation etc
     * @param line the line in question
     * @return the word
     */
    protected String getLastWord( String text )
    {
        int len = text.length();
        if ( len > 0 )
        {
            int start = 0;
            int size=0,i=len-1;
            // point beyond trailing hyphen
            if ( len>1 && text.endsWith("--") )
            {
                lastEndsInHyphen = true;
                return "--";
            }
            else if ( text.charAt(len-1) == '-' )
            {
                lastEndsInHyphen = true;
                len--;
                i--;
            }
            else
            {
                lastEndsInHyphen = false;
                // point to last non-space
                for ( ;i>0;i-- )
                {
                    if ( !Character.isWhitespace(text.charAt(i)) )
                        break;
                }
            }
            int j = i;
            for ( ;i>0;i-- )
            {
                if ( !Character.isLetter(text.charAt(i)) )
                {
                    start = i+1;
                    size = j-i;
                    break;
                }
            }
            if ( i==0 )
                size = (j-i)+1;
            return text.substring(start,start+size);
        }
        else
            return "";
    }
    /**
     * Reinitialise for a new conversion
     */
    protected void init()
    {
        written = 0;
        markup.clear();
    }
    public abstract void configure( JSONObject config );
    /**
     * Short description of this filter
     * @return a string
     */
    public abstract String getDescription();
    /**
     * Subclasses should override this
     * @param input the input text for conversion
     * @param name the name of the new version
     * @param cortex the cortex archive to save filtered text in
     * @param corcode the corcode archive to save the inferred markup in
     * @return the log output
     */
    public abstract String convert( String input, String name, Archive cortex, 
        Archive corcode ) throws ImporterException;
}
