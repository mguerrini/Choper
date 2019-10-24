/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package choper.domain.SerialComm;

import com.pi4j.io.serial.Serial;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import jssc.SerialPort;

/**
 *
 * @author max22
 */
    public class SerialData
    {
        public SerialPort Channel;

        public boolean IsTimeout;


        public byte[] Data;

        public byte[] Response;

        public int ResponseLength;

        public Exception Error;
        
    }
