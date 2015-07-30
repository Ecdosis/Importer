/*
 * This file is part of Compare.
 *
 *  Compare is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  Compare is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Copare.  If not, see <http://www.gnu.org/licenses/>.
 *  (c) copyright Desmond Schmidt 2015
 */

package importer.handler;
import importer.exception.ImporterException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.Charset;

/**
 * Abstract super-class for all handlers: PUT, POST, DELETE, GET
 * @author ddos
 */
abstract public class ImporterHandler 
{
    protected String encoding;
    protected String version1;
    protected String docid;
    public ImporterHandler()
    {
        this.encoding = Charset.defaultCharset().name();
    }
    public abstract void handle( HttpServletRequest request, 
        HttpServletResponse response, String urn ) throws ImporterException;
    protected String getAuthor()
    {
        if ( docid != null )
        {
            String[] parts = docid.split("/");
            if ( parts.length > 1 )
                return parts[1];
            else
                return "";
        }
        else
            return "";
    }
    protected String getWork()
    {
        if ( docid != null )
        {
            String[] parts = docid.split("/");
            if ( parts.length > 2 )
                return parts[2];
            else
                return "";
        }
        else
            return "";
    }
    public String guessEncoding(byte[] bytes) 
    {
        org.mozilla.universalchardet.UniversalDetector detector =
            new org.mozilla.universalchardet.UniversalDetector(null);
        detector.handleData(bytes, 0, bytes.length);
        detector.dataEnd();
        String charset = detector.getDetectedCharset();
        if ( charset == null )
            charset = checkForMac(bytes);
        if ( charset == null )
            charset = "UTF-8";
        detector.reset();
        if ( !charset.equals(encoding) ) 
            encoding = charset;
        return encoding;
    }
    private String checkForMac( byte[] data )
    {
        int macchars = 0;
        for ( int i=0;i<data.length;i++ )
        {
            if ( data[i]>=0xD0 && data[i]<=0xD5 )
            {
                macchars++;
                if ( macchars > 5 )
                    break;
            }
        }
        if ( macchars > 5 )
            return "macintosh";
        else
            return null;
    }
}
