package bgu.spl.net.impl.BGRSServer.Tester;

import java.io.*;

public class StreamReader implements Runnable {

    private final   InputStream is;
    private         String  lastOutput = "Starting connect to 127.0.0.1:7777";
    private         String  allOutPut  = "";

    private boolean continueRead = true;
    private boolean readyToWrite = false;

    public StreamReader(InputStream is) {
        this.is = is;
    }
    public void    kill()            {  continueRead = false; }

    public String  getLastResponse() {  return lastOutput;    }
    public void    clearLastResponse(){
        lastOutput = "";
    }
    public void    clearAllResponse(){
        allOutPut = "";
    }
    public String  getAllResponse(){return allOutPut;}

    public boolean isReadyToWrite(){
        return readyToWrite;
    }
    @Override
    public void run() {
        try {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br     = new BufferedReader(isr);
            String currentLine    = "";
            while ((currentLine = br.readLine()) != null) {
                if(currentLine.equals("Starting connect to 127.0.0.1:7777")) {
                    readyToWrite = true;
                }
                currentLine = currentLine.replaceAll("\0",""); //sanity
                lastOutput = currentLine;
                allOutPut += currentLine + "\n";
                if (!continueRead) {
                    break;
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}