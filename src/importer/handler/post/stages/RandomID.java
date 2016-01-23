/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package importer.handler.post.stages;
import java.util.HashSet;
import java.util.UUID;
/**
 *
 * @author desmond
 */
public class RandomID {
    static int KEY_LEN = 8;
    HashSet<String> keys;
    public RandomID()
    {
        keys = new HashSet<String>();
    }
    String newKey()
    {
        String longKey = UUID.randomUUID().toString();
        String shortKey = longKey.substring(0,6);
        while ( keys.contains(shortKey) )
        {
            longKey = UUID.randomUUID().toString();
            shortKey = longKey.substring(0,KEY_LEN);
        }
        keys.add( shortKey );
        return shortKey;
    }
}
