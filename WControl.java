package wcontrol;

import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Robot;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Andy
 */
public class WControl extends Thread {

    Robot bot;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        WControl wc = new WControl();
        wc.run();
    }

    public WControl() {
        try {
            this.bot = new Robot();
        } catch (AWTException ex) {
            Logger.getLogger(WControl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void run() {
        boolean stopped = false;
        try {
            ServerSocket serverSocket = new ServerSocket(8090);
            while (!stopped) {
                Socket clientSocket = serverSocket.accept();
                InetAddress client = clientSocket.getInetAddress();
                BufferedReader input
                        = new BufferedReader(new InputStreamReader(clientSocket.
                                        getInputStream()));
                DataOutputStream output
                        = new DataOutputStream(clientSocket.getOutputStream());
                stopped = http_handler(input, output);
            }
        } catch (IOException ex) {
        }
    }

    private boolean http_handler(BufferedReader input, DataOutputStream output) {
        int method = 0; //1 get, 2 head, 0 not supported
        String http = new String(); //a bunch of strings to hold
        String path = new String(); //the various things, what http v, what path,
        String file = new String(); //what file
        String user_agent = new String(); //what user_agent
        try {
            //This is the two types of request we can handle
            //GET /index.html HTTP/1.0
            //HEAD /index.html HTTP/1.0
            String tmp = input.readLine(); //read from the stream
            String tmp2 = new String(tmp);
            tmp.toUpperCase(); //convert it to uppercase
            if (tmp.startsWith("GET")) { //compare it is it GET
                method = 1;
            } //if we set it to method 1
            if (tmp.startsWith("HEAD")) { //same here is it HEAD
                method = 2;
            } //set method to 2

            if (method == 0) { // not supported
                try {
                    output.writeBytes(construct_http_header(501, 0));
                    output.close();
                    return false;
                } catch (Exception e3) { //if some error happened catch it
                    s("error:" + e3.getMessage());
                } //and display error
            }

            int start = 0;
            int end = 0;
            for (int a = 0; a < tmp2.length(); a++) {
                if (tmp2.charAt(a) == ' ' && start != 0) {
                    end = a;
                    break;
                }
                if (tmp2.charAt(a) == ' ' && start == 0) {
                    start = a;
                }
            }
            path = tmp2.substring(start + 2, end); //fill in the path
            end = path.indexOf("?");

            String paras = "";
            if (end != -1) {
                paras = path.substring(end + 1);
                path = path.substring(0, end);
            }

            if (path.equals("Close")) {
                return true;
            } else if (path.equals("index.html") && !paras.equals("")) {
                action(paras);
                return false;
            }
        } catch (Exception e) {
            return false;
  //          s("errorr" + e.getMessage());
        } //catch any exception

        //path do now have the filename to what to the file it wants to open
        s("\nClient requested:" + new File(path).getAbsolutePath() + "\n");
        FileInputStream requestedfile = null;

        try {
            requestedfile = new FileInputStream(path);
        } catch (Exception e) {
            try {
                //if you could not open the file send a 404
                output.writeBytes(construct_http_header(404, 0));
                //close the stream
                output.close();
            } catch (Exception e2) {
            }
            s("error" + e.getMessage());
        } //print error to gui

        //happy day scenario
        try {
            int type_is = 0;
            //find out what the filename ends with,
            //so you can construct a the right content type
            if (path.endsWith(".zip") || path.endsWith(".exe")
                    || path.endsWith(".tar")) {
                type_is = 3;
            }
            if (path.endsWith(".jpg") || path.endsWith(".jpeg")) {
                type_is = 1;
            }
            if (path.endsWith(".gif")) {
                type_is = 2;
                //write out the header, 200 ->everything is ok we are all happy.
            }
            output.writeBytes(construct_http_header(200, 5));

            //if it was a HEAD request, we don't print any BODY
            if (method == 1) { //1 is GET 2 is head and skips the body
                while (true) {
                    //read the file from filestream, and print out through the
                    //client-outputstream on a byte per byte base.
                    int b = requestedfile.read();
                    if (b == -1) {
                        break; //end of file
                    }
                    output.write(b);
                }

            }
//clean up the files, close open handles
            output.close();
            requestedfile.close();
        } catch (Exception e) {
        }
        return false;
    }

    private String construct_http_header(int return_code, int file_type) {
        String s = "HTTP/1.0 ";
        //you probably have seen these if you have been surfing the web a while
        switch (return_code) {
            case 200:
                s = s + "200 OK";
                break;
            case 400:
                s = s + "400 Bad Request";
                break;
            case 403:
                s = s + "403 Forbidden";
                break;
            case 404:
                s = s + "404 Not Found";
                break;
            case 500:
                s = s + "500 Internal Server Error";
                break;
            case 501:
                s = s + "501 Not Implemented";
                break;
        }

        s = s + "\r\n"; //other header fields,
        s = s + "Connection: close\r\n"; //we can't handle persistent connections
        s = s + "Server: Mac Control Test Server v0\r\n"; //server name

        switch (file_type) {
            //plenty of types for you to fill in
            case 0:
                break;
            case 1:
                s = s + "Content-Type: image/jpeg\r\n";
                break;
            case 2:
                s = s + "Content-Type: image/gif\r\n";
            case 3:
                s = s + "Content-Type: application/x-zip-compressed\r\n";
            default:
                s = s + "Content-Type: text/html\r\n";
                break;
        }

        ////so on and so on......
        s = s + "\r\n"; //this marks the end of the httpheader
        //and the start of the body
        //ok return our newly created header!
        return s;
    }

    private void action(String paras) {
        int x = 0, y = 0;
        if (paras.startsWith("x=")) {
            String[] para = paras.split("&");
            if (para.length == 2) {
                PointerInfo PointerInfo = MouseInfo.getPointerInfo();
                Point p = PointerInfo.getLocation();
                x = p.x;
                x += Integer.valueOf(para[0].substring(2));
                y = Integer.valueOf(para[1].substring(2));
                y += p.y;
                bot.mouseMove(x, y);
            }
        }
    }

    void s(String str) {
        System.out.println(str);

    }
}
