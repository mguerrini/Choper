/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package choper.domain;

import choper.domain.displays.IDisplay16x2;

/**
 *
 * @author mguerrini
 */
public class Display
{
    public Display(IDisplay16x2 innerDisplay)
    {
        this.RealDisplay = innerDisplay;
    }

    private IDisplay16x2 RealDisplay;

    private int CurrentLenLine1 = 0;
    private int CurrentLenLine2 = 0;

    
    public void UpdateParameters()
    {
        this.RealDisplay.UpdateParameters();
    }
    
        public void ShowPulses(int pulses)
    {
        this.RealDisplay.ClearLine1();

        String newLine = "Pulses: " + pulses;

        this.CurrentLenLine1 = newLine.length();
        this.RealDisplay.ShowLine1(newLine);
    }
    
    public void UpdatePulses(int pulses)
    {
        int curLen = this.CurrentLenLine1;
        String num = String.valueOf(pulses);
        this.CurrentLenLine1 = ("Pulses: " + pulses).length();

        while ((num.length() + 8) < curLen)
        {
            num += " ";
        }

        this.RealDisplay.ShowLine1(8, num);
    }
    
        
    public void ShowBalance(float value)
    {
        this.RealDisplay.ClearLine1();

        String num = String.format("%.02f", value);
        String newLine = "Saldo: $ " + num;

        this.CurrentLenLine1 = newLine.length();
        this.RealDisplay.ShowLine1(newLine);
    }

    public void UpdateBalance(float value)
    {
        String num = String.format("%.02f", value);
        int curLen = this.CurrentLenLine1;
        this.CurrentLenLine1 = ("Saldo: $ " + num).length();

        while ((num.length() + 10) < curLen)
        {
            num += " ";
        }

        this.RealDisplay.ShowLine1(9, num);
    }

    public void ShowVolume(float value)
    {
        this.RealDisplay.ClearLine2();

        int vol = Math.round(value);
        String newLine = "Vol. cm3: " + vol;

        this.CurrentLenLine2 = newLine.length();
        this.RealDisplay.ShowLine2(newLine);
    }

    public void UpdateVolume(float value)
    {
        String num = String.format("%.02f", value);
        int curLen = this.CurrentLenLine2;
        this.CurrentLenLine2 = ("Vol. cm3: " + num).length();

        while ((num.length() + 10) < curLen)
        {
            num += " ";
        }

        this.RealDisplay.ShowLine2(10, num);
    }

    public void ShowTitle(String title)
    {
        //linea completa
        if (title.length() < this.CurrentLenLine1)
        {
            this.RealDisplay.ClearLine1();
        }

        this.RealDisplay.ShowLine1(title);
        this.CurrentLenLine1 = title.length();
    }

    public void ShowMessage(String msg)
    {
//linea completa
        if (msg.length() < this.CurrentLenLine2)
        {
            this.RealDisplay.ClearLine2();
        }

        this.RealDisplay.ShowLine2(msg);
        this.CurrentLenLine2 = msg.length();
    }
}
