/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package importer.handler.post.stages;
import calliope.AeseStripper;
import calliope.core.constants.Formats;
import org.w3c.tidy.Tidy;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import importer.Archive;
import calliope.json.JSONResponse;
/**
 * Import a series of HTML files
 * @author desmond
 */
public class Stage3HTML extends Stage
{
    String stripConfig;
    String style;
    String dict;
    String hhExcepts;
    String encoding;
    public Stage3HTML( Stage last, String style, String dict, 
        String hhExcepts, String encoding )
    {
        super();
        this.style = style;
        this.hhExcepts = hhExcepts;
        this.encoding = encoding;
        this.dict = (dict==null||dict.length()==0)?"en_GB":dict;
        for ( int i=0;i<last.files.size();i++ )
        {
            File f = last.files.get( i );
            if ( f.isHTML() )
                this.files.add( f );
        }
    }
    public String process( Archive cortex, Archive corcode )
    {
        Tidy t = new Tidy();
        t.setXmlOut( true );
        for ( int i=0;i<files.size();i++ )
        {
            try
            {
                byte[] data = files.get(i).data.getBytes(encoding);
                ByteArrayInputStream bis = new ByteArrayInputStream(data);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                System.out.println("Loading "+files.get(i).name);
                t.parseDOM( bis, bos );
                AeseStripper stripper = new AeseStripper();
                JSONResponse markup = new JSONResponse(JSONResponse.STIL);
                JSONResponse text = new JSONResponse(JSONResponse.TEXT);
                int res = stripper.strip( bos.toString(encoding), 
                    stripConfig, style, dict, hhExcepts, true,
                    text, markup );
                if ( res == 1 )
                {
                    String vid = "Base/";
                    vid += stripSuffix(files.get(i).name);
                    cortex.put( vid, text.getBody().toCharArray() );
                    corcode.put( vid, markup.getBody().toCharArray() );
                    log.append( "Stripped " );
                    log.append( files.get(i).name );
                    log.append(" successfully\n" );
                }
                else
                    log.append( "Stage3HTML: failed to strip "+files.get(i).name);
            }
            catch ( Exception e )
            {
                e.printStackTrace(System.out);
            }
        }
        return "";
    }
    public void setStripConfig( String configName )
    {
        this.stripConfig = configName;
    }
}
