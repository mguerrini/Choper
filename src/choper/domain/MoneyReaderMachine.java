/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package choper.domain;

import choper.domain.SerialComm.SerialChannel;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import jssc.SerialPortList;

/**
 *
 * @author max22
 */
public class MoneyReaderMachine
{
    /*
    public String FindPort()
    {
          String[] portNames = SerialPortList.getPortNames();
    for(int i = 0; i < portNames.length; i++){
        System.out.println(portNames[i]);
    }
    }
    */
    
    public String FindPort()
    {
        List<String> ports = this.GetPortNames();

        for (String p : ports)
        {
            SerialChannel ch = null;
            try
            {
                ch = new SerialChannel(p); //, 1000);
                boolean b = ch.Open();
                
                if (!b)
                    continue;
                
                byte[] res = ch.SendDataSync(5000, new byte[] { (byte)182 }, 4);

                if (res != null && res.length == 4 && (res[3] == 0 || res[3] == 1))
                {
                    return p;
                }
            }
            catch (Exception ex)
            {
                System.out.println(ex);
            }
            finally
            {
                if (ch != null)
                {
                    ch.Close();
                }
            }
        }

        return "";
    }

    public List<String> GetPortNames()
    {
        /*
        String[] list = SerialPortList.getPortNames();
        ArrayList<String> output = new ArrayList<String>();
        
        for(String s : list)
        {
            output.add(s);
        }
        
        return output;
        */
        
        //File folder = new File("/dev/serial");
        //try (Stream<Path> walk = Files.walk(Paths.get("/dev")))
        try (Stream<Path> walk = Files.walk(Paths.get("/dev/serial/by-id")))
        {
            List<String> result = walk.map(x -> x.toString()).collect(Collectors.toList());
            result.forEach(System.out::println);
            return result;

        }
        catch (IOException e)
        {
            System.out.println(e.getMessage());
            return new ArrayList<String>();
        }
    }
}
