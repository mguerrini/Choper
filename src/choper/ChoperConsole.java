/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package choper;

import choper.domain.ChoperMachine;
import choper.domain.Environment;
import java.io.IOException;
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
        
        try
        {
            while(!this.Finish)
            {
                cmd = System.console().readLine();
                this.Finish = "quit".equalsIgnoreCase(cmd) || "exit".equalsIgnoreCase(cmd);
                
                if (this.Finish)
                    continue;
                
                String[] split = cmd.split(" ");
            }
        }
        catch (Exception ex)
        {
            
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
            Logger.getLogger(ChoperConsole.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
