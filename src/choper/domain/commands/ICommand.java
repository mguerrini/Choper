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
public interface ICommand
{
    String GetName();
    
    void Init(ICommandChannel channel);
    
    void Execute();
    
    void Reconfigure();
}
