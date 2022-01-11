#ifndef CONNECTION_HANDLER__
#define CONNECTION_HANDLER__

#include <string>
#include <iostream>
#include <boost/asio.hpp>

using boost::asio::ip::tcp;

class ConnectionHandler {
private:
    const std::string host_;
    const short port_;
    boost::asio::io_service io_service_;   // Provides core I/O functionality
    tcp::socket socket_;

public:
    ConnectionHandler(std::string host, short port);

    virtual ~ConnectionHandler();

    // Connect to the remote machine
    bool connect();

    // Read a fixed number of bytes from the server - blocking.
    // Returns false in case the connection is closed before bytesToRead bytes can be read.
    bool getBytes(char bytes[], unsigned int bytesToRead);

    // Send a fixed number of bytes from the client - blocking.
    // Returns false in case the connection is closed before all the data is sent.
    bool sendBytes(const char bytes[], int bytesToWrite);

    // Read an ascii line from the server
    // Returns false in case connection closed before a newline can be read.
    bool getLine(std::string &line);

    // Send an ascii line from the server
    // Returns false in case connection closed before all the data is sent.
    bool sendLine(std::string &line);

    // Get Ascii data from the server until the delimiter character
    // Returns false in case connection closed before null can be read.
    bool getFrameBGRS(std::string &frame, char delimiter);

    // Send a message to the remote host.
    // Returns false in case connection is closed before all the data is sent.
    bool sendFrameBGRS(const std::string &frame, char delimiter);

    // Close down the connection properly.
    void close();

    // run method read from cin, terminate when LOGOUT recieved from cin
    void run();

private:
    // Decode 2 bytes to short
    short bytesToShort(char *bytesArr);
    //Encode shortToBytes
    void shortToBytes(short num, char *bytesArr);;

    bool decodeERR(std::string &frame);

    bool decodeACK(std::string &frame);

    bool decodeAdditionalData(std::string &frame);

    std::vector<std::string> splitMsg (std::string msg);

    std::vector<char> buildFixCharVector(std:: string& msg);

    std::vector<char> createCharVector1Vrb(std::vector<std::string> &myInfo, short opcode);

    std::vector<char> createCharVector2Vrb(std::vector<std::string>& myInfo, short opcode);

    std::vector<char> createCharVector3Vrb(std::vector<std::string>& myInfo, short opcode);

}; //class ConnectionHandler




#endif