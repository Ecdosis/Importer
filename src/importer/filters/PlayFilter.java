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

package importer.filters;
import importer.Archive;
import java.util.StringTokenizer;
import java.util.HashSet;
import importer.exception.ImporterException;
import calliope.json.corcode.Range;
import org.json.simple.JSONObject;
import java.util.ArrayList;

/**
 * Import a plain text play
 * 1. Skim through the file looking at all words other than those at the 
 * start of lines or after "\.\s+". Put those words into a dictionary, 
 * but don't add any words that start with a capital.
 * 2. Look at line starts. Recover all words starting with a capital and 
 * ending in . or : followed by \s. Lowercase the word. If it does not 
 * appear in the word list calculate in step 1 then it is a speaker.
 * @author desmond
 */
public class PlayFilter extends Filter
{
    HashSet<String> words;
    HashSet<String> speakers;
    HashSet<String> stageKeys;
    int maxLineSyllables;
    static String vowels;
    static String trailing;
    char speakerEnd;
    String sentenceEnd;
    String[] defaultStageKeys = {"Enter","Exit","Sennet"};
    public PlayFilter()
    {
        super();
        words = new HashSet<String>();
        speakers = new HashSet<String>();
        stageKeys = new HashSet<String>();
    }
    /**
     * This MUST be called before calling the filter
     * @param config parameters appropriate for this type of play
     */
    public void configure( JSONObject config )
    {
        maxLineSyllables = (Integer)config.get( PlayConfig.MAXLINE_SYLLABLES );
        vowels = (String)config.get( PlayConfig.VOWELS );
        trailing = (String)config.get( PlayConfig.TRAILING );
        String se = (String)config.get( PlayConfig.SPEAKER_END );
        speakerEnd = se.charAt(0);
        sentenceEnd = (String)config.get( PlayConfig.SENTENCE_END );
        ArrayList array = (ArrayList)config.get( PlayConfig.STAGE_KEYS );
        for ( int i=0;i<array.size();i++ )
            stageKeys.add( (String)array.get(i) );
    }
    @Override
    public String getDescription()
    {
        return "General play filter";
    }
    /**
     * Remove non-letter and hyphen chars from the string
     * @param input the raw punctuated word
     * @return a punctuation-free word (but with hyphens)
     */
    String strip( String input )
    {
        StringBuilder sb = new StringBuilder();
        for ( int i=0;i<input.length();i++ )
        {
            char token = input.charAt(i);
            if ( Character.isLetter(token)||token == '-' )
                sb.append( token );
        }
        return sb.toString();
    }
    /**
     * Is the toke at what looks like sentence-end?
     * @param token the token with perhaps embedded punctuation
     * @return true if it is
     */
    boolean endOfSentence( String token )
    {
        char last = token.charAt(token.length()-1);
        return last == '.' || last=='!'||last=='?';
    }
    /**
     * Is the token an ordinary word, i.e. not a name?
     * @param token the token
     * @return true if it's a word
     */
    boolean isWord( String token )
    {
        return Character.isLowerCase(token.charAt(0));
    }
    /**
     * Construct a list of what you know are definitely words
     * @param input the input text
     * @return error log
     */
    private String buildWordList( String input )
    {
        StringTokenizer st = new StringTokenizer( input, "\n\t ", true );
        int state = 0;
        while ( st.hasMoreElements() )
        {
            String token = st.nextToken();
            if ( token.length()==0 )
                break;
            switch ( state )
            {
                // at line-start
                case 0:
                    if ( !Character.isWhitespace(token.charAt(0)) )
                    {
                        if ( endOfSentence(token) )
                            state = 2;
                        else
                            state = 1;
                        token = strip( token );
                        if ( token.length()==0 )
                            break;
                        if ( isWord(token) &&!words.contains(token) )
                            words.add( token );
                    }
                    break;
                    //not at line-start, not after full stop
                case 1:
                    if ( !Character.isWhitespace(token.charAt(0)) )
                    {
                        if ( endOfSentence(token) )
                            state = 2;
                        token = strip( token );
                        if ( token.length()==0 )
                            break;
                        if ( isWord(token) )
                        {
                            if ( !words.contains(token) )
                                words.add( token );
                        }
                    }
                    else if ( token.equals("\n") )
                        state = 0;
                    break;
                    // after sentence end
                case 2:
                    if ( !Character.isWhitespace(token.charAt(0)) )
                    {
                        if ( !endOfSentence(token) )
                            state = 1;
                        token = strip( token );
                        if ( token.length()==0 )
                            break;
                        if ( isWord(token) )
                        {
                            if ( !words.contains(token) )
                                words.add( token );
                        }
                    }
                    else if ( token.equals("\n") )
                        state = 0;
                    break;
            }
        }
        // this will do for now
        return "";
    }
    /** 
     * Is a sentence starting with a speaker?
     * @param sentence the sentence in question
     * @return true if it is a speech
     */
    boolean isSpeech( String sentence )
    {
        String[] words = sentence.split("[\n\t ]");
        for ( int i=0;i<words.length;i++ )
        {
            String word = words[i]+speakerEnd;
            if ( speakers.contains(words[i])||speakers.contains(word) )
                return true;
            else if ( words[i].length()>0 )
                return false;
        }
        return false;
    }
    /**
     * Look at line starts. Recover all words starting with a capital and 
     * ending in . or : followed by \s. Lowercase the word. If it occurs in 
     * the configured stagekeys then it is a stage direction.
     * @param input the input text
     * @return the log
     */
    private String identifyStageDirections( String input ) 
        throws ImporterException
    {
        try
        {
            String[] sentences = input.split( sentenceEnd );
            int offset = 0;
            int stageStart = -1;
            int state = 0;
            int len = 0;
            for ( int i=0;i<sentences.length;i++ )
            {
                char[] current = sentences[i].toCharArray();
                String word1 = firstWord(sentences[i],true);
                int extra = (i<sentences.length-1)?1:0;
                switch ( state )
                {
                    case 0:
                        if ( stageKeys.contains(word1) )
                        {
                            stageStart = offset;
                            state = 1;
                            len = current.length+extra;
                        }
                        break;
                    case 1:
                        if ( !isSpeech(sentences[i]) )
                            len += current.length+extra;
                        else 
                        {
                            if ( current.length>0 )
                                markup.add( "stage", stageStart, len );
                            /*System.out.println("identified stage direction "
                                +input.substring(stageStart,stageStart+len));*/
                            state = 0;
                            len = -1;
                        }
                        break;
                }
                offset += current.length+1;
            }
            if ( state == 1 && len > 0 )
                markup.add( "stage", stageStart, len );
            return "";
        }
        catch ( Exception e )
        {
            throw new ImporterException( e );
        }
    }
    /**
     * Can the first word of a sentence be a speaker name?
     * @param sp the first word of the sentence
     * @return true if it's a speaker
     */
    boolean canBeSpeaker( String sp )
    {
        char last = sp.charAt(sp.length()-1);
        return last == speakerEnd;
    }
    /**
     * Look at line starts. Recover all words starting with a capital and 
     * ending in speakerEnd. Lowercase the word. If it does not 
     * appear in the word list calculated in step 1 then it is a speaker.
     */
    private String identifySpeakers( String input ) throws ImporterException
    {
        try
        {
            StringBuilder log = new StringBuilder();
            String[] lines = input.split("\n");
            int offset = 0;
            for ( int i=0;i<lines.length;i++ )
            {
                int index = 0;
                byte[] current = lines[i].getBytes(ENC);
                for ( int j=0;j<lines[i].length();j++ )
                {
                    char token = lines[i].charAt(j);
                    if ( token==speakerEnd || j==lines[i].length()-1 )
                    {
                        index = j+1;
                        break;
                    }
                    else if ( token==' '||token=='\t' ) 
                    {
                        index = j;
                        break;
                    }
                }
                if ( index > 0 )
                {
                    String speaker = lines[i].substring( 0, index );
                    if ( canBeSpeaker(speaker) )
                    {
                        String stripped = strip(speaker);
                        String token = stripped.toLowerCase();
                        if ( !words.contains(token) && !stageKeys.contains(stripped) )
                        {
                            speakers.add( speaker );
                            // record speaker in markup set
                            markup.add( "speaker", 
                                lines[i].indexOf(stripped)+offset, 
                                stripped.getBytes(ENC).length );
                        }
                    }
                }
                offset += current.length+1;
            }
            return log.toString();
        }
        catch ( Exception e )
        {
            throw new ImporterException( e );
        }
    }
    /**
     * Headings go at the top
     * @param input the input containing just the headings
     * @return the log output if any
     */
    String identifyHeadings( String input ) throws ImporterException
    {
        try
        {
            int pos = 0;
            String[] lines = input.split("\n");
            for ( int i=0;i<lines.length;i++ )
            {
                byte[] current = lines[i].getBytes(ENC);
                String trimmed = lines[i].trim();
                int local = lines[i].indexOf( trimmed );
                String leading = lines[i].substring(0,local);
                byte[] leadBytes = leading.getBytes(ENC);
                byte[] trimBytes = trimmed.getBytes(ENC);
                markup.add( "head", pos+leadBytes.length, trimBytes.length );
                pos += current.length+1;
            }
            return "";
        }
        catch ( Exception e )
        {
            throw new ImporterException( e );
        }
    }
    /**
     * Work out if we have a poetic line by counting syllables
     * @param line the line or paragraph to test
     * @return true if it is, else false
     */
    boolean itsALine( String line )
    {
        int state = 0;
        int numSyllables = 0;
        for ( int i=0;i<line.length();i++ )
        {
            char token = Character.toLowerCase(line.charAt(i));
            switch ( state )
            {
                case 0: // word start
                    if ( vowels.indexOf(token)!= -1 )
                        state = 1;
                    else if ( Character.isLetter(token) )
                        state = 2;
                    break;
                case 1: // general vowel state
                    if ( !Character.isLetter(token) )
                    {
                        numSyllables++;
                        state = 0;
                    }
                    else if ( vowels.indexOf(token)== -1 )
                    {
                    	state = 2;
                        numSyllables++;
                    }
                    break;
                case 2:// consonants
                    if ( trailing.indexOf(token)!=-1 )
                        state = 3;
                    else if ( vowels.indexOf(token)!=-1 )
                        state = 1;
                    else if ( !Character.isLetter(token) )
                        state = 0;
                    break;
                case 3:// trailing vowel after consonant
                    if ( !Character.isLetter(token) )
                        state = 0;
                    else if ( vowels.indexOf(token)!=-1 )
                        state = 1;
                    else
                    {
                        numSyllables++;
                        state = 2;
                    }
                    break;
            }
        }
        if ( state == 1 )
            numSyllables++;
        //System.out.println("detected "+numSyllables+" syllables");
        return numSyllables < maxLineSyllables;
    }
    /**
     * Get the first word of a string
     * @param text the text to get the first word of
     * @param stripPunctuation strip punctuation
     * @return the first word
     */
    String firstWord( String text, boolean stripPunctuation )
    {
        String word1 = text.trim();
        int pos = 0;
        for ( int i=0;i<word1.length();i++ )
        {
            char token = word1.charAt(i);
            if ( token==speakerEnd )
            {
                pos = i+1;
                break;
            }
            else if ( Character.isWhitespace(token) )
            {
                pos = i;
                break;
            }
        }
        if ( pos > 0 )
            word1 = word1.substring(0,pos);
        if ( stripPunctuation )
            return strip( word1 );
        else
            return word1;
    }
    /**
     * Is an entire line just whitespace?
     * @param line the line in question
     * @return true if it is
     */
    boolean isWhitespace( String line )
    {
        for ( int i=0;i<line.length();i++ )
        {
            if ( !Character.isWhitespace(line.charAt(i)) )
                return false;
        }
        return true;
    }
    /**
     * Identify the speaker and break up the body into lines or paragraphs
     * @param input the text of the speech
     * @param offset the offset of the speech
     * @return the log
     */
    String identifySpeech( String input, int offset ) throws ImporterException
    {
        try
        {
            int speakerOffset=0;
            byte[] speechBytes = input.getBytes(ENC);
            markup.add( "sp", offset, speechBytes.length );
            String word1 = firstWord(input,false);
            if ( speakers.contains(word1) )
            {
                byte[] spBytes = word1.getBytes(ENC);
                int leadIndex = input.indexOf(word1);
                if ( leadIndex > 0 )
                {
                    String leading = input.substring(0,leadIndex);
                    leadIndex += leading.getBytes(ENC).length;
                }
                speakerOffset=+spBytes.length+leadIndex;
                byte token = speechBytes[speakerOffset];
                while ( speakerOffset<speechBytes.length-1
                    &&(token==' '||token=='\t'||token=='\r'||token=='\n') )
                {
                    speakerOffset++;
                    token = speechBytes[speakerOffset];
                }
            }
            // speakerOffset points to the first non-speaker UTF-8 byte
            String[] lines = input.split("\n");
            for ( int i=0;i<lines.length;i++ )
            {
                byte[] current = lines[i].getBytes(ENC);
                // on other than the 1st line read from start of line
                if ( i>0 )
                    speakerOffset = 0;
                if ( current.length > speakerOffset )
                {
                    if ( itsALine(lines[i]) )
                    {
                        markup.add( "l", offset+speakerOffset, 
                            current.length-speakerOffset );
                    }
                    else if ( !isWhitespace(lines[i]) )
                        markup.add( "p", offset+speakerOffset, current.length );
                }
                // add 1 for removed CR
                offset += current.length+1;
            }
            return "";
        }
        catch ( Exception e )
        {
            throw new ImporterException( e );
        }
    }
    /**
     * Having identified the speakers and stage directions now identify the 
     * intervening speeches
     * @param input the text of the play
     * @return the log
     * @throws ImportException 
     */
    String identifySpeeches( String input ) throws ImporterException
    {
        StringBuilder log = new StringBuilder();
        Range prev = null;
        int rSize = markup.size();
        Range[] ranges = new Range[rSize];
        markup.toArray( ranges );
        for ( int i=0;i<rSize;i++ )
        {
            Range r = ranges[i];
            if ( r.name.equals("speaker")||r.name.equals("stage") )
            {
                if ( prev != null )
                    log.append( identifySpeech(
                        input.substring(prev.offset,r.offset),prev.offset) );
                if ( r.name.equals("stage") )
                    prev = null;
                else
                    prev = r;
            }
        }
        return log.toString();
    }
    /**
     * Convert to standoff properties
     * @param input the raw text input string
     * @param cortext a cortext mvd archive
     * @param corcode a corcode mvd archive
     * @return log output
     */
    @Override
    public String convert( String input, String name, Archive cortex, 
        Archive corcode ) throws ImporterException
    {
        init();
        if ( vowels==null )
            throw new ImporterException("configure required before convert");
        StringBuilder log = new StringBuilder();
        log.append( buildWordList(input) );
        log.append( identifySpeakers(input) );
        log.append( identifyStageDirections(input) );
        markup.sort();
        int offset = markup.getFirstOffset();
        if ( offset > 0 )
        {
            String headingZone = input.substring(0,offset );
            log.append( identifyHeadings(headingZone) );
            markup.sort();
        }
        log.append( identifySpeeches(input) );
        markup.sort();
        cortex.put( name, input.toCharArray() );
        corcode.put( name, markup.toSTILDocument().toString().toCharArray() );
        return log.toString();
    }
}
