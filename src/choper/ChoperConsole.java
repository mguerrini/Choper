/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package choper;

import choper.domain.ChoperMachine;
import choper.domain.ChoperStatusType;
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

        String cmdline = "";
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        while (!this.Finish)
        {
            try
            {
                //this.Machine.StartCalibration();
                //ChoperStatusType st = this.Machine.GetStatus();
                //System.out.println("Status: " + st.toString());

                cmdline = reader.readLine();
                this.Finish = "quit".equalsIgnoreCase(cmdline) || "exit".equalsIgnoreCase(cmdline);

                if (this.Finish)
                {
                    continue;
                }

                String[] split = cmdline.split(" ");
                if (StringUtil.IsNullOrEmpty(split[0]))
                {
                    System.out.println("Comando inválido!");
                    continue;
                }

                String cmd = split[0];

                if (cmd.equalsIgnoreCase("add")
                        || cmd.equalsIgnoreCase("sub")
                        || cmd.equalsIgnoreCase("bal")
                        || cmd.equalsIgnoreCase("balance")
                        || cmd.equalsIgnoreCase("cleancard")
                        || cmd.equalsIgnoreCase("clearcard")
                        || cmd.equalsIgnoreCase("card"))
                {
                    this.ProcessCardCommands(split);
                }
                else if (cmd.equalsIgnoreCase("o")
                        || cmd.equalsIgnoreCase("open")
                        || cmd.equalsIgnoreCase("c")
                        || cmd.equalsIgnoreCase("close")
                        || cmd.equalsIgnoreCase("valve"))
                {
                    this.ProcessValveCommands(split);
                }
                else if (cmd.equalsIgnoreCase("get")
                        || cmd.equalsIgnoreCase("set")
                        || cmd.equalsIgnoreCase("price")
                        || cmd.equalsIgnoreCase("pulses")
                        || cmd.equalsIgnoreCase("config"))
                {
                    this.ProcessConfigurationCommands(split);
                }
                else if (cmd.equalsIgnoreCase("flow")
                        || cmd.equalsIgnoreCase("flow")
                        || cmd.equalsIgnoreCase("flow"))
                {
                    this.ProcessFlowCommands(split);
                }
                else if (cmd.equalsIgnoreCase("calibrate")
                        || cmd.equalsIgnoreCase("cancel")
                        || cmd.equalsIgnoreCase("accept")
                        || cmd.equalsIgnoreCase("ok")
                        || cmd.equalsIgnoreCase("finish"))
                {
                    this.ProcessCalibrationCommands(split);
                }
                else if (cmd.equalsIgnoreCase("reset"))
                {
                    this.Machine.Reset();
                }
                else if (cmd.equalsIgnoreCase("update"))
                {
                    this.Machine.UpdateParameters();
                }
                else if (cmd.equalsIgnoreCase("status"))
                {
                    Object st = this.Machine.GetStatus();
                    System.out.println("Status: " + st);
                }
                else if (cmd.equalsIgnoreCase("loglevel"))
                {
                    System.out.println("Log Level: " + Logger.getGlobal().getLevel());
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

    public void Stop()
    {
        this.Finish = true;
        try
        {
            System.console().reader().close();

        }
        catch (IOException ex)
        {
            Logger.getLogger(ChoperConsole.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void ProcessConfigurationCommands(String[] parameters)
    {
        String cmd = parameters[0];
        String key = null;
        String value = null;

        if (cmd.equalsIgnoreCase("price") || cmd.equalsIgnoreCase("pulses"))
        {
            if (parameters.length > 1)
            {
                cmd = "set";
                value = parameters[1];
            }
            else
            {
                cmd = "get";
            }

            if (cmd.equalsIgnoreCase("price"))
            {
                key = "ChoperMachine.Price";
            }
            else if (cmd.equalsIgnoreCase("pulses"))
            {
                key = "FlowSensor.PulsesPerLiter";
            }
        }
        else if (cmd.equalsIgnoreCase("get"))
        {
            key = parameters[1];
        }
        else if (cmd.equalsIgnoreCase("set"))
        {
            key = parameters[1];
            if (parameters.length <= 2)
            {
                System.out.println("Comando inválido.");
                return;
            }
            value = parameters[2];
        }
        else if (cmd.equalsIgnoreCase("config"))
        {
            this.ShowConfiguration();
            return;
        }

        if (cmd.equalsIgnoreCase("get"))
        {
            Object val = ConfigurationProvider.Instance.Get(key);
            System.out.println(key + " = " + val.toString());
        }
        else if (cmd.equalsIgnoreCase("set"))
        {
            if (ConfigurationProvider.Instance.Exists(key))
            {
                this.Machine.SetParameter(key, value);
                Object val = this.Machine.GetParameter(key);
                System.out.println(key + " = " + val.toString());
            }
            else
            {
                System.out.println("Parámetro de configuración inválido");
            }
        }
    }

    private void ShowConfiguration()
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
        String cmd = parameters[0];
        int nextParam = 1;
        if (cmd.equalsIgnoreCase("card"))
        {
            if (parameters.length == 1)
            {
                System.out.println("Card present?:" + this.Machine.IsSmartCardPresent());
                return;
            }

            cmd = parameters[1];

            nextParam = 2;
        }

        if (cmd.equalsIgnoreCase("bal") || cmd.equalsIgnoreCase("balance"))
        {
            Float value = this.Machine.GetBalance();
            if (value != null)
            {
                System.out.println("Saldo: $" + value.toString());
            }

        }
        else if (cmd.equalsIgnoreCase("cleancard") || cmd.equalsIgnoreCase("clearcard"))
        {
            this.Machine.CleanCardMoney();
            System.out.println("Saldo: $ 0");
        }
        else if (cmd.equalsIgnoreCase("add"))
        {
            float valueAdd = Float.parseFloat(parameters[nextParam]);
            this.Machine.AddMoney(valueAdd);
        }
        else if (cmd.equalsIgnoreCase("sub"))
        {
            float valueSub = Float.parseFloat(parameters[nextParam]);
            this.Machine.SubstractMoney(valueSub);
        }

        System.out.println("Comando inválido");

    }

    private void ProcessCalibrationCommands(String[] parameters)
    {
        String key = parameters[0];

        if (key.equalsIgnoreCase("calibrate"))
        {
            this.Machine.StartCalibration();
            ChoperStatusType st = this.Machine.GetStatus();
            System.out.println("Status: " + st.toString());
        }
        else if (key.equalsIgnoreCase("cancel"))
        {
            this.Machine.CancelCalibration();
            ChoperStatusType st = this.Machine.GetStatus();
            System.out.println("Status: " + st.toString());
        }
        else if (key.equalsIgnoreCase("accept") || key.equalsIgnoreCase("finish") || key.equalsIgnoreCase("ok"))
        {
            this.Machine.FinishCalibration();
            ChoperStatusType st = this.Machine.GetStatus();
            System.out.println("Status: " + st.toString());
        }
    }

    private void ProcessFlowCommands(String[] split)
    {
        if (split.length == 1)
        {
            System.out.println("Comando inválido");
            return;
        }
        String cmd = split[1];
        if (cmd.equalsIgnoreCase("clean") || cmd.equalsIgnoreCase("c") || cmd.equalsIgnoreCase("clear"))
        {
            this.Machine.FlowSensorReset();
        }

    }

    private void ProcessValveCommands(String[] split)
    {
        String cmd = split[0];

        if (cmd.equalsIgnoreCase("o") || cmd.equalsIgnoreCase("open"))
        {
this.Machine.OpenFlowValve();
            System.out.println("Válvula abierta");
        }
        else if (cmd.equalsIgnoreCase("c") || cmd.equalsIgnoreCase("close"))
        {
this.Machine.CloseFlowValve();
            System.out.println("Válvula cerrada");
        }
        else if (cmd.equalsIgnoreCase("valve"))
        {
boolean b = this.Machine.IsValveOpen();
System.out.println("Válvula abierta: " + b);
        }
    }
}
