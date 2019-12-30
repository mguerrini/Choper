/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package choper.domain.commands;

import choper.platform.events.Event;
import choper.platform.events.IEvent;

/**
 *
 * @author mguerrini
 */
public class ChoperCommandBroker implements ICommandChannel
{
    private IEvent<ChoperCommand> CommandReady = new Event<ChoperCommand>("ChoperCommandBroker->ChoperCommand");

    public IEvent<ChoperCommand> GetCommandReadyEvent()
    {
        return this.CommandReady;
    }

    private void RaiseCommandSync(ChoperCommand args)
    {
        ((Event<ChoperCommand>) this.CommandReady).Invoke(this, args);
    }

    private void RaiseCommandAsync(ChoperCommand args)
    {
        ((Event<ChoperCommand>) this.CommandReady).InvokeAsync(this, args);
    }
    
    public void SendSync(ChoperCommand op)
    {
        this.RaiseCommandSync(op);
    }

    public void SendAsync(ChoperCommand op)
    {
        this.RaiseCommandAsync(op);
    }
}
