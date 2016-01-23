/* This file is part of calliope.
 *
 *  calliope is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  calliope is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with calliope.  If not, see <http://www.gnu.org/licenses/>.
 */

package importer.handler.post.stages;
import importer.Archive;
import importer.exception.ImporterException;
import calliope.json.JSONResponse;
import calliope.AeseStripper;
import calliope.core.Utils;
import calliope.core.database.*;
import calliope.core.constants.Database;
import calliope.core.exception.DbException;
import importer.constants.Globals;
import calliope.core.constants.JSONKeys;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.Iterator;
import importer.handler.post.annotate.Annotation;
import importer.handler.post.annotate.NoteStripper;
import mml.filters.Filter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import java.io.FileOutputStream;
import java.util.UUID;
/**
 * Process the XML files for import
 * @author desmond
 */
public class StageThreeXML extends Stage
{
    String stripConfig;
    String splitConfig;
    String style;
    String xslt;
    String dict;
    String hhExcepts;
    boolean hasTEI;
    String docid;
    String encoding;
    ArrayList<Annotation> notes;
    // deafult config really only works with Harpur
    // so you need a resource at projid+"/html/default"
    static String DEFAULT_HTML_CONFIG = 
        "{\"docid\" : \"english/harpur/html/default\", \"patterns\":[{"
    +"\"xmlTag\":\"addName\",\"htmlTag\":\"span\",\"htmlAttrName\""
    +":\"class\",\"htmlAttrValue\":\"author\",\"xmlAttrName\":null"
    +",\"xmlAttrValue\":null},\n{\"xmlTag\":\"author\",\"htmlTag\""
    +":\"span\",\"htmlAttrName\":\"class\",\"htmlAttrValue\":\"aut"
    +"hor\",\"xmlAttrName\":null,\"xmlAttrValue\":null},\n{\"xmlTa"
    +"g\":\"date\",\"htmlTag\":\"span\",\"htmlAttrName\":\"class\""
    +",\"htmlAttrValue\":\"date\",\"xmlAttrName\":null,\"xmlAttrVa"
    +"lue\":null},\n{\"xmlTag\":\"emph\",\"htmlTag\":\"span\",\"ht"
    +"mlAttrName\":\"class\",\"htmlAttrValue\":\"emphasis\",\"xmlA"
    +"ttrName\":null,\"xmlAttrValue\":null},\n{\"xmlTag\":\"emph\""
    +",\"htmlTag\":\"span\",\"htmlAttrName\":\"class\",\"htmlAttrV"
    +"alue\":\"italics\",\"xmlAttrName\":\"rend\",\"xmlAttrValue\""
    +":\"it\"},\n{\"xmlTag\":\"emph\",\"htmlTag\":\"span\",\"htmlA"
    +"ttrName\":\"class\",\"htmlAttrValue\":\"underlined\",\"xmlAt"
    +"trName\":\"rend\",\"xmlAttrValue\":\"ul\"},\n{\"xmlTag\":\"h"
    +"ead\",\"htmlTag\":\"h3\",\"htmlAttrName\":\"class\",\"htmlAt"
    +"trValue\":\"head\",\"xmlAttrName\":null,\"xmlAttrValue\":nul"
    +"l},\n{\"xmlTag\":\"hi\",\"htmlTag\":\"span\",\"htmlAttrName\""
    +":\"class\",\"htmlAttrValue\":\"italics\",\"xmlAttrName\":nu"
    +"ll,\"xmlAttrValue\":null},\n{\"xmlTag\":\"hi\",\"htmlTag\":\""
    +"span\",\"htmlAttrName\":\"class\",\"htmlAttrValue\":\"itali"
    +"cs\",\"xmlAttrName\":\"rend\",\"xmlAttrValue\":\"it\"},\n{\""
    +"xmlTag\":\"hi\",\"htmlTag\":\"span\",\"htmlAttrName\":\"clas"
    +"s\",\"htmlAttrValue\":\"underlined\",\"xmlAttrName\":\"rend\""
    +",\"xmlAttrValue\":\"ul\"},\n{\"xmlTag\":\"hi\",\"htmlTag\":"
    +"\"span\",\"htmlAttrName\":\"class\",\"htmlAttrValue\":\"smal"
    +"lcaps\",\"xmlAttrName\":\"rend\",\"xmlAttrValue\":\"sc\"},\n"
    +"{\"xmlTag\":\"hi\",\"htmlTag\":\"span\",\"htmlAttrName\":\"c"
    +"lass\",\"htmlAttrValue\":\"double-underlined\",\"xmlAttrName"
    +"\":\"rend\",\"xmlAttrValue\":\"dul\"},\n{\"xmlTag\":\"hi\",\""
    +"htmlTag\":\"span\",\"htmlAttrName\":\"class\",\"htmlAttrVal"
    +"ue\":\"superscript\",\"xmlAttrName\":\"rend\",\"xmlAttrValue"
    +"\":\"ss\"},\n{\"xmlTag\":\"l\",\"htmlTag\":\"span\",\"htmlAt"
    +"trName\":\"class\",\"htmlAttrValue\":\"line\",\"xmlAttrName\""
    +":null,\"xmlAttrValue\":null},\n{\"xmlTag\":\"l\",\"htmlTag\""
    +":\"span\",\"htmlAttrName\":\"class\",\"htmlAttrValue\":\"li"
    +"ne-indent1\",\"xmlAttrName\":\"rend\",\"xmlAttrValue\":\"ind"
    +"ent1\"},\n{\"xmlTag\":\"l\",\"htmlTag\":\"span\",\"htmlAttrN"
    +"ame\":\"class\",\"htmlAttrValue\":\"line-indent2\",\"xmlAttr"
    +"Name\":\"rend\",\"xmlAttrValue\":\"indent2\"},\n{\"xmlTag\":"
    +"\"l\",\"htmlTag\":\"span\",\"htmlAttrName\":\"class\",\"html"
    +"AttrValue\":\"line-indent3\",\"xmlAttrName\":\"rend\",\"xmlA"
    +"ttrValue\":\"indent3\"},\n{\"xmlTag\":\"l\",\"htmlTag\":\"sp"
    +"an\",\"htmlAttrName\":\"class\",\"htmlAttrValue\":\"line-ind"
    +"ent4\",\"xmlAttrName\":\"rend\",\"xmlAttrValue\":\"indent4\""
    +"},\n{\"xmlTag\":\"l\",\"htmlTag\":\"span\",\"htmlAttrName\":"
    +"\"class\",\"htmlAttrValue\":\"line-indent5\",\"xmlAttrName\""
    +":\"rend\",\"xmlAttrValue\":\"indent5\"},\n{\"xmlTag\":\"lb\""
    +",\"htmlTag\":\"br\",\"htmlAttrName\":null,\"htmlAttrValue\":"
    +"null,\"xmlAttrName\":null,\"xmlAttrValue\":null},\n{\"xmlTag"
    +"\":\"lg\",\"htmlTag\":\"div\",\"htmlAttrName\":\"class\",\"h"
    +"tmlAttrValue\":\"stanza\",\"xmlAttrName\":null,\"xmlAttrValu"
    +"e\":null},\n{\"xmlTag\":\"metamark\",\"htmlTag\":\"span\",\""
    +"htmlAttrName\":\"class\",\"htmlAttrValue\":\"metamark\",\"xm"
    +"lAttrName\":null,\"xmlAttrValue\":null},\n{\"xmlTag\":\"p\","
    +"\"htmlTag\":\"p\",\"htmlAttrName\":null,\"htmlAttrValue\":nu"
    +"ll,\"xmlAttrName\":null,\"xmlAttrValue\":null},\n{\"xmlTag\""
    +":\"pb\",\"htmlTag\":\"a\",\"htmlAttrName\":\"href\",\"htmlAt"
    +"trValue\":null,\"xmlAttrName\":\"facs\",\"xmlAttrValue\":nul"
    +"l},\n{\"xmlTag\":\"q\",\"htmlTag\":\"blockquote\",\"htmlAttr"
    +"Name\":null,\"htmlAttrValue\":null,\"xmlAttrName\":null,\"xm"
    +"lAttrValue\":null},\n{\"xmlTag\":\"ref\",\"htmlTag\":\"a\",\""
    +"htmlAttrName\":\"href\",\"htmlAttrValue\":null,\"xmlAttrNam"
    +"e\":\"target\",\"xmlAttrValue\":null},\n{\"xmlTag\":\"unclea"
    +"r\",\"htmlTag\":\"span\",\"htmlAttrName\":\"class\",\"htmlAt"
    +"trValue\":\"unclear\",\"xmlAttrName\":null,\"xmlAttrValue\":"
    +"null}]}\n";
    
