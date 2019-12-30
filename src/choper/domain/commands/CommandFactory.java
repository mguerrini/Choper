/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package choper.domain.commands;

import choper.platform.ConfigurationProvider;
import java.util.*;
import java.util.Map.Entry;

/**
 *
 * @author mguerrini
 */
public class CommandFactory
{
    public static CommandFactory Instance = new CommandFactory();

    public List<ICommand> CreateAll()
    {
        Map<String, Object> map = new HashMap<>();
        List<Entry<String, Object>> configs = ConfigurationProvider.Instance.GetAll("Command");
        for (Entry<String, Object> entry : configs)
        {
            String name = entry.getKey().replace("Command.", "");
            int index = name.indexOf("_");
            name = name.substring(0, index);
            map.put(name, name);
        }

        List<ICommand> output = new ArrayList<ICommand>();
        for (Entry<String, Object> entry : map.entrySet())
        {
            ICommand cmd = this.Create(entry.getKey());
            if (cmd != null)
            {
                output.add(cmd);
            }
        }

        return output;
    }

    public ICommand Create(String cmdName)
    {
        String type = ConfigurationProvider.Instance.GetString("Command", cmdName, "Type");
        if (type == null)
        {
            return null;
        }

        ICommand cmd;
        switch (type)
        {
            case "Manual":
                cmd = new ManualCommand(cmdName);
                break;

            case "GPIO":
                cmd = new GpioCommand(cmdName);
                break;

            case "I2C":
                cmd = new I2CCommand(cmdName);
                break;

            default:
                cmd = new ManualCommand(cmdName);
                break;
        }
        
        return cmd;
    }
}
