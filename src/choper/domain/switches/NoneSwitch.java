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
public class NoneSwitch implements ISwitch
{
    private boolean _isOpened = true;
    
    @Override
    public void Init()
    {
        _isOpened = true;
    }

    @Override
    public boolean IsClosed()
    {
        return !_isOpened;
    }

    @Override
    public boolean IsOpened()
    {
        return _isOpened;
    }

    @Override
    public void CloseContacts()
    {
        _isOpened = false;
    }

    @Override
    public void OpenContacts()
    {
        _isOpened = true;
    }

    @Override
    public void UpdateParameters()
    {
    }
}
