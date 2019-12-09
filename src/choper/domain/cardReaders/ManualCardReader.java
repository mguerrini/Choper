/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package choper.domain.cardReaders;

import choper.platform.events.EventArgs;
import choper.platform.events.IEvent;

/**
 *
 * @author mguerrini
 */
public class ManualCardReader extends CardReaderBase implements IVirtualCardReader
{
    private float _currentBalance;
    private boolean _isCardPresent = false;

    @Override
    public void Init()
    {
        this._currentBalance = 0f;
    }

    @Override
    public void Connect()
    {
        this._currentBalance = 0f;
    }

    @Override
    public void Disconnect()
    {
    }

    @Override
    public void UpdateParameters()
    {
    }

    @Override
    public boolean IsCardPresent()
    {
        return this._isCardPresent;
    }

    @Override
    public float GetBalance()
    {
        return this._currentBalance;
    }

    @Override
    public boolean SetBalance(float amount)
    {
        if (!this.IsCardPresent())
        {
            return false;
        }

        _currentBalance = amount;
        if (amount <= 0)
        {
            System.out.println("CardReader - SetBalance: $ 0 - Removing Card");
            this.RemoveCard();
        }
        else
        {
            System.out.println("CardReader - SetBalance: $ " + amount);
            this.RaiseBalanceChanged();
        }

        return true;
    }

    public void InsertCard(float valueAdd)
    {
        if (this.IsCardPresent())
        {
            float bal = this.GetBalance() + valueAdd;
            this.SetBalance(bal);
            return;
        }

        if (valueAdd <= 0)
        {
            return;
        }

        _currentBalance = valueAdd;
        _isCardPresent = true;
        System.out.println("Card Inserted: $ " + valueAdd);
        this.RaiseCardInserted();
    }

    public void RemoveCard()
    {
        if (!this.IsCardPresent())
        {
            return;
        }

        _currentBalance = 0;
        _isCardPresent = false;
        this.RaiseCardRemoved();
    }
}
