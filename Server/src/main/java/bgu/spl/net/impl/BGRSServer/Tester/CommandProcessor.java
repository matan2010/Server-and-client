package bgu.spl.net.impl.BGRSServer.Tester;

import java.io.PrintWriter;

public class CommandProcessor {

    private PrintWriter  consoleWriter;
    private bgu.spl.net.impl.BGRSServer.Tester.StreamReader consoleReader;
    private Process      consoleProcess;

    private boolean processIsReadyForCommands = false;

    public void kill() {
        consoleProcess.destroy();
        consoleReader.kill();
    }

    public void initialize() {
        try {
            String binName = "./BGRSclient 127.0.0.1 7777";
            consoleProcess = Runtime.getRuntime().exec(binName);
            consoleReader = new StreamReader(
                    consoleProcess.getInputStream());
            new Thread(consoleReader).start();
            consoleWriter = new PrintWriter(consoleProcess.getOutputStream());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String sendCommand(String command) {
        String response = "";
        try {
            do{
                Thread.sleep(100);
            }while(!consoleReader.isReadyToWrite());

            consoleWriter.write(command);
            consoleWriter.write("\n");
            consoleWriter.flush();

            Thread.sleep(200);

            if (command.contains("STUDENTSTAT") || command.contains("COURSESTAT"))
                response = consoleReader.getAllResponse();
            else response = consoleReader.getLastResponse();
            consoleReader.clearLastResponse();
            consoleReader.clearAllResponse();
        }catch(Exception e){
            e.printStackTrace();
        }
        return response;
    }
}