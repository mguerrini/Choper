/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package choper.domain.test;

import choper.domain.displays.I2CLCD;
import choper.domain.displays.I2CWiringpiLCD;
import com.pi4j.component.lcd.impl.I2CLcdDisplay;
import com.pi4j.io.i2c.*;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;
import com.pi4j.platform.PlatformAlreadyAssignedException;
import com.pi4j.util.Console;
import com.pi4j.wiringpi.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author mguerrini
 */
public class LcdTest
{
    public void Test()throws InterruptedException, PlatformAlreadyAssignedException, IOException, UnsupportedBusNumberException
    {
     // create Pi4J console wrapper/helper
        // (This is a utility class to abstract some of the boilerplate code)
        final Console console = new Console();

        // print program title/header
        console.title("<-- The Pi4J Project -->", "I2C Example");
        
        // allow for user to exit program using CTRL-C
        console.promptForExit();
/*
        // fetch all available busses
        try {
            int[] ids = I2CFactory.getBusIds();
            console.println("Found follow I2C busses: " + Arrays.toString(ids));
        } catch (IOException exception) {
            console.println("I/O error during fetch of I2C busses occurred");
        }

        // find available busses
      
        for (int number = I2CBus.BUS_0; number <= I2CBus.BUS_17; ++number) {
            try {
                @SuppressWarnings("unused")
		I2CBus bus = I2CFactory.getInstance(number);
                console.println("Supported I2C bus " + number + " found");
            } catch (IOException exception) {
                console.println("I/O error on I2C bus " + number + " occurred");
            } catch (UnsupportedBusNumberException exception) {
                console.println("Unsupported I2C bus " + number + " required");
            }
        }
*/
        I2CDevice _device = null;
        I2CLCD _lcd = null;

        //com.pi4j.wiringpi.I2C.wiringPiI2CSetup(0) .Lcd. n = new Lcd();
        //com.pi4j.wiringpi.GpioInterrupt.GpioUtil.
        try {
            I2CBus bus = I2CFactory.getInstance(I2CBus.BUS_1);
            _device = bus.getDevice(0x3F);
            _lcd = new I2CLCD(_device);
            _lcd.init();
            _lcd.backlight(true);
            _lcd.display_string_pos("Hello, world!", 1, 2);
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
    }
    
    
    public void Test2() throws InterruptedException
    {
       try {
            I2CWiringpiLCD display = new I2CWiringpiLCD();
            
            display.init();
            display.backlight(true);
            display.clear();
            display.display_string_pos("Hola Helena!", 1, 2);
            //display.backlight(false);
        } catch (Exception ex) {
            System.out.println(ex);
        }
       Thread.sleep(30000);
        /*
        int deviceId = com.pi4j.wiringpi.I2C.wiringPiI2CSetup(0x3F);
        /*
        com.pi4j.wiringpi.I2C.wiringPiI2CWrite(deviceId, 0x04);
        com.pi4j.wiringpi.I2C.wiringPiI2CWrite(deviceId, (byte)"H".charAt(0));
        com.pi4j.wiringpi.I2C.wiringPiI2CWrite(deviceId, (byte)"o".charAt(0));
        com.pi4j.wiringpi.I2C.wiringPiI2CWrite(deviceId, (byte)"l".charAt(0));
        com.pi4j.wiringpi.I2C.wiringPiI2CWrite(deviceId, (byte)"a".charAt(0));
        */
    }
    
}
