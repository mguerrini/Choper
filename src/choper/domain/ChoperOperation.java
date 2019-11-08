/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package choper.domain;

import choper.domain.flowSensors.FlowSensorEventArgs;

/**
 *
 * @author mguerrini
 */
public class ChoperOperation
{
    public OperationType Operation;
    
    public float Amount;
    
    public FlowSensorEventArgs FlowSensorData;
    
    public boolean Silent;
}


