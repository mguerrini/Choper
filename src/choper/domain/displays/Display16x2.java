/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package choper.domain.displays;

import choper.domain.DisplayTextAlign;
import choper.domain.Environment;

/**
 *
 * @author mguerrini
 */
public class Display16x2 implements IDisplay16x2
{
    public Display16x2()
    {
        if (Environment.IsRaspberryPiPlatform())
        {
            this.RealDisplay = new I2CWiringpiLCD();
            this.RealDisplay.init();
        }
    }

    private I2CWiringpiLCD RealDisplay;

    @Override
    public void Init()
    {
    }

    @Override
    public void UpdateParameters()
    {

    }

    @Override
    public void ShowLine1(String txt)
    {
        this.DoShowLine(1, txt);
    }

    @Override
    public void ShowLine1(int position, String txt)
    {
        this.DoShowLine(1, position, txt);
    }

    @Override
    public void ClearLine1()
    {
        this.DoClearLine(1);

    }

    @Override
    public void ClearLine1(int fromPos)
    {
        this.DoClearLine(1, fromPos);
    }

    @Override
    public void ShowLine2(String txt)
    {
        this.DoShowLine(2, txt);
    }

    @Override
    public void ShowLine2(int position, String txt)
    {
        this.DoShowLine(2, position, txt);
    }

    @Override
    public void ClearLine2()
    {
        this.DoClearLine(2);
    }

    @Override
    public void ClearLine2(int fromPos)
    {
        this.DoClearLine(2, fromPos);
    }

    @Override
    public void Clear()
    {
        if (this.RealDisplay != null)
        {
            this.RealDisplay.clear();
        }
    }

    private void DoShowLine(int line, String txt)
    {
        if (txt.length() < 16)
        {
            for (int i = txt.length(); i < 16; i++)
            {
                txt += " ";
            }
        }

        if (this.RealDisplay != null)
        {
            this.RealDisplay.display_string(txt, line);
        }
        else
        {
            System.out.println(txt);
        }
    }

    private void DoShowLine(int line, int position, String txt)
    {
        if ((txt.length() + position) < 16)
        {
            for (int i = (txt.length() + position); i < 16; i++)
            {
                txt += " ";
            }
        }

        if (this.RealDisplay != null)
        {
            this.RealDisplay.display_string_pos(txt, line, position);
        }
        else
        {
            System.out.println(txt);
        }
    }

    private void DoClearLine(int line)
    {
        String txt = "                ";
        if (this.RealDisplay != null)
        {
            this.RealDisplay.display_string(txt, line);
        }
    }

    private void DoClearLine(int line, int position)
    {
        String txt = "";

        for (int i = position; i < 16; i++)
        {
            txt += " ";
        }

        if (this.RealDisplay != null)
        {
            this.RealDisplay.display_string_pos(txt, line, position);
        }
    }

    @Override
    public void ShowLine1(String txt, DisplayTextAlign align)
    {
        this.DoShowLine(1, txt, align);
    }

    @Override
    public void ShowLine2(String txt, DisplayTextAlign align)
    {
        this.DoShowLine(2, txt, align);
    }

    private void DoShowLine(int line, String txt, DisplayTextAlign align)
    {
        if (align == DisplayTextAlign.Left || txt.length() == 16)
        {
            this.DoShowLine(line, txt);
        }
        else if (align == DisplayTextAlign.Center)
        {
            if (txt.length() > 16)
            {
                this.DoShowLine(line, txt);
            }
            else
            {
                int margin = 16 - txt.length();
                margin = margin / 2;
                this.DoShowLine(line, margin, txt);
            }
        }
        else
        {
            if (txt.length() > 16)
            {
                txt = txt.substring(txt.length() - 16);
                this.DoShowLine(line, txt);
            }
            else
            {
                int margin = 16 - txt.length();
                this.DoShowLine(line, margin, txt);
            }
        }
    }

}
