/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package choper;

import choper.domain.ChoperMachine;
import choper.domain.Environment;
import choper.platform.ConfigurationProvider;
import choper.platform.strings.StringUtil;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mguerrini
 */
public class ChoperConsole
{
    public ChoperConsole()
    {

    }

    private ChoperMachine Machine;
    private boolean Finish = false;

    public void Start()
    {
        Environment.Configure();

        this.Machine = new ChoperMachine();
        this.Machine.Init();
        this.Machine.Connect();

        String cmd = "";
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        while (!this.Finish)
        {
            try
            {
                cmd = reader.readLine();
                this.Finish = "quit".equalsIgnoreCase(cmd) || "exit".equalsIgnoreCase(cmd);

                if (this.Finish)
                {
                    continue;
                }

                String[] split = cmd.split(" ");

                if (StringUtil.IsNullOrEmpty(split[0]))
                {
                    System.out.println("Comando inválido!");
                    continue;
                }

                if (split[0].equalsIgnoreCase("clear"))
                {
                    this.ShowConfiguration(split);
                }
                else if (split[0].equalsIgnoreCase("card"))
                {
                    this.ProcessCardCommands(split);
                }
                else if (split[0].equalsIgnoreCase("config"))
                {
                    this.ShowConfiguration(split);
                }
                else if (split[0].equalsIgnoreCase("reset"))
                {
                    this.Machine.Reset();
                }
                else if (split[0].equalsIgnoreCase("set"))
                {
                    this.ProcessSetCommand(split);
                }
                else if (split[0].equalsIgnoreCase("get"))
                {
                    this.ProcessGetCommand(split);
                }
                else
                {
                    System.out.println("Comando inválido!");
                }
            }
            catch (Exception ex)
            {
                System.out.println(ex);
            }
        }

        this.Machine.Disconnect();
    }

    private void ShowConfiguration(String[] parameters)
    {
        List<Entry<String, Object>> values = ConfigurationProvider.Instance.GetAll();
        System.out.println("======== Configuration ========");
        for (Entry<String, Object> item : values)
        {
            System.out.println(item.getKey() + " = " + item.getValue());
        }
        System.out.println("======== Fin Configuration ========");
    }

    private void ProcessCardCommands(String[] parameters)
    {
        if (parameters.length < 2)
        {
            System.out.println("Comando inválido!");
            return;
        }

        String key = parameters[1];

        if (StringUtil.IsNullOrEmpty(key))
        {
            System.out.println("Comando inválido!");
            return;
        }

        Float value = null;
        if (key.equalsIgnoreCase("clear") || key.equalsIgnoreCase("clean"))
        {
            this.Machine.CleanMoney();
        }
        else if (key.equalsIgnoreCase("bal") || key.equalsIgnoreCase("balance"))
        {
            value = this.Machine.GetBalance();
            if (value != null)
            {
                System.out.println("Saldo: $" + value.toString());
            }
        }
        else
        {
            if (parameters.length < 3)
            {
                System.out.println("Comando inválido!");
                return;
            }
            
            value = Float.parseFloat(parameters[2]);
            if (value == null)
            {
                System.out.println("Comando inválido!");
                return;
            }

            if (key.equalsIgnoreCase("add"))
            {
                this.Machine.AddMoney(value);
            }
            else if (key.equalsIgnoreCase("sub") || key.equalsIgnoreCase("substract"))
            {
                this.Machine.SubstractMoney(value);
            }
        }

    }

    private void ProcessGetCommand(String[] parameters)
    {
        String key = parameters[1];

        if (StringUtil.IsNullOrEmpty(key))
        {
            return;
        }

        if (key.equalsIgnoreCase("price") || key.equalsIgnoreCase("ChoperMachine.Price"))
        {
            Float value = ConfigurationProvider.Instance.GetFloat(ChoperMachine.class, "Price");
            System.out.println("Valor por litro: $" + value.toString());
        }
        else if (key.equalsIgnoreCase("PulsesPerLiter") || key.equalsIgnoreCase("FlowSensor.PulsesPerLiter"))
        {
            Integer value = ConfigurationProvider.Instance.GetInt("FlowSensor.PulsesPerLiter");
            System.out.println("Pulsos por litro: " + value.toString());
        }
        else if (key.equalsIgnoreCase("loglevel"))
        {
            System.out.println("Log Level: " + Logger.getGlobal().getLevel());
        }
    }

    private void ProcessSetCommand(String[] parameters)
    {
        String key = parameters[1];
        String value = parameters[2];

        if (StringUtil.IsNullOrEmpty(key) || StringUtil.IsNullOrEmpty(value))
        {
            return;
        }

        if (key.equalsIgnoreCase("price") || key.equalsIgnoreCase("ChoperMachine.Price"))
        {
            try
            {
                Float price = Float.parseFloat(value);
                ConfigurationProvider.Instance.Save(ChoperMachine.class, "Price", price);
                price = ConfigurationProvider.Instance.GetFloat(ChoperMachine.class, "Price");
                System.out.println("Nuevo Valor por litro: $" + price.toString());

                this.Machine.UpdateParameters();
            }
            catch (Exception ex)
            {
                System.out.println("Valor inválido");
            }
        }
        else if (key.equalsIgnoreCase("PulsesPerLiter") || key.equalsIgnoreCase("FlowSensor.PulsesPerLiter"))
        {
            try
            {
                Integer pulses = Integer.parseInt(value);
                ConfigurationProvider.Instance.Save("FlowSensor.PulsesPerLiter", pulses);
                pulses = ConfigurationProvider.Instance.GetInt("FlowSensor.PulsesPerLiter");
                System.out.println("Nueva cantidad de pulsos por litro: " + pulses.toString());

                this.Machine.UpdateParameters();
            }
            catch (Exception ex)
            {
                System.out.println("Valor inválido");
            }
        }
        else if (key.equalsIgnoreCase("loglevel"))
        {
            try
            {
                Level lvl = Level.parse(parameters[1]);
                Logger.getGlobal().setLevel(lvl);
                System.out.println("Log Level: " + Logger.getGlobal().getLevel());
            }
            catch (Exception ex)
            {
                System.out.println("Valor inválido");
            }
        }
    }

    public void Stop()
    {
        this.Finish = true;
        try
        {
            System.console().reader().close();
        }
        catch (IOException ex)
        {
            Logger.getLogger(ChoperConsole.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
