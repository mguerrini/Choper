/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package choper.domain.commands;

import choper.domain.ChoperCore;
import choper.domain.Environment;
import choper.platform.ConfigurationProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author mguerrini
 */
public class CommandBase implements ICommand
{
    protected ICommandChannel _channel;
    protected String _name;

    protected List<CommandType> Actions = new ArrayList<>();
    protected Float Parameter;

    public CommandBase(String commandName)
    {
        this._name = commandName;

    }

    public String GetName()
    {
        return _name;
    }

    @Override
    public void Init(ICommandChannel channel)
    {
        this._channel = channel;

        this.Reconfigure();
    }

    @Override
    public void Execute()
    {
        ChoperCommand cmd;
        if (this.Actions.size() == 1)
        {
            cmd = new ChoperCommand();
            cmd.Parameter = this.Parameter;
            cmd.Command = this.Actions.get(0);

            this._channel.SendAsync(cmd);
            return;
        }

        for(CommandType cmdType : this.Actions)
        {
            cmd = new ChoperCommand();
            cmd.Parameter = this.Parameter;
            cmd.Command = cmdType;

            this._channel.SendAsync(cmd);
        }
    }

    @Override
    public void Reconfigure()
    {
        //Command.HalfPint_Actions=BuyPint
        //Command.HalfPint_Parameter=0.5

        String actionsStr = ConfigurationProvider.Instance.GetString("Command", this._name, "Actions");
        String[] actions = actionsStr.split(";");
        for (String act : actions)
        {
            try
            {
                CommandType cmd = CommandType.valueOf(act);
                this.Actions.add(cmd);
            }
            catch (Exception ex)
            {
                System.out.println("Error al intentar crear el comando " + this.GetName());
                System.out.println(ex);
            }
        }

        this.Parameter = ConfigurationProvider.Instance.GetFloat("Command", this._name, "Parameter");
    }
}