    public StageThreeXML()
    {
        super();
        this.dict = Globals.DEFAULT_DICT;
    }
    public StageThreeXML( String style, String dict, String hhExcepts )
    {
        super();
        this.style = style;
        this.hhExcepts = hhExcepts;
        this.dict = (dict==null||dict.length()==0)?"en_GB":dict;
    }
    /**
     * 
     * @param last
     * @param style
     * @param dict name of dictionary e.g. en_GB
     * @param hhExcepts hard hyphen exceptions list (space delimited)
     */
    public StageThreeXML( Stage last, String style, String dict, 
        String hhExcepts )
    {
        super();
        this.style = style;
        this.hhExcepts = hhExcepts;
        this.dict = (dict==null||dict.length()==0)?"en_GB":dict;
        for ( int i=0;i<last.files.size();i++ )
        {
            File f = last.files.get( i );
            if ( f.isXML(log) )
            {
                if ( f.isTEI() )
                    hasTEI = true;
                if ( f.isTEICorpus() )
                {
                    File[] members = f.splitTEICorpus();
                    for ( int j=0;j<members.length;j++ )
                        this.files.add( members[j] );
                }
                else
                    this.files.add( f );
            }
            // irrelvant files already exceluded by stage 1
//            else
//            {
//                log.append( "excluding from XML set ");
//                log.append( f.name );
//                log.append(", not being valid XML\n" );
//            }
        }
    }     
    public ArrayList<Annotation> getAnnotations()
    {
        return notes;
    }
    /**
     * Does this stage3 have at least ONE TEI file?
     * @return true if it does
     */
    public boolean hasTEI()
    {
        return hasTEI;
    }
    /**
     * Set the XSLT stylesheet
     * @param xslt the XSLT transform stylesheet (XML)
     */
    public void setTransform( String xslt )
    {
        this.xslt = xslt;
    }
    /**
     * Set the stripping recipe for the XML filter
     * @param config a json document from the database
     */
    public void setStripConfig( String config )
    {
        this.stripConfig = config;
    }
    /**
     * Set the splitting recipe for the XML filter
     * @param config a json document from the database
     */
    public void setSplitConfig( String config )
    {
        this.splitConfig = config;
    }
    /**
     * Convert ordinary quotes into curly ones
     * @param a char array containing the unicode text
     */
    void convertQuotes( char[] chars )
    {
        char prev = 0;
        for ( int i=0;i<chars.length;i++ )
        {
            if ( chars[i]==39 )    // single quote, straight
            {
                if ( Character.isWhitespace(prev)
                    ||Character.getType(prev)==21
                    ||Character.getType(prev)==29 )
                    chars[i] = '‘';
                else
                    chars[i] = '’';
            }
            else if ( chars[i]==34 )   // double quote, straight
            {
                if ( Character.isWhitespace(prev)
                    ||Character.getType(prev)==21
                    ||Character.getType(prev)==29 )
                    chars[i] = '“';
                else
                    chars[i]='”';
            }
            prev = chars[i];
        }
    }
    /**
     * Get the version name from a standard file name
     * @param fileName the raw file name
     * @return the version name if it followed the standard pattern
     */
    String extractVersionName( String fileName )
    {
        String stripped = fileName;
        int index = stripped.lastIndexOf("/");
        if ( index != -1 )
            stripped = stripped.substring(index+1);
        index = stripped.lastIndexOf(".");
        if ( index != -1 )
            stripped = stripped.substring(0,index);
        index = stripped.indexOf("#");
        if ( index != -1 )
            stripped = stripped.substring(index+1);
        index = stripped.lastIndexOf("-");
        if ( index != -1 )
            stripped = stripped.substring(index+1);
        return stripped;
    }
    /**
     * Reduce the length of the package name of the filter being sought
     * @param className the classname of the filter that wasn't there
     * @return a shorted path e.g. mml.filters.english.harpur.Filter 
     * instead of mml.filters.english.harpur.h642.Filter
     */
    String popClassName( String className )
    {
        String[] parts = className.split("\\.");
        StringBuilder sb = new StringBuilder();
        if ( parts.length > 1 )
        {
            for ( int i=0;i<parts.length;i++ )
            {
                if ( i != parts.length-2 )
                {
                    if ( sb.length()>0 )
                        sb.append(".");
                    sb.append(parts[i]);
                }
            }
        }
        return sb.toString();
    }
    public void setEncoding( String encoding )
    {
        this.encoding = encoding;
    }
    public void setDocId( String docid )
    {
        this.docid = docid;
    }
    /**
     * Convert the corcode using the filter corresponding to its docid
     * @param pair the stil and its corresponding text - return result here
     * @param docID the docid of the document
     * @param enc the encoding
     */
    void convertCorcode( StandoffPair pair )
    {
        String[] parts = this.docid.split("/");
        StringBuilder sb = new StringBuilder("mml.filters");
        for ( String part : parts )
        {
            if ( part.length()>0 )
            {
                sb.append(".");
                sb.append(part);
            }
        }
        sb.append(".Filter");
        String className = sb.toString();
        Filter f = null;
        while ( className.length() > "mml.filters".length() )
        {
            try
            {
                Class fClass = Class.forName(className);
                f = (Filter) fClass.newInstance();
                break;
            }
            catch ( Exception e )
            {
                System.out.println("no filter for "+className+" popping...");
                className = popClassName(className);
            }
        }
        if ( f != null )
        {
            System.out.println("Applying filter "+className+" to "+pair.vid );
            // woo hoo! we have a filter baby!
            try
            {
                JSONObject cc = (JSONObject)JSONValue.parse(pair.stil);
//                if ( pair.vid.equals("A") )
//                {
//                    printFile(pair.stil,"/tmp/A-stil.json");
//                    printFile(pair.text,"/tmp/A.txt");
//                }
                cc = f.translate( cc, pair.text );
                pair.stil = cc.toJSONString();
                pair.text = f.getText();
            }
            catch ( Exception e )
            {
                //OK it didn't work
                System.out.println("It failed for "+pair.vid+e.getMessage());
                e.printStackTrace(System.out);
            }
        }
        else
            System.out.println("Couldn't find filter "+className+" for "+this.docid);
    }    
    void printFile( String str, String path )
    {
        try
        {
            java.io.File f = new java.io.File(path);
            if ( f.exists() )
                f.delete();
            FileOutputStream fos = new FileOutputStream(f);
            byte[] data = str.getBytes("UTF-8");
            fos.write( data );
            fos.close();
        }
        catch ( Exception e )
        {
        }
    }
    
