/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package choper.platform.strings;

/**
 *
 * @author mguerrini
 */
public class StringUtil {
    
    public static boolean IsNullOrEmpty(String val)
    {
        if (val == null)
            return true;
        
        if (val.length() == 0)
            return true;
        
        return false;
    }
}
