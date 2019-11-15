/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serverudp;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author alessia
 */
public class SERVERUDP {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        // TODO code application logic here
        
        
          int c;
        Thread thread;
        try {
            
            // TODO code application logic here
            UDPEcho echoServer= new UDPEcho(1077);
            thread= new Thread(echoServer);
            //echoServer.start();
            thread.start();
            c=System.in.read();
            //echoServer.interrupt();
            thread.interrupt();
            //echoServer.join();
            thread.join();
            System.out.println("sono il main");
//          for(;;){
//              
//          }
        } catch (SocketException ex) {
            Logger.getLogger(SERVERUDP.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnknownHostException ex) {
            Logger.getLogger(SERVERUDP.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
        
}
