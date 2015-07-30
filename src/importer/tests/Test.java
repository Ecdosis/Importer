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

package importer.tests;
import calliope.core.database.Connector;
import importer.exception.ImporterException;
import importer.handler.ImporterHandler;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import importer.constants.Params;
import calliope.core.constants.Database;
import calliope.core.constants.JSONKeys;
import calliope.exception.*;
import org.json.simple.JSONValue;
import org.json.simple.JSONObject;
import html.*;
/**
 *
 * @author desmond
 */
public abstract class Test extends ImporterHandler
{
    // names of tabs we support
    public static String tabs = "Home Compare Edition Html "
        +"Image Import Internal Table Tilt";
    private static String KING_LEAR = 
        "english/shakespeare/kinglear/act1/scene1";
    /** contains a leading slash */
    protected String version1;
    protected String description;
    private static String VLEN_KEY="<!--version-length:";
    protected HTML doc;
    /** ID of the document to test with */
    protected String docID;
    static String SPRY_PANEL_CSS = 
    ".TabbedPanels { margin: 0px; padding: 0px; float: left; clear"
    +": none; width: 100%; }\n.TabbedPanelsTabGroup { margin: 0px;"
    +" padding: 0px;}\n.TabbedPanelsTab { position: relative; top:"
    +" 1px; float: left;padding: 4px 10px; margin: 0px 1px 0px 0px"
    +"; font: bold 0.7em sans-serif;background-color: #DDD; list-s"
    +"tyle: none; border-left: solid 1px #CCC;border-bottom: solid"
    +" 1px #999; border-top: solid 1px #999;border-right: solid 1p"
    +"x #999; -moz-user-select: none;-khtml-user-select: none; cur"
    +"sor: pointer; }\n.TabbedPanelsTabHover { background-color: #"
    +"CCC; }.TabbedPanelsTabSelected { background-color: #EEE; bor"
    +"der-bottom: 1px solid #EEE;}\n.TabbedPanelsTab a { color: bl"
    +"ack; text-decoration: none; }\n.TabbedPanelsContentGroup { c"
    +"lear: both; border-left: solid 1px #CCC;border-bottom: 0px; "
    +"border-top: solid 1px #999;border-right: solid 1px #999; bac"
    +"kground-color: #EEE; }\n.TabbedPanelsContent { padding: 0px;"
    +" }\n.VTabbedPanels .TabbedPanelsTabGroup { float: left; widt"
    +"h: 10em;height: 20em; background-color: #EEE; position: rela"
    +"tive;border-top: solid 1px #999; border-right: solid 1px #99"
    +"9;border-left: solid 1px #CCC; border-bottom: 0px;}\n.VTabbe"
    +"dPanels .TabbedPanelsTab { float: none; margin: 0px;border-t"
    +"op: none; border-left: none; border-right: none; }\n.VTabbed"
    +"Panels .TabbedPanelsTabSelected { background-color: #EEE;bor"
    +"der-bottom: solid 1px #999;}\n.VTabbedPanels .TabbedPanelsCo"
    +"ntentGroup { clear: none; float: left;padding: 0px; width: 3"
    +"0em; height: 20em;}\nh1 {color:darkgrey;font-variant: small-"
    +"caps}";
    public Test()
    {
        // set default docID
        docID = KING_LEAR;
    }
    /**
     * Get the correct tab name from the first part of the urn
     * @param urn the urn to examine
     * @return the tab name
     */
    private String extractTabName( String urn )
    {
        int pos = tabs.indexOf(" ");
        String defTab = tabs.substring(0,pos);
        String tab = defTab;
        pos = urn.indexOf('/', 1);
        if ( pos != -1 )
        {
            tab = urn.substring(pos+1);
            if ( tab.length()>0 )
            {
                if ( tab.endsWith("/") )
                    tab = tab.substring(0,tab.length()-1);
                if ( tab.length()>0 )
                    tab = Character.toUpperCase(tab.charAt(0))+tab.substring(1);
                else
                    tab = defTab;
            }
            else
                tab = defTab;
        }
        return tab;
    }
    /**
     * Save the given param as a hidden input field within the form if any
     * @param doc the doc with a form to save it in
     * @param key the param's key
     * @param value its value
     */
    void rememberParam( Element doc, String key, String value )
    {
        Element form = doc.getElementByTagName( HTMLNames.FORM );
        Element child = doc.getElementById( key );
        if ( form != null && child == null )
        {
            Element elem = new Element( HTMLNames.INPUT );
            elem.addAttribute( HTMLNames.TYPE, HTMLNames.HIDDEN );
            elem.addAttribute( HTMLNames.NAME, key );
            elem.addAttribute( HTMLNames.ID, key );
            elem.addAttribute( HTMLNames.VALUE, value );
            form.addChild( elem );
        }
        // else not required or already set
    }
    /**
     * Get the docID of the first document you find in the database
     * @return its docID
     * @throws AeseException if there were no docIDs
     */
    private String getDefaultDocID() throws AeseException
    {
        try
        {
            String json = Connector.getConnection().getFromDb(
                Database.CORTEX,KING_LEAR );
            if ( json != null )
                return KING_LEAR;
            else
            {
                String[] docs = 
                    Connector.getConnection().listCollection(Database.CORTEX);
                if ( docs.length==0 )
                    throw new AeseException( "No docs in database" );
                else
                    return docs[0];
            }
        }
        catch ( Exception e )
        {
            throw new AeseException( e );
        }
    }
    /**
     * Get the default version1 for the current docID
     * @return the name of version1 
     * @throws AeseException if there was no such field
     */
    private String getDefaultVersion1() throws AeseException
    {
        try
        {
            String json = Connector.getConnection().getFromDb(
                Database.CORTEX, docID );
            if ( json != null )
            {
                JSONObject doc = (JSONObject)JSONValue.parse( json );
                if ( !doc.containsKey(JSONKeys.VERSION1) )
                    throw new AeseException("Doc "+docID
                        +" does not contain a "+JSONKeys.VERSION1+" field");
                return (String)doc.get(JSONKeys.VERSION1);
            }
            else 
                throw new AeseException("no docs in database");
        }
        catch ( Exception e )
        {
            throw new AeseException( e );
        }
    }
    /**
     * Set the docID from the request
     * @param request the http request
     */
    void setDocID( HttpServletRequest request ) throws AeseException
    {
        //String prevDocID = docID;
        if ( request.getParameter(Params.DOCID) != null )
            docID = request.getParameter(Params.DOCID);
        else
            docID = getDefaultDocID();
        String paramValue = request.getParameter(Params.VERSION1);
        if ( paramValue != null )
            //&& prevDocID!=null && docID.equals(prevDocID) )
            version1 = paramValue;
        else
            version1 = getDefaultVersion1();
    }
    /**
     * Scan the body returned by the formatter for the relevant CSS
     * @param body the body returned by a call to formatter
     * @return the doctored body
     */
    protected String extractCSSFromBody( String body )
    {
        String css = null;
        int pos1 = body.indexOf("<!--styles: ");
        int pos2 = body.indexOf("-->",pos1+12);
        if ( pos1 >= 0 && pos2 > 0 && pos1 < pos2 )
        {
            // skip "<!--styles: "
            css = body.substring( 12+pos1, pos2 );
            // header must NOT already be committed
            doc.getHead().addCss( css );
            String p1 = body.substring( 0, pos1 );
            String p2 = body.substring( pos2+3 );
            body = p1+p2;
        }
        return body;
    }
    /**
     * Scan the body returned by the formatter for the relevant CSS
     * @param body the body returned by a call to formatter
     * @return the version length (leave body as is)
     */
    protected int getLengthFromBody( String body, int dflt )
    {
        int length = dflt;
        int keylen = 4+VLEN_KEY.length();
        int pos1 = body.indexOf("<!--"+VLEN_KEY);
        if ( pos1 != -1 )
        {
            int pos2 = body.indexOf("-->",pos1+keylen);
            if ( pos2 > 0 )
            {
                // skip "<!--"
                String len = body.substring( pos1+keylen, pos2 );
                try
                {
                    length = Integer.parseInt( len.trim() );
                }
                catch ( Exception e )
                {
                    length = dflt;
                }
            }
        }
        return length;
    }
    /**
     * Generate a default DOCID input element 
     * @return the Element object for easy adding to the test page
     */
    protected Element defaultDocIDElem()
    {
        Element docIDElem = new Element( HTMLNames.INPUT );
        docIDElem.addAttribute( HTMLNames.TYPE, HTMLNames.HIDDEN );
        docIDElem.addAttribute( HTMLNames.ID, Params.DOCID );
        docIDElem.addAttribute( HTMLNames.NAME, Params.DOCID );
        docIDElem.addAttribute( HTMLNames.VALUE, 
            "english/shakespeare/kinglear/act1/scene1" );
        return docIDElem;
    }
    /**
     * Create the DOCID element
     * @param docID the docid value to be used
     * @return a hidden element
     */
    protected Element docIDHidden( String docID )
    {
        Element input = new Element(HTMLNames.INPUT);
        input.addAttribute( HTMLNames.TYPE, HTMLNames.HIDDEN);
        input.addAttribute( HTMLNames.NAME, Params.DOCID );
        input.addAttribute( HTMLNames.ID, Params.DOCID );
        input.addAttribute( HTMLNames.VALUE, docID );
        return input;
    }
    /**
     * Generate the default form element for the test
     * @param url the URL to link to this page
     * @return a form Element
     */
    protected Element formElement( String url )
    {
        Element form = new Element( HTMLNames.FORM );
        form.addAttribute( HTMLNames.NAME, HTMLNames.DEFAULT );
        form.addAttribute( HTMLNames.ID, HTMLNames.DEFAULT );
        form.addAttribute( HTMLNames.METHOD, HTMLNames.POST );
        form.addAttribute( HTMLNames.ACTION, url );
        return form;
    }
    /**
     * Display the test GUI, selecting the default Home tab
     * @param request the request to read from
     * @param urn the original URN - ignored
     * @return a formatted html String
     */
    @Override
    public void handle( HttpServletRequest request, 
        HttpServletResponse response, String urn ) throws ImporterException
    {
        try
        {
            TabPanel panel = new TabPanel();
            panel.setTestInstance( this );
            String[] children = Test.tabs.split(" ");
            for ( int i=0;i<children.length;i++ )
            {
                panel.addTab( children[i] );
            }
            // default to first subclass
            String name = children[0];// seems redundant
            name = extractTabName( urn );
            panel.setCurrent( name );
            setDocID( request );
            if ( doc == null )
                doc = new HTML();
            /*Element h1 = new Element( "h1" );
            h1.addText( "AeseServer Sample GUIs and Tests");
            doc.add( h1 );
            */
            doc.getHead().addCss(SPRY_PANEL_CSS);
            doc.addElement(panel );
            doc.build();
            //if ( version1 != null )
            //    rememberParam( doc, Params.VERSION1, version1 );
            if ( docID != null )
                rememberParam( doc, Params.DOCID, docID );
            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().println(doc.toString());
        }
        catch ( Exception e )
        {
            e.printStackTrace( System.out );
            HTML doc = new HTML();
            Element p = new Element("p");
            p.addText( "Error: "+e.getMessage() );
            doc.addElement( p );
            response.setContentType("text/html;charset=UTF-8");
            try 
            {
                response.getWriter().println(doc.toString());
            }
            catch ( Exception e2 )
            {
                throw new ImporterException( e2 );
            }
        }
    }
    /**
     * Get the content of this tab (mostly handled in subclasses)
     * If any nodes are added in overrides of this method call build on them 
     * @return an element
     */
    public Element getContent()
    {
        return new Text(description);
    }
    /**
     * Get the contents of a corform
     * @param corformId the path to the corform
     * @return a String or "" if not found
     */
    protected String getCorForm( String corformId )
    {
        try
        {
            String json = Connector.getConnection().getFromDb( 
                Database.CORFORM, corformId );
            JSONObject doc = (JSONObject)JSONValue.parse( json );
            return doc.get(JSONKeys.BODY).toString();
        }
        catch (Exception e )
        {
            return "";
        }
    }
    /**
     * Get the description string
     * @return a String
     */
    public String getDescription()
    {
        return description;
    }
    /**
     * Add a get param to an existing URL in string form
     * @param url the url in question
     * @param name the name of the parameter
     * @param value its value
     * @return the full url
     */
    protected String addGetParam( String url, String name, String value )
    {
        StringBuilder sb = new StringBuilder(url);
        if ( !url.contains("?") )
            sb.append("?");
        else
            sb.append("&");
        sb.append(name);
        sb.append("=");
        sb.append(value);
        return sb.toString();       
    }
}
