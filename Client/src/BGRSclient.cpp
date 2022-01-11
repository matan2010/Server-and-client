#include <stdlib.h>
#include <thread>
#include "../include/connectionHandler.h"

/**
* This code assumes that the server replies the exact text the client sent it (as opposed to the practical session example)
*/

int main (int argc, char *argv[]) {

    if (argc < 3) {

        std::cerr << "Usage: " << argv[0] << " host port" << std::endl << std::endl;
        return 1;
    }

    std::string host = argv[1];
    short port = atoi(argv[2]);

    ConnectionHandler connectionHandler(host, port);
    // getting cin lines from user
    std::thread th0(&ConnectionHandler::run, &connectionHandler);
    if (!connectionHandler.connect()) {
        std::cerr << "Cannot connect to " << host << ":" << port << std::endl;
        return 1;
    }

    // getting answers from server
    while (true) {

        std::string ans;
        if (!connectionHandler.getLine(ans)) {
            std::cout << "Disconnected. Exiting...\n" << std::endl;
            break;
        }
        std::cout << ans << std::endl;
        if (ans=="ACK 4")
            break;
    }
    return 0;
}