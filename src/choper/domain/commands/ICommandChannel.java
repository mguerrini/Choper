/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package choper.domain.commands;

/**
 *
 * @author mguerrini
 */
public interface ICommandChannel
{
    public void SendSync(ChoperCommand op);

    public void SendAsync(ChoperCommand op);
}
