/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package choper.domain;

/**
 *
 * @author max22
 */
public class Display16x2Provider
{
    public  static Display16x2Provider Instance = new   Display16x2Provider();
    
    private Display16x2 SingleInstance;
    
    public Display16x2 Get()
    {
        if (this.SingleInstance == null)
            this.SingleInstance = new Display16x2();
        
        return this.SingleInstance;
    }
}

