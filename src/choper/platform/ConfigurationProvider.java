/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package choper.platform;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author max22
 */
public class ConfigurationProvider
{
    private String ConfigurationFileName = "resources/config.properties";
    private Properties _properties;
    
    private Properties GetProperties() 
    {
        if (_properties == null)
        {
            Properties aux = new Properties();
            String workingDir = System.getProperty("user.dir");
            String filename = Path.of(workingDir, ConfigurationFileName).toString();
            
            try
            {
                FileInputStream ip= new FileInputStream(filename);
                aux.load(ip);
                
                _properties = aux;
            }
            catch (FileNotFoundException ex)
            {
                Logger.getLogger(ConfigurationProvider.class.getName()).log(Level.SEVERE, null, ex);
            }
            catch (IOException ex)
            {
                Logger.getLogger(ConfigurationProvider.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return _properties;
    }
    
    public int GetInt(String key){
        return 0;
    }
    
    public  String GetString(String key){
        return "";
    }
}
