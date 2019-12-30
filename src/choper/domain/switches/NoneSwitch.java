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
    private boolean _isLocked = false;

    @Override
    public void Init()
    {
        this.OpenContacts();
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
        if (this.IsLocked())
        {
            System.out.println("Switch - Locked");
            if (this.IsOpened())
            {
                System.out.println("Switch - Opened");
            }
            else
            {
                System.out.println("Switch - Closed");
            }

            return;
        }

        _isOpened = false;
        System.out.println("Switch - Closed");

    }

    @Override
    public void OpenContacts()
    {
        if (this.IsLocked())
        {
            System.out.println("Switch - Locked");
            if (this.IsOpened())
            {
                System.out.println("Switch - Opened");
            }
            else
            {
                System.out.println("Switch - Closed");
            }

            return;
        }
        _isOpened = true;
        System.out.println("Switch - Opened");
    }

    @Override
    public void UpdateParameters()
    {
    }

    @Override
    public boolean IsLocked()
    {
        return _isLocked;
    }

    @Override
    public void Lock()
    {
        _isLocked = true;
        System.out.println("Switch - Locked");
    }

    @Override
    public void Unlock()
    {
        _isLocked = false;
        System.out.println("Switch - Unlocked");
    }
}
