package bgu.spl.net.impl.BGRSServer;


import bgu.spl.net.srv.BGRSEncoderDecoder;
import bgu.spl.net.srv.BGRSProtocol;
import bgu.spl.net.srv.TPCServer;

//Integer.parseInt(args[0])
public class TPCMain {
    public static void main(String[] args) {
        TPCServer<String[]> server = new TPCServer<>(
                7777,
                ()->new BGRSProtocol<>(),
                ()->new BGRSEncoderDecoder());
        server.serve();
    }
}
