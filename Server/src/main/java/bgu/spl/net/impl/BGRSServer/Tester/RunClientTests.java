package bgu.spl.net.impl.BGRSServer.Tester;


public class RunClientTests {

        public static void main(String[] args) {
                new Thread(new Tests()).start();
        }
}