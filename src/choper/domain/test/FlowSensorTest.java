/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package choper.domain.test;

import com.pi4j.wiringpi.*;
/**
 *
 * @author mguerrini
 */
public class FlowSensorTest
{
    private int FlowCounter;
    private int Gpio17 = 0;

    private void IncrementFlowCounter(int pin)
    {
        this.FlowCounter++;
    }

    public void Setup()
    {
        int setup = Gpio.wiringPiSetup();
        if (setup < 0)
        {
            System.out.print("No es posible inicializar Wiring Pi");
        }
        
        Gpio.pinMode(Gpio17,Gpio.INPUT);
        Gpio.pullUpDnControl(Gpio17, Gpio.PUD_UP);
    }
    
    public void Start() throws InterruptedException
    {
        
        Gpio.wiringPiISR(Gpio17, Gpio.INT_EDGE_FALLING, this::IncrementFlowCounter);
        this.FlowCounter = 0;
        
        long tStart = Gpio.millis();
        long tStop = Gpio.millis();
        long dif;
        
        for (int i=0; i< 60; i++)
        {
            Thread.sleep(1000l);
            tStop = Gpio.millis();
            dif = tStop - tStart;
            
            System.out.println("Cantidad de pulsos: " + this.FlowCounter + ", Tiempo ms: " + dif);
            tStart = tStop;
        }
        
        this.Stop();
    }
    
    public void Stop()
    {
        Gpio.wiringPiClearISR(Gpio17);
    }
    
    /*
    void loop()
{
   
   if((millis() - oldTime) > 1000)    // Only process counters once per second
  { 
    // Disable the interrupt while calculating flow rate and sending the value to
    // the host
    detachInterrupt(sensorInterrupt);
        
    // Because this loop may not complete in exactly 1 second intervals we calculate
    // the number of milliseconds that have passed since the last execution and use
    // that to scale the output. We also apply the calibrationFactor to scale the output
    // based on the number of pulses per second per units of measure (litres/minute in
    // this case) coming from the sensor.
    flowRate = ((1000.0 / (millis() - oldTime)) * pulseCount) / calibrationFactor;
    
    // Note the time this processing pass was executed. Note that because we've
    // disabled interrupts the millis() function won't actually be incrementing right
    // at this point, but it will still return the value it was set to just before
    // interrupts went away.
    oldTime = millis();
    
    // Divide the flow rate in litres/minute by 60 to determine how many litres have
    // passed through the sensor in this 1 second interval, then multiply by 1000 to
    // convert to millilitres.
    flowMilliLitres = (flowRate / 60) * 1000;
    
    // Add the millilitres passed in this second to the cumulative total
    totalMilliLitres += flowMilliLitres;
      
    unsigned int frac;
    
    // Print the flow rate for this second in litres / minute
    Serial.print("Flow rate: ");
    Serial.print(int(flowRate));  // Print the integer part of the variable
    Serial.print("L/min");
    Serial.print("\t"); 		  // Print tab space

    // Print the cumulative total of litres flowed since starting
    Serial.print("Output Liquid Quantity: ");        
    Serial.print(totalMilliLitres);
    Serial.println("mL"); 
    Serial.print("\t"); 		  // Print tab space
	Serial.print(totalMilliLitres/1000);
	Serial.print("L");
    

    // Reset the pulse counter so we can start incrementing again
    pulseCount = 0;
    
    // Enable the interrupt again now that we've finished sending output
    attachInterrupt(sensorInterrupt, pulseCounter, FALLING);
  }
}
*/
}
