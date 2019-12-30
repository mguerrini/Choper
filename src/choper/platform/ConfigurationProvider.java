/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package choper.platform;

import choper.domain.flowSensors.FlowSensorYFS201b;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Path;
import java.security.CodeSource;
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
    //private String TemporalConfigurationFileName = "src/resources/temporal.properties";
    private String DefaultConfigurationFileName = "resources/config.properties";
    private String CustomConfigurationFileName = "resources/custom.properties";
    private String TemporalConfigurationFileName = "resources/temporal.properties";

    private Properties _defaultProperties;
    private Properties _customProperties;
    private Properties _temporalProperties;

    private Properties _currentProperties;
    private boolean IsTemporal = false;

    public ConfigurationProvider()
    {
        this.LoadProperties();
        this.SetCurrentProperties(_defaultProperties, _customProperties);
    }

    private void SetCurrentProperties(Properties... properties)
    {

        _currentProperties = new Properties();

        for (int i = 0; i < properties.length; i++)
        {
            Set<Entry<Object, Object>> entries = properties[i].entrySet();

            for (Entry<Object, Object> entry : entries)
            {
                if (_currentProperties.containsKey(entry.getKey()))
                {
                    _currentProperties.replace(entry.getKey(), entry.getValue());
                }
                else
                {
                    _currentProperties.put(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    private void LoadProperties()
    {
        Properties defaultProp = this.ReadProperties(DefaultConfigurationFileName);
        Properties customProp = this.ReadProperties(CustomConfigurationFileName);
        /*
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
         */
        _defaultProperties = defaultProp;
        _customProperties = customProp;
    }

    private Properties ReadProperties(String file)
    {
        Properties aux = new Properties();
        String filename = this.GetFullFileName(file);
        Logger.getGlobal().log(Level.INFO, filename);

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

    private String GetFullFileName(String filenameOnly)
    {
        try
        {
            String path = this.GetContainerFolder(this.getClass());

            //String workingDir = System.getProperty("user.dir");
            //String filename = Path.of(workingDir, filenameOnly).toString();
            String filename = Path.of(path, filenameOnly).toString();
            System.out.println("Configuration File: " + filename);
            return filename;
        }
        catch (Exception ex)
        {
            Logger.getLogger(ConfigurationProvider.class.getName()).log(Level.SEVERE, null, ex);
            return filenameOnly;
        }
    }

    private String GetContainerFolder(Class aclass) throws Exception
    {
        CodeSource codeSource = aclass.getProtectionDomain().getCodeSource();

        File jarFile;

        if (codeSource.getLocation() != null)
        {
            jarFile = new File(codeSource.getLocation().toURI().getPath());
        }
        else
        {
            String path = aclass.getResource(aclass.getSimpleName() + ".class").getPath();
            String jarFilePath = path.substring(path.indexOf(":") + 1, path.indexOf("!"));
            jarFilePath = URLDecoder.decode(jarFilePath, "UTF-8");
            jarFile = new File(jarFilePath);
        }

        if (jarFile.getAbsolutePath().endsWith(".jar"))
        {
            return jarFile.getParentFile().getAbsolutePath();
        }
        else
        {
            return jarFile.getPath();
        }

    }

    private Properties GetProperties()
    {
        return _currentProperties;
    }

    public void BeginTemporalConfiguration()
    {
        this.IsTemporal = true;
    }

    public void CancelTemporalConfiguration()
    {
        this.SetCurrentProperties(_defaultProperties, _customProperties);

        this.IsTemporal = false;
    }

    public void FinishTemporalConfiguration()
    {
        //recorro la configuracion y actualizo la custom
        List<Entry<String, Object>> entries = this.GetAll();

        for (Entry<String, Object> entry : entries)
        {
            this.SetCustomProperties(entry.getKey(), entry.getValue());
        }

        this.DoSave(_customProperties, CustomConfigurationFileName);

        this.SetCurrentProperties(_defaultProperties, _customProperties);

        this.IsTemporal = false;
    }

    public boolean Exists(String module, String key)
    {
        return this.GetProperties().containsKey(module + "_" + key);
    }

    public boolean Exists(String fullkey)
    {
        return this.GetProperties().containsKey(fullkey);
    }

    public List<Entry<String, Object>> GetAll(String module)
    {
        List<Entry<String, Object>> all = this.GetAll();
        List<Entry<String, Object>> output = new ArrayList<>();

        for (Entry<String, Object> entry : all)
        {
            if (entry.getKey().startsWith(module + "."))
            {
                Entry<String, Object> item = new SimpleEntry<String, Object>(entry.getKey().toString(), entry.getValue());
                output.add(item);
            }
        }

        return output;
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

        Collections.sort(output, (c1, c2) -> c1.getKey().compareTo(c2.getKey()));

        return output;
    }

    public List<Entry<String, Object>> GetAll(String module, String key)
    {
        List<Entry<String, Object>> all = this.GetAll();
        List<Entry<String, Object>> output = new ArrayList<>();

        for (Entry<String, Object> entry : all)
        {
            if (entry.getKey().startsWith(module + "." + key + "_") || entry.getKey().equals(module + "." + key))
            {
                Entry<String, Object> item = new SimpleEntry<String, Object>(entry.getKey().toString(), entry.getValue());
                output.add(item);
            }
        }

        return output;
    }

    public List<Entry<String, Object>> GetAll(String module, String key, String subKey)
    {
        List<Entry<String, Object>> all = this.GetAll();
        List<Entry<String, Object>> output = new ArrayList<>();

        for (Entry<String, Object> entry : all)
        {
            if (entry.getKey().startsWith(module + "." + key + "_" + subKey + "_") || entry.getKey().equals(module + "." + key + "_" + subKey))
            {
                Entry<String, Object> item = new SimpleEntry<String, Object>(entry.getKey().toString(), entry.getValue());
                output.add(item);
            }
        }

        return output;
    }

    public Integer GetInt(String key)
    {
        if (this.GetProperties().containsKey(key))
        {
            String val = this.GetProperties().get(key).toString().trim();
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
            String val = this.GetProperties().get(key).toString().trim();
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
            String val = this.GetProperties().get(key).toString().trim();
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

    public Float GetFloat(String modulo, String key, String subKey)
    {
        return this.GetFloat(modulo + "." + key + "_" + subKey);
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

    public Boolean GetBool(String modulo, String key, String subKey)
    {
        return this.GetBool(modulo + "." + key + "_" + subKey);
    }

    public Boolean GetBool(String key)
    {
        if (this.GetProperties().containsKey(key))
        {
            String bool = this.GetProperties().get(key).toString().trim();
            return Boolean.parseBoolean(bool);
        }
        else
        {
            return null;
        }
    }

    public Object Get(String key)
    {
        if (this.GetProperties().containsKey(key))
        {
            return this.GetProperties().get(key);
        }
        else
        {
            return null;
        }
    }

    public Object Get(String module, String key, String subkey)
    {
        if (this.GetProperties().containsKey(module + "." + key + "_" + subkey))
        {
            return this.GetProperties().get(key);
        }
        else
        {
            return null;
        }
    }

    public Object Get(String module, String key)
    {
        if (this.GetProperties().containsKey(module + "." + key))
        {
            return this.GetProperties().get(key);
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

    private void SetValue(String key, Object value, boolean isTemporal, boolean save)
    {
        if (value == null)
        {
            return;
        }

        if (_currentProperties.containsKey(key))
        {
            _currentProperties.replace(key, value.toString());
        }
        else
        {
            _currentProperties.put(key, value.toString());
        }

        String fileName;
        Properties prop;

        if (isTemporal)
        {
            fileName = this.TemporalConfigurationFileName;
            prop = this._currentProperties;

            //no hago nada...ya esta en currentProperties.
        }
        else
        {
            fileName = this.CustomConfigurationFileName;
            prop = this._customProperties;

            this.SetCustomProperties(key, value);
        }

        if (save && !isTemporal)
        {
            this.DoSave(prop, fileName);
        }
    }

    private void SetCustomProperties(String key, Object value)
    {
        if (this._defaultProperties.containsKey(key))
        {
            Object curr = _defaultProperties.get(key);
            if (curr.equals(value))
            {
                _customProperties.remove(key);
            }
            else
            {
                if (this._customProperties.containsKey(key))
                {
                    this._customProperties.replace(key, value.toString());
                }
                else
                {
                    this._customProperties.put(key, value.toString());
                }
            }
        }
        else
        {
            if (this._customProperties.containsKey(key))
            {
                this._customProperties.replace(key, value.toString());
            }
            else
            {
                this._customProperties.put(key, value.toString());
            }
        }
    }

    public void Save(String key, Object value)
    {
        this.SetValue(key, value, this.IsTemporal, true);
    }

    private void DoSave(Properties prop, String filename)
    {
        FileOutputStream o = null;
        String fullfilename = this.GetFullFileName(filename);
        try
        {
            o = new FileOutputStream(fullfilename);
            prop.store(o, null);
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
