/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package choper.domain.displays;

import choper.domain.DisplayTextAlign;

/**
 *
 * @author mguerrini
 */
public interface IDisplay16x2
{
    void Init();

    public void ShowLine1(String txt);

    public void ShowLine1(String txt, DisplayTextAlign align);

    public void ShowLine1(int position, String txt);

    public void ClearLine1();

    public void ClearLine1(int fromPos);

    
    public void ShowLine2(String txt);

    public void ShowLine2(String txt, DisplayTextAlign align);

    public void ShowLine2(int position, String txt);

    public void ClearLine2();

    public void ClearLine2(int fromPos);

    public void Clear();
}
