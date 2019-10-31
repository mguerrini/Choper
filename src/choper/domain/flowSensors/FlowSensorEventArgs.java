/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package choper.domain.flowSensors;

/**
 *
 * @author mguerrini
 */
public class FlowSensorEventArgs
{
    /**
     * Expresa la cantidad de eventos desde que se conecto el sensor
     */
    public int EventNumber;

    /**
     * Cantidad de milisegundos al momento de conectarse el sensor
     */
    public long StartTimeMillis;
    
    /**
     * Cantidad de milisegundos al momento de dispararse el evento
     */
    public long CurrentTimeMillis;
    
    /**
     * Expresa la cantidad de cm cúbicos de líquido desde el último evento
     */
    public float DeltaVolume;

    /**
     * Expresa la cantidad de cm cúbicos de líquido desde que se conecto el sensor
     */
    public float TotalVolume;
}
