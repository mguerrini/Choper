/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package choper;

import choper.domain.*;
import choper.domain.cardReaders.*;
import choper.domain.cores.SmartCardChoperCore;
import choper.domain.flowSensors.*;
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
    private ChoperController Controller;
    private ChoperServer Server;

    public void Start()
    {
        Environment.Configure();

        this.Machine = new ChoperMachine();
        this.Controller = new ChoperController(this.Machine);

        this.Machine.Init();
        this.Machine.Connect();

        //inicio el servidor
        this.Server = new ChoperServer(this.Controller);
        this.Server.Start();

        String cmdline = "";
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        while (!this.Finish)
        {
            try
            {
                //this.Machine.StartCalibration();
                //ChoperStatusType st = this.Machine.GetStatus();
                //System.out.println("Status: " + st.toString());

                if (cmdline == "")
                {
                    cmdline = reader.readLine();
                }

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
                        || cmd.equalsIgnoreCase("balance"))
                {
                    this.ProcessMoneyCommands(split);
                }
                else if (cmd.equalsIgnoreCase("o")
                        || cmd.equalsIgnoreCase("open")
                        || cmd.equalsIgnoreCase("c")
                        || cmd.equalsIgnoreCase("close")
                        || cmd.equalsIgnoreCase("lock")
                        || cmd.equalsIgnoreCase("unlock")
                        || cmd.equalsIgnoreCase("valve"))
                {
                    this.ProcessValveCommands(split);
                }
                else if (cmd.equalsIgnoreCase("get")
                        || cmd.equalsIgnoreCase("set")
                        || cmd.equalsIgnoreCase("literprice")
                        || cmd.equalsIgnoreCase("pintprice")
                        || cmd.equalsIgnoreCase("pintsize")
                        || cmd.equalsIgnoreCase("pulsesPerLiter")
                        || cmd.equalsIgnoreCase("config"))
                {
                    this.ProcessConfigurationCommands(split);
                }
                else if (cmd.equalsIgnoreCase("flow"))
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
                    this.ShowStatus();
                }
                else if (cmd.equalsIgnoreCase("cmd"))
                {
                    if (split.length > 1 && split[1] != "")
                        this.Machine.ExecuteCommand(split[1]);
                }
                else if (cmd.equalsIgnoreCase("loglevel"))
                {
                    System.out.println("Log Level: " + Logger.getGlobal().getLevel());
                }
                else if (cmd.equalsIgnoreCase("test"))
                {
                    this.ProcessTestCommands(split);
                }
                /*
                else if (cmd.equalsIgnoreCase("insert") || cmd.equalsIgnoreCase("remove"))
                {
                    this.ProcessVirtualCardCommands(split);
                }
                 */
                else
                {
                    System.out.println("Comando inválido!");
                }

                cmdline = "";
            }
            catch (Exception ex)
            {
                System.out.println(ex);
            }
        }

        this.Server.Stop();

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
            Logger.getLogger(ChoperConsole.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void ProcessConfigurationCommands(String[] parameters)
    {
        String cmd = parameters[0];

        if (cmd.equalsIgnoreCase("literprice") || cmd.equalsIgnoreCase("pintprice"))
        {
            float fvalue = Integer.parseInt(parameters[1]);
            if (cmd.equalsIgnoreCase("pintprice"))
            {
                ConfigurationProvider.Instance.Save("ChoperMachine", "Pint", "Price", fvalue);
                Object val = ConfigurationProvider.Instance.Get("ChoperMachine", "Pint", "Price");
                System.out.println("ChoperMachine.Pint_Price" + " = " + val.toString());
            }
            else
            {
                ConfigurationProvider.Instance.Save("ChoperMachine", "Liter", "Price", fvalue);
                Object val = ConfigurationProvider.Instance.Get("ChoperMachine", "Liter", "Price");
                System.out.println("ChoperMachine.Liter_Price" + " = " + val.toString());
            }
        }
        if (cmd.equalsIgnoreCase("pintsize") || cmd.equalsIgnoreCase("pulsesPerLiter"))
        {
            int ivalue = Integer.parseInt(parameters[1]);
            if (cmd.equalsIgnoreCase("pintsize"))
            {
                ConfigurationProvider.Instance.Save("ChoperMachine", "Pint", "Size", ivalue);
                Object val = ConfigurationProvider.Instance.Get("ChoperMachine", "Pint", "Size");
                System.out.println("ChoperMachine.Pint_Size" + " = " + val.toString());
            }
            else
            {
                ConfigurationProvider.Instance.Save("FlowSensor", "PulsesPerLiter", ivalue);
                Object val = ConfigurationProvider.Instance.Get("FlowSensor", "PulsesPerLiter");
                System.out.println("FlowSensor.PulsesPerLiter" + " = " + val.toString());
            }
        }
        if (cmd.equalsIgnoreCase("get"))
        {
            Object val = ConfigurationProvider.Instance.Get(parameters[1]);
            System.out.println(parameters[1] + " = " + val.toString());
        }
        else if (cmd.equalsIgnoreCase("set"))
        {
            if (ConfigurationProvider.Instance.Exists(parameters[1]))
            {
                Object currVal = ConfigurationProvider.Instance.Exists(parameters[1]);
                if (currVal != null)
                {
                    Object newVal = this.toObject(currVal.getClass(), parameters[1]);
                    this.Machine.SetParameter(parameters[1], newVal);
                }
                else
                {
                    this.Machine.SetParameter(parameters[1], parameters[2]);
                }
                
                Object val = this.Machine.GetParameter(parameters[1]);
                System.out.println(parameters[1] + " = " + val.toString());
            }
            else
            {
                System.out.println("Parámetro de configuración inválido");
            }
        }
        else if (cmd.equalsIgnoreCase("config"))
        {
            this.ShowConfiguration();
            return;
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
        if (cmd.equalsIgnoreCase("reset") || cmd.equalsIgnoreCase("c") || cmd.equalsIgnoreCase("clear"))
        {
            this.Machine.FlowSensorReset();
        }
        else if (cmd.equalsIgnoreCase("pulses"))
        {
            int value = Integer.parseInt(split[2]);
            IFlowSensor sensor = this.Machine.GetFlowSensor();

            if (!ManualFlowSensor.class.isAssignableFrom(sensor.getClass()))
            {
                return;
            }

            ManualFlowSensor mSensor = (ManualFlowSensor) sensor;
            if (split[2].startsWith("+"))
            {
                mSensor.IncrementPulses(value);
            }
            else
            {
                mSensor.UpdatePulses(value);
            }
        }

    }

    private void ProcessValveCommands(String[] split)
    {
        String cmd = split[0];

        if (cmd.equalsIgnoreCase("o") || cmd.equalsIgnoreCase("open"))
        {
            this.Machine.OpenFlowValve();
        }
        else if (cmd.equalsIgnoreCase("c") || cmd.equalsIgnoreCase("close"))
        {
            this.Machine.CloseFlowValve();
        }
        else if (cmd.equalsIgnoreCase("lock"))
        {
            this.Machine.LockUnlockFlowValve(true);
        }
        else if (cmd.equalsIgnoreCase("unlock"))
        {
            this.Machine.LockUnlockFlowValve(false);
        }
        else if (cmd.equalsIgnoreCase("valve"))
        {
        }

        boolean l = this.Machine.IsValveLocked();
        boolean o = this.Machine.IsValveOpen();

        System.out.println("-- Válvula --");
        if (l)
        {
            System.out.println("- Bloqueada -");
        }
        else
        {
            System.out.println("- Desbloqueada -");
        }
        if (o)
        {
            System.out.println("- Abierta -");
        }
        else
        {
            System.out.println("- Cerrada -");
        }
    }

    
    private void ShowStatus()
    {
        List<Entry<String, Object>> values = ConfigurationProvider.Instance.GetAll();
        System.out.println("======== Status ========");
        System.out.println("Status: " + this.Machine.GetStatus());
        
        if (this.Machine.IsValveOpen())
        {
            System.out.println("Válvula abierta");
        }
        else
        {
            System.out.println("Válvula cerrada");
        }
        
        if (this.Machine.IsValveLocked())
        {
            System.out.println("Válvula bloqueada");
        }
        else
        {
            System.out.println("Válvula desbloqueada");
        }
        System.out.println("======== Fin Status ========");
    }

    
    private Object toObject(Class clazz, String value)
    {
        if (Boolean.class.isAssignableFrom(clazz))
        {
            return Boolean.parseBoolean(value);
        }
        if (Byte.class.isAssignableFrom(clazz))
        {
            return Byte.parseByte(value);
        }
        if (Short.class.isAssignableFrom(clazz))
        {
            return Short.parseShort(value);
        }
        if (Integer.class.isAssignableFrom(clazz))
        {
            return Integer.parseInt(value);
        }
        if (Long.class.isAssignableFrom(clazz))
        {
            return Long.parseLong(value);
        }
        if (Float.class.isAssignableFrom(clazz))
        {
            return Float.parseFloat(value);
        }
        if (Double.class.isAssignableFrom(clazz))
        {
            return Double.parseDouble(value);
        }
        if (String.class.isAssignableFrom(clazz))
        {
            return value;
        }
        return value;
    }
    
    private void ProcessTestCommands(String[] parameters)
    {
        String cmd = parameters[0];
        if (parameters.length < 3)
        {
            return;
        }
        cmd = parameters[1];
        String val;

        if (cmd.equalsIgnoreCase("pulses"))
        {
            int value = Integer.parseInt(parameters[2]);
            IFlowSensor sensor = this.Machine.GetFlowSensor();

            if (!ManualFlowSensor.class.isAssignableFrom(sensor.getClass()))
            {
                return;
            }

            ManualFlowSensor mSensor = (ManualFlowSensor) sensor;
            if (parameters[2].startsWith("+"))
            {
                mSensor.IncrementPulses(value);
            }
            else
            {
                mSensor.UpdatePulses(value);
            }
        }
        else if (cmd.equalsIgnoreCase("insert"))
        {
            float valueAdd = Float.parseFloat(parameters[2]);
            ICardReader reader = this.Machine.GetCardReader();

            if (!ManualCardReader.class.isAssignableFrom(reader.getClass()))
            {
                return;
            }

            ManualCardReader mReader = (ManualCardReader) reader;
            mReader.InsertCard(valueAdd);
        }
        else
        {
            System.out.println("Comando inválido");
        }

    }

    /*

    private void ProcessVirtualCardCommands(String[] parameters)
    {
        ICardReader reader = this.Machine.GetCardReader();

        if (!IVirtualCardReader.class.isAssignableFrom(reader.getClass()))
        {
            return;
        }

        IVirtualCardReader virtualReader = (IVirtualCardReader) reader;
        String cmd = parameters[0];

        if (cmd.equalsIgnoreCase("insert"))
        {
            float valueAdd = Float.parseFloat(parameters[1]);

            virtualReader.InsertCard(valueAdd);
        }
        else if (cmd.equalsIgnoreCase("remove"))
        {
            virtualReader.RemoveCard();
        }
        else
        {
            System.out.println("Comando inválido");
        }
    }
    
    
    private void ProcessCardCommands(String[] parameters)
    {
        String cmd = parameters[0];
        int nextParam = 1;
        SmartCardChoperCore reader = (SmartCardChoperCore) this.Machine.GetCore();
        if(reader == null)
            return;
        
        if (cmd.equalsIgnoreCase("card"))
        {
            if (parameters.length == 1)
            {
                System.out.println("Card present?:" + reader.IsSmartCardPresent());
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
            reader.CleanCardMoney();
            System.out.println("Saldo: $ 0");
        }
        else if (cmd.equalsIgnoreCase("add"))
        {
            float valueAdd = Float.parseFloat(parameters[nextParam]);
            reader.AddMoney(valueAdd);
        }
        else if (cmd.equalsIgnoreCase("sub"))
        {
            float valueSub = Float.parseFloat(parameters[nextParam]);
            reader.SubstractMoney(valueSub);
        }

        System.out.println("Comando inválido");

    }
     */
    private void ProcessMoneyCommands(String[] split)
    {
        String cmd = split[0];
        if (cmd.equalsIgnoreCase("bal") || cmd.equalsIgnoreCase("balance"))
        {
            Float value = this.Machine.GetBalance();
            if (value != null)
            {
                System.out.println("Saldo: $" + value.toString());
            }
            return;
        }
        else if (cmd.equalsIgnoreCase("add"))
        {
            float valueAdd = Float.parseFloat(split[1]);
            this.Machine.AddMoney(valueAdd);
            Float value = this.Machine.GetBalance();
            if (value != null)
            {
                System.out.println("Saldo: $" + value.toString());
            }
            return;
        }
        else if (cmd.equalsIgnoreCase("sub"))
        {
            float valueSub = Float.parseFloat(split[1]);
            this.Machine.SubstractMoney(valueSub);
            Float value = this.Machine.GetBalance();
            if (value != null)
            {
                System.out.println("Saldo: $" + value.toString());
            }

            return;
        }

        System.out.println("Comando inválido");
    }
}
