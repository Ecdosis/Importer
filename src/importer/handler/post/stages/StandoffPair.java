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
//import calliope.constants.JSONKeys;
import org.json.simple.*;
import java.util.Stack;
import java.nio.charset.Charset;

/**
 * A wrapper for a TEXT + STIL markup pair
 * @author desmond
 */
public class StandoffPair {
    public String text;
    public String stil;
    public String vid;
    private byte[] data;
    private StringBuilder sb;
    private Stack<JSONObject> stack;
    StandoffPair( String stil, String text, String vid )
    {
        try
        {
            this.text = text;
            this.data = this.text.getBytes("UTF-8");
        }
        catch ( Exception e )
        {
            this.data = this.text.getBytes();
        }
        this.sb = new StringBuilder();
        this.stack = new Stack<JSONObject>();
        this.stil = stil;
        this.vid = vid;  
    }
    /**
     * Check if any ranges have now finished on the stack and pop them off
     * @param offset the current byte offset in data
     * @param lastWritePos the last position in data written to sb
     * @return the updated lastWritePos value
     */
    private int checkStack( int offset, int lastWritePos )
    {
        while ( !stack.isEmpty() 
            && ((Number)stack.peek().get("byteEnd")).intValue()<=offset )
        {
            JSONObject r = stack.pop();
            int rangeByteEnd = ((Number)r.get("byteEnd")).intValue();
            int rangeCharStart = ((Number)r.get("rangeStart")).intValue();
            r.remove("byteEnd");
            r.remove("rangeStart");
            if ( lastWritePos < rangeByteEnd )
            {
                String chunk = new String( data, lastWritePos, 
                    rangeByteEnd-lastWritePos, 
                    Charset.forName("UTF-8") );
                sb.append( chunk );
                lastWritePos = rangeByteEnd;
            }
            r.put("len",sb.length()-rangeCharStart);
        }
        return lastWritePos;
    }
    /**
     * Convert the raw byte-offsets to character offsets for Java
     * @param stil the original stil markup from C stripper
     * @return the stil document with character offsets and lengths
     * (not needed any more because stripper now uses character offsets)
    private String byteToCharacterOffsets( String stil )
    {
        try
        {
            JSONObject src = (JSONObject) JSONValue.parse(stil);
            JSONObject dst = new JSONObject();
            dst.put(JSONKeys.RANGES,new JSONArray());
            dst.put( JSONKeys.STYLE, src.get(JSONKeys.STYLE) );
            int offset = 0;
            int lastWritePos = 0;
            int lastRangeStart = 0;
            JSONArray ranges = (JSONArray)src.get(JSONKeys.RANGES);
            JSONArray newRanges = (JSONArray)dst.get(JSONKeys.RANGES);
            for ( int i=0;i<ranges.size();i++ )
            {
                JSONObject range = (JSONObject)ranges.get(i);
                offset += ((Number)range.get("reloff")).intValue();
                // check stack for ranges that have ended before or at offset
                lastWritePos = checkStack( offset, lastWritePos );
                if ( lastWritePos < offset )
                {
                    String chunk = new String( data, lastWritePos, 
                        offset-lastWritePos, 
                        Charset.forName("UTF-8") );
                    sb.append( chunk );
                    lastWritePos = offset;
                }
                // update reloff
                range.put("reloff",sb.length()-lastRangeStart);
                lastRangeStart = sb.length();
                // stuff these into range temporarily
                range.put("rangeStart",sb.length());
                range.put("byteEnd",offset+((Number)range.get("len")).intValue());
                newRanges.add(range);
                stack.push( range );
            }
            // coda
            checkStack( data.length, lastWritePos );
            this.stil = dst.toJSONString();
            if ( !verifyCorCode(this.stil,this.text) )
                System.out.println("corcode inside standoffpair of "+this.vid+" was invalid");
            return this.stil;
        }
        catch ( Exception e )
        {
            return stil;
        }
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
     */   
}
