/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package choper.domain.switches;

/**
 *
 * @author mguerrini
 */
public interface ISwitch
{
    void Init();

    boolean IsClosed();

    boolean IsOpened();

    boolean IsLocked();
    
    void CloseContacts();

    void OpenContacts();

    void Lock();
    
    void Unlock();
    
    void UpdateParameters();
}