    /**
     * Get the xml to HTML conversion file from the db
     * @param conn the database connection
     * @return the configuration string
     */
    String getHtmlConfig( Connection conn )
    {
        try
        {
            String[] parts = docid.split("/");
            String composite = docid+"/html/default";
            // try two-part docid
            if ( parts.length > 1 )
            {
                composite = parts[0]+"/"+parts[1]+"/html/default";
                String json = (String)conn.getFromDb(Database.CONFIG,composite);
                if ( json != null )
                    return json;
            }
            if ( parts.length > 2 )
            {
                composite = parts[0]+"/"+parts[1]+"/"+parts[2]+"/html/default";
                String json = (String)conn.getFromDb(Database.CONFIG,composite);
                if ( json != null )
                    return json;
            }
            return DEFAULT_HTML_CONFIG;
        }
        catch ( DbException dbe )
        {
            return DEFAULT_HTML_CONFIG;
        }
    }
    /**
     * Process the files
     * @param cortex the cortext MVD to accumulate files into
     * @param corcode the corcode MVD to accumulate files into
     * @return the log output
     */
    @Override
    public String process( Archive cortex, Archive corcode ) 
        throws ImporterException
    {
        try
        {
            if ( files.size() > 0 )
            {
                JSONObject jDoc = (JSONObject)JSONValue.parse( splitConfig );
                Splitter splitter = new Splitter( jDoc );
                for ( int i=0;i<files.size();i++ )
                {
                    File file = files.get(i);
                    String fileText = file.toString();
                    long startTime = System.currentTimeMillis();
                    Map<String,String> map = splitter.split( fileText );
                    long diff = System.currentTimeMillis()-startTime;
                    log.append("Split ");
                    log.append( file.name );
                    log.append(" in " );
                    log.append( diff );
                    log.append( " milliseconds into " );
                    log.append( map.size() );
                    log.append( " versions\n" );
                    Set<String> keys = map.keySet();
                    Iterator<String> iter = keys.iterator();
                    while ( iter.hasNext() )
                    {
                        String key = iter.next();
                        JSONResponse markup = new JSONResponse(JSONResponse.STIL);
                        JSONResponse text = new JSONResponse(JSONResponse.TEXT);
                        AeseStripper stripper = new AeseStripper();
                        String xml = map.get(key);
                        // extract the notes from the xml
                        String vid = extractVersionName(files.get(i).name);
                        NoteStripper ns = new NoteStripper();
                        // strip out the bona fide notes
                        xml = ns.strip( xml, docid, vid+"/"+key ); 
                        ArrayList notes = ns.getNotes();
                        int res = stripper.strip( xml, stripConfig, 
                            style, dict, hhExcepts, 
                            Utils.isHtml(xml), text, markup );
                        if ( res == 1 )
                        {
                            // save notes in database if any
                            if ( notes.size() > 0 )
                            {
                                Connection conn = Connector.getConnection();
                                if ( !vid.startsWith("/") )
                                    vid = "/"+vid;
                                conn.removeFromDbByExpr( Database.ANNOTATIONS, 
                                    JSONKeys.DOCID, docid+vid+"/.*");
                                RandomID rid = new RandomID();
                                String config = getHtmlConfig(conn);
                                JSONObject jObj = (JSONObject)JSONValue.parse(config);
                                if ( jObj != null )
                                {
                                    for ( int j=0;j<notes.size();j++ )
                                    {
                                        Annotation ann = (Annotation)notes.get(j);
                                        String json = ann.toJSONString(jObj);
                                        String annid = docid;
                                        if ( vid.startsWith("/") )
                                            annid += vid;
                                        else
                                            annid += "/"+vid;
                                        annid += "/"+rid.newKey();
                                        conn.putToDb(Database.ANNOTATIONS,annid,json);
                                    }
                                }
                                else
                                    System.out.println("Failed to load xml->html config");
                            }
                            if ( map.size()>1 )
                                vid += "/"+key;
                            //char[] chars = text.getBody().toCharArray();
                            //convertQuotes( chars );
                            //cortex.put( group+key, new String(chars).getBytes("UTF-8") );
                            StandoffPair pair = new StandoffPair(
                                markup.getBody(),text.getBody(),vid);
                            //System.out.println("text len="+text.getBody().length()+" xml length ="+xml.length());
                            convertCorcode( pair );
                            cortex.put( vid, pair.text.toCharArray() );
                            corcode.put( vid, pair.stil.toCharArray() );
                            if ( !verifyCorCode(pair.stil,pair.text) )
                                System.out.println("corcode of "+pair.vid+" was invalid");
                            log.append( "Stripped " );
                            log.append( file.name );
                            log.append("(");
                            log.append( key );
                            log.append(")");
                            log.append(" successfully\n" );
                        }
                        else
                        {
                            throw new ImporterException("Stripping of "
                                +files.get(i).name+" XML failed");
                        }
                    }
                }
            }
        }
        catch ( Exception e ) 
        {
            if ( e instanceof ImporterException )
                throw (ImporterException)e;
            else
                throw new ImporterException( e );
        }
        return log.toString();
    }
    boolean verifyCorCode(String stil, String text )
    {
        JSONObject jObj = (JSONObject)JSONValue.parse(stil);
        JSONArray ranges = (JSONArray)jObj.get(JSONKeys.RANGES);
        int offset = 0;
        for ( int i=0;i<ranges.size();i++ )
        {
            JSONObject range = (JSONObject)ranges.get(i);
            offset += ((Number)range.get("reloff")).intValue();
            int len = ((Number)range.get("len")).intValue();
            if ( offset+len > text.length() )
                return false;
        }
        return true;
    }
}
