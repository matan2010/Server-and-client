package bgu.spl.net.impl.BGRSServer;

import bgu.spl.net.srv.BGRSEncoderDecoder;
import bgu.spl.net.srv.BGRSProtocol;
import bgu.spl.net.srv.Reactor;

public class ReactorMain {
    public static void main(String[] args) {
        int port = Integer.parseInt(args[0]);
        int numOfThreads = Integer.parseInt(args[1]);
        Reactor<String[]> server = new Reactor<>(
                numOfThreads,
                port,
                () -> new BGRSProtocol<>(),
                () -> new BGRSEncoderDecoder());
        server.serve();

    }
}
