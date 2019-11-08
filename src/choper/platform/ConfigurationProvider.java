/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package choper.platform;

import choper.domain.flowSensors.FlowSensorYFS201b;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author max22
 */
public class ConfigurationProvider
{
    public static ConfigurationProvider Instance = new ConfigurationProvider();

    //private String ConfigurationFileName = "src/resources/config.properties";
    //private String CustomConfigurationFileName = "src/resources/custom.properties";
    private String ConfigurationFileName = "resources/config.properties";
    private String CustomConfigurationFileName = "resources/custom.properties";

    private Properties _properties;
    private Properties _customProperties;

    private String GetFilename(String fileNameOnly)
    {
        String workingDir = System.getProperty("user.dir");
        String filename = Path.of(workingDir, fileNameOnly).toString();
        Logger.getGlobal().log(Level.FINEST, filename);
        return filename;
    }

    private Properties GetProperties()
    {
        if (_properties == null)
        {

            Properties defaultProp = this.ReadProperties(ConfigurationFileName);
            Properties customProp = this.ReadProperties(CustomConfigurationFileName);

            Set<Entry<Object, Object>> entries = customProp.entrySet();

            for (Entry<Object, Object> entry : entries)
            {
                if (defaultProp.containsKey(entry.getKey()))
                {
                    defaultProp.replace(entry.getKey(), entry.getValue());
                }
                else
                {
                    defaultProp.put(entry.getKey(), entry.getValue());
                }

            }

            _properties = defaultProp;
            _customProperties = customProp;
        }

        return _properties;
    }

    private Properties GetCustomProperties()
    {
        if (_customProperties == null)
        {
            this.GetProperties();
        }

        return _customProperties;
    }

    private Properties ReadProperties(String file)
    {
        Properties aux = new Properties();
        String filename = this.GetFilename(file);
        FileInputStream ip = null;
        try
        {
            ip = new FileInputStream(filename);
            aux.load(ip);

        }
        catch (FileNotFoundException ex)
        {
            Logger.getGlobal().log(Level.SEVERE, null, ex);
        }
        catch (IOException ex)
        {
            Logger.getGlobal().log(Level.SEVERE, null, ex);
        }
        finally
        {
            if (ip != null)
            {
                try
                {
                    ip.close();
                }
                catch (IOException ex)
                {
                    Logger.getGlobal().log(Level.SEVERE, null, ex);
                }
            }
        }

        return aux;
    }

    public List<Entry<String, Object>> GetAll()
    {
        List<Entry<String, Object>> output = new ArrayList<>();
        Set<Entry<Object, Object>> entries = this.GetProperties().entrySet();

        for (Entry<Object, Object> entry : entries)
        {
            Entry<String, Object> item = new SimpleEntry<String, Object>(entry.getKey().toString(), entry.getValue());
            output.add(item);
        }

        Collections.sort(output, (c1, c2) ->  c1.getKey().compareTo(c2.getKey()));  

        return output;
    }

    public Integer GetInt(String key)
    {
        if (this.GetProperties().containsKey(key))
        {
            String val = this.GetProperties().get(key).toString();
            return Integer.parseInt(val);
        }
        else
        {
            return null;
        }
    }

    public Integer GetInt(String modulo, String key)
    {
        return this.GetInt(modulo + "." + key);
    }

    public Integer GetInt(Class modulo, String key)
    {
        return this.GetInt(this.getClassName(modulo) + "." + key);
    }

    public Integer GetInt(String modulo, String key, String subKey)
    {
        return this.GetInt(modulo + "." + key + "_" + subKey);
    }

    public Integer GetInt(Class modulo, String key, String subKey)
    {
        return this.GetInt(this.getClassName(modulo) + "." + key + "_" + subKey);
    }

    public Long GetLong(String key)
    {
        if (this.GetProperties().containsKey(key))
        {
            String val = this.GetProperties().get(key).toString();
            return Long.parseLong(val);
        }
        else
        {
            return null;
        }
    }

