/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maccontrol;

/**
 *
 * @author Andy
 */
import java.awt.AWTException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.awt.Robot;
import java.awt.MouseInfo;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MacControl {

    public static void main(String[] args) throws AWTException {

//        Robot r = new Robot();
//        r.mouseMove(100, 100);
//        for(int i=0 ;i<100;i++){
//            r.mouseMove(i*10, i*2);
//            r.delay(300);
//        }
//        MouseInfo.getPointerInfo().getLocation();
//        try {
//            InetAddress host = InetAddress.getLocalHost();
//            System.out.println(host.getHostAddress());
//            Socket socket = new Socket(host.getHostAddress(), 17798);
//            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
//            String message = (String) ois.readObject();
//            System.out.println("Message: " + message);
//
//            ois.close();
        //        } catch (UnknownHostException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }
        try {
            ServerSocket serverSocket = new ServerSocket(19877);
            Socket clientSocket = serverSocket.accept();
            BufferedReader in;
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {

                System.out.println(inputLine);
                if (inputLine.equals("Bye.")) {
                    break;
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(MacControl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
