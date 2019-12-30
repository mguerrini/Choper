/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package choper.domain.commands;

import choper.platform.ConfigurationProvider;
import java.util.List;
import java.util.Map;

/**
 *
 * @author mguerrini
 */
public class ManualCommand extends CommandBase
{
    public ManualCommand(String commandName)
    {
        super(commandName);
    }
}