    public Long GetLong(String modulo, String key)
    {
        return this.GetLong(modulo + "." + key);
    }

    public Long GetLong(Class modulo, String key)
    {
        return this.GetLong(this.getClassName(modulo) + "." + key);
    }

    public Long GetLong(String modulo, String key, String subKey)
    {
        return this.GetLong(modulo + "." + key + "_" + subKey);
    }

    public Long GetLong(Class modulo, String key, String subKey)
    {
        return this.GetLong(this.getClassName(modulo) + "." + key + "_" + subKey);
    }

    public Float GetFloat(String key)
    {
        if (this.GetProperties().containsKey(key))
        {
            String val = this.GetProperties().get(key).toString();
            return Float.parseFloat(val);
        }
        else
        {
            return null;
        }
    }

    public Float GetFloat(Class modulo, String key)
    {
        return this.GetFloat(this.getClassName(modulo) + "." + key);
    }

    public Float GetFloat(Class modulo, String key, String subKey)
    {
        return this.GetFloat(this.getClassName(modulo) + "." + key + "_" + subKey);
    }

    public String GetString(String key)
    {
        return this.GetProperties().get(key).toString();
    }

    public String GetString(String modulo, String key)
    {
        return this.GetString(modulo + "." + key);
    }

    public String GetString(Class modulo, String key)
    {
        return this.GetString(this.getClassName(modulo) + "." + key);
    }

    public String GetString(String modulo, String key, String subKey)
    {
        return this.GetString(modulo + "." + key + "_" + subKey);
    }

    public String GetString(Class modulo, String key, String subKey)
    {
        return this.GetString(this.getClassName(modulo) + "." + key + "_" + subKey);
    }

    public Boolean GetBool(Class modulo, String key)
    {
        return this.GetBool(this.getClassName(modulo), key);
    }

    public Boolean GetBool(String modulo, String key)
    {
        return this.GetBool(modulo + "." + key);
    }

    public Boolean GetBool(String key)
    {
        if (this.GetProperties().containsKey(key))
        {
            String bool = this.GetProperties().get(key).toString();
            return Boolean.parseBoolean(bool);
        }
        else
        {
            return null;
        }
    }

    public void Save(String modulo, String key, Object value)
    {
        this.Save(modulo + "." + key, value);
    }

    public void Save(Class modulo, String key, Object value)
    {
        this.Save(this.getClassName(modulo) + "." + key, value);
    }

    public void Save(String modulo, String key, String subKey, Object value)
    {
        this.Save(modulo + "." + key + "_" + subKey, value);
    }

    public void Save(Class modulo, String key, String subKey, Object value)
    {
        this.Save(this.getClassName(modulo) + "." + key + "_" + subKey, value);
    }

    public void Save(String key, Object value)
    {
        if (this.GetProperties().containsKey(key))
        {
            this.GetProperties().replace(key, value.toString());
        }
        else
        {
            this.GetProperties().put(key, value.toString());
        }

        if (this.GetCustomProperties().containsKey(key))
        {
            this.GetCustomProperties().replace(key, value.toString());
        }
        else
        {
            this.GetCustomProperties().put(key, value.toString());
        }

        FileOutputStream o = null;
        try
        {

            o = new FileOutputStream(this.GetFilename(CustomConfigurationFileName));
            this.GetCustomProperties().store(o, null);
        }
        catch (FileNotFoundException ex)
        {
            Logger.getLogger(ConfigurationProvider.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (IOException ex)
        {
            Logger.getLogger(ConfigurationProvider.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally
        {
            try
            {
                o.close();
            }
            catch (IOException ex)
            {
                Logger.getLogger(ConfigurationProvider.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    private String getClassName(Class c)
    {
        int len = c.getPackageName().length();
        return c.getName().substring(len + 1);
    }

}
