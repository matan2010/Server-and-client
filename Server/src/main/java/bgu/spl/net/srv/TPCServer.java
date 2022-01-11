package bgu.spl.net.srv;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.MessagingProtocol;

import java.io.IOException;
import java.util.function.Supplier;

public class TPCServer<T> extends BaseServer<T> {

    public TPCServer(int port,
                     Supplier<MessagingProtocol<T>> protocolFactory,
                     Supplier<MessageEncoderDecoder<T>> encdecFactory) {
        super(port, protocolFactory, encdecFactory);
    }

    @Override
    protected void execute(BlockingConnectionHandler<T> handler) {
        new Thread(handler).run();
    }
}
