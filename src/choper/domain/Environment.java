/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package choper.domain;

import com.pi4j.platform.Platform;
import com.pi4j.wiringpi.Gpio;

/**
 *
 * @author mguerrini
 */
public class Environment
{

    private static boolean IsWiringPiConfigured = false;
    private static Boolean IsRaspberryPI = null;

    public static void Configure()
    {
        if (IsWiringPiConfigured)
        {
            return;
        }

 
        if (Environment.IsRaspberryPiPlatform())
        {
            int setup = Gpio.wiringPiSetup();
            if (setup < 0)
            {
                System.out.print("No es posible inicializar Wiring Pi");
            }
        }

        IsWiringPiConfigured = true;
    }

    public static boolean IsRaspberryPiPlatform()
    {
        if (IsRaspberryPI == null)
        {
            String os = System.getProperty("java.vm.vendor");
            //System.getProperties().list(System.out);
            IsRaspberryPI = os.equals("Raspbian");
        }

        return IsRaspberryPI;
    }
}
