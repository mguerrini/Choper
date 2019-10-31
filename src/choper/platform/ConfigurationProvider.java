/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package choper.platform;

import choper.domain.flowSensors.FlowSensorYFS201b;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author max22
 */
public class ConfigurationProvider
{
    public static ConfigurationProvider Instance = new ConfigurationProvider();

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
                FileInputStream ip = new FileInputStream(filename);
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

    public int GetInt(String key)
    {
        return (int) this.GetProperties().get(key);
    }

    public int GetInt(String modulo, String key)
    {
        return this.GetInt(modulo + "." + key);
    }

    public int GetInt(Class modulo, String key)
    {
        return this.GetInt(modulo.getName() + "." + key);
    }

    public int GetInt(String modulo, String key, String subKey)
    {
        return this.GetInt(modulo + "." + key + "_" + subKey);
    }

    public int GetInt(Class modulo, String key, String subKey)
    {
        return this.GetInt(modulo.getName() + "." + key + "_" + subKey);
    }

    public String GetString(String key)
    {
        return (String) this.GetProperties().get(key);
    }

    public String GetString(String modulo, String key)
    {
        return this.GetString(modulo + "." + key);
    }

    public String GetString(Class modulo, String key)
    {
        return this.GetString(modulo.getName() + "." + key);
    }

    public String GetString(String modulo, String key, String subKey)
    {
        return this.GetString(modulo + "." + key + "_" + subKey);
    }

    public String GetString(Class modulo, String key, String subKey)
    {
        return this.GetString(modulo.getName() + "." + key + "_" + subKey);
    }

    public boolean GetBool(Class module, String key)
    {
        return this.GetBool(module.getName(), key);

    }

    public boolean GetBool(String module, String key)
    {
        return (boolean) this.GetProperties().get(key);
    }

    public void Save(String modulo, String key, Object value)
    {
        this.Save(modulo + "." + key, value);
    }

    public void Save(String modulo, String key, String subKey, Object value)
    {
        this.Save(modulo + "." + key + "_" + subKey, value);
    }

    public void Save(String key, Object value)
    {
        if (this.GetProperties().containsKey(key))
        {
            this.GetProperties().replace(key, value);
        }
        else
        {
            this.GetProperties().put(key, value);
        }
    }

}
