#include "../include/connectionHandler.h"

using boost::asio::ip::tcp;

using std::cin;
using std::cout;
using std::cerr;
using std::endl;
using std::string;

#include <iostream>
#include <vector>
#include <unordered_map>

using namespace std;

ConnectionHandler::ConnectionHandler(string host, short port) : host_(host), port_(port), io_service_(),
                                                                socket_(io_service_) {}

ConnectionHandler::~ConnectionHandler() {
    close();
}

bool ConnectionHandler::connect() {
    std::cout << "Starting connect to "
              << host_ << ":" << port_ << std::endl;
    try {
        tcp::endpoint endpoint(boost::asio::ip::address::from_string(host_), port_); // the server endpoint
        boost::system::error_code error;
        socket_.connect(endpoint, error);
        if (error)
            throw boost::system::system_error(error);
    }
    catch (std::exception &e) {
        std::cerr << "Connection failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}

//as is - do not touch
bool ConnectionHandler::getBytes(char bytes[], unsigned int bytesToRead) {
    size_t tmp = 0;
    boost::system::error_code error;
    try {
        while (!error && bytesToRead > tmp) {

            tmp += socket_.read_some(boost::asio::buffer(bytes + tmp, bytesToRead - tmp), error);
        }
        if (error)
            throw boost::system::system_error(error);
    } catch (std::exception &e) {
        std::cerr << "recv failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}

//as is - do not touch
bool ConnectionHandler::sendBytes(const char bytes[], int bytesToWrite) {
    int tmp = 0;
    boost::system::error_code error;
    try {
        while (!error && bytesToWrite > tmp) {
            tmp += socket_.write_some(boost::asio::buffer(bytes + tmp, bytesToWrite - tmp), error);
        }
        if (error)
            throw boost::system::system_error(error);
    } catch (std::exception &e) {
        std::cerr << "recv failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}


bool ConnectionHandler::getLine(std::string &line) {
    return getFrameBGRS(line, '\n');
}

bool ConnectionHandler::sendLine(std::string &line) {
    return sendFrameBGRS(line, '\n');
}

//get and line and decode ACK or ERR, and optional data.
bool ConnectionHandler::getFrameBGRS(std::string &frame, char delimiter) {

    // Stop when we encounter the null character.
    // Notice that the null character is not appended to the frame string.
    char opcodeInBytes[2];
    opcodeInBytes[0] = '\0';
    opcodeInBytes[1] = '\0';

    try {
        short opcode;
        //not maneged to read opcode
        if (!getBytes(opcodeInBytes, 2)) {
            return false;
        }

        opcode = bytesToShort(opcodeInBytes);

        if (opcode == 12) {
            return decodeACK(frame);
        } else if (opcode == 13) {
            return decodeERR(frame);
        }
        else{
            cout<< "Error in reading opcode" << endl;
            return false;
        }
    } catch (std::exception &e) {
        std::cerr << "recv failed2 (Error: " << e.what() << ')' << std::endl;
        return false;
    }
}

//switch first command from string to opcode, and encode the rest to UTF8(ASCII)
bool ConnectionHandler::sendFrameBGRS(const std::string &frame, char delimiter) {
    //build fix vector of chars with opcode + parameter1 + parameter 2....
//    string frameCopy = frame;
//    buildFixCharVector(&frameCopy);
    // break into myInfo vector


    std::vector<std::string> myInfo = splitMsg(frame);
    std::vector<char> vectorOfChars;
    // map of commands
    std::unordered_map<std::string, short> messageType;

    messageType = {{"ADMINREG",     1},
                   {"STUDENTREG",   2},
                   {"LOGIN",        3},
                   {"LOGOUT",       4},
                   {"COURSEREG",    5},
                   {"KDAMCHECK",    6},
                   {"COURSESTAT",   7},
                   {"STUDENTSTAT",  8},
                   {"ISREGISTERED", 9},
                   {"UNREGISTER",   10},
                   {"MYCOURSES",    11}
    };

    short opcode = messageType.at(myInfo.at(0));


    // all myInfo cells combined - notice that fixed opcode combined instead of "COMMAND"
    if (opcode == 1 || opcode == 2 || opcode == 3) {
        vectorOfChars = createCharVector3Vrb(myInfo, opcode);
    } else if (opcode == 4 || opcode == 11) {
        vectorOfChars = createCharVector1Vrb(myInfo, opcode);
    } else if (opcode == 5 || opcode == 6 || opcode == 7 || opcode == 9 || opcode == 10 || opcode == 8) {
        vectorOfChars = createCharVector2Vrb(myInfo, opcode);
    } else { return false; }

    char arrayOfChars[vectorOfChars.size()];
    //convert vector of chars to array of chars
    for (int i = 0; i < vectorOfChars.size(); i++) {
        arrayOfChars[i] = vectorOfChars[i];
    }
    // return sendBytes
    bool isSent= sendBytes(arrayOfChars, vectorOfChars.size());
    return isSent;

}

// Close down the connection properly.
void ConnectionHandler::close() {
    try {
        socket_.close();
    } catch (...) {
        std::cout << "closing failed: connection already closed" << std::endl;
    }

}

// Decode 2 bytes to short
short ConnectionHandler::bytesToShort(char *bytesArr) {
    short result = (short) ((bytesArr[0] & 0xff) << 8);
    result += (short) (bytesArr[1] & 0xff);
    return result;
}

// Encode short to 2 bytes
void ConnectionHandler::shortToBytes(short num, char *bytesArr) {
    bytesArr[0] = ((num >> 8) & 0xFF);
    bytesArr[1] = (num & 0xFF);
}

bool ConnectionHandler::decodeACK(std::string &frame) {
    //first adding ACK to frame string, after getting 2 first bytes.
    frame.append("ACK ");
    //second, getting next 2 bytes, the opcode of the acked command and adding opcode to frame string.
    char ackedCommandOpcodeBytes[2];
    try {
        if (!getBytes(ackedCommandOpcodeBytes, 2)) {
            return false;
        }

        short ackedCommandOpcode = bytesToShort(ackedCommandOpcodeBytes);
        frame.append(std::to_string(ackedCommandOpcode));

        //check if there is an additional data
        //if the acked command is KDAMCHECK, COURSESTAT, STUDENTSTAT, ISREGISTERED OR MYCOURSES there will be an additional data.
        if (ackedCommandOpcode == 6 || ackedCommandOpcode == 7 || ackedCommandOpcode == 8 ||
            ackedCommandOpcode == 9 ||
            ackedCommandOpcode == 11) {
            //checkit!!!!!!!!!!
            return decodeAdditionalData(frame);
        }
        else{
            if (!getBytes(ackedCommandOpcodeBytes,1))
                return false;
        }
        //otherwise, finished.
        return true;
    } catch (std::exception &exception) {
        return false;
    }
}

bool ConnectionHandler::decodeERR(std::string &frame) {
    //first adding ERROR to frame string, after getting 2 first bytes.
    frame.append("ERROR ");
    //second, getting next 2 bytes, the opcode of the errored command
    char erroredCommandOpcodeBytes[2];
    try {
        if (!getBytes(erroredCommandOpcodeBytes, 2)) {
            return false;
        }
        // adding opcode to frame string.
        short erroredCommandOpcode = bytesToShort(erroredCommandOpcodeBytes);
        frame.append(std::to_string(erroredCommandOpcode));
        return true;
    } catch (std::exception &exception) {
        return false;
    }
}

bool ConnectionHandler::decodeAdditionalData(std::string &frame) {
    frame.append(" ");
    char ch[1];
    do {
        if (!getBytes(ch, 1)) {
            return false;
        }
        if (ch[0] != '\0') {
            frame.append(1, ch[0]);
        }
    } while (ch[0] != '\0');
    return true;

}

//returns a vector with all the organized data
std::vector<std::string> ConnectionHandler::splitMsg(std::string msg) {
    std::vector<std::string> myInfo;
    string buff = "";
    for (int i = 0; i < msg.length(); i++) {
        if (msg[i] != ' ')
            buff += msg[i];
        if ((i + 1) == msg.length() || msg[i + 1] == ' ') {
            myInfo.push_back(buff);
            buff = "";
        }
    }
    return myInfo;
}

std::vector<char> ConnectionHandler::buildFixCharVector(std::string &msg) {

    // break into myInfo vector
    std::vector<std::string> myInfo = splitMsg(msg);
    // map of commands
    std::unordered_map<std::string, short> messageType;
    messageType = {{"ADMINREG",     1},
                   {"STUDENTREG",   2},
                   {"LOGIN",        3},
                   {"LOGOUT",       4},
                   {"COURSEREG",    5},
                   {"KDAMCHECK",    6},
                   {"COURSESTAT",   7},
                   {"STUDENTSTAT",  8},
                   {"ISREGISTERED", 9},
                   {"UNREGISTER",   10},
                   {"MYCOURSES",    11}
    };
    short opcode = messageType.at(myInfo.at(0));
    // all myInfo cells combined - notice that fixed opcode combined insted of "COMMAND"

    if (opcode == 1 || opcode == 2 || opcode == 3) {
        return createCharVector3Vrb(myInfo, opcode);
    } else if (opcode == 4 || opcode == 11) {
        return createCharVector1Vrb(myInfo, opcode);
    } else if (opcode == 5 || opcode == 6 || opcode == 7 || opcode == 9 || opcode == 10 || opcode == 8) {
        return createCharVector2Vrb(myInfo, opcode);

    };
}
//"yellow kind of command"
std::vector<char> ConnectionHandler::createCharVector3Vrb(std::vector<std::string> &myInfo, short opcode) {
    std::vector<char> output;
    string username = myInfo.at(1);
    string password = myInfo.at(2);

    //push 2 bytes of short opcode
    char bytes[2];
    shortToBytes(opcode, bytes);
    output.push_back(bytes[0]);
    output.push_back(bytes[1]);

    //push username
    for (int i = 0; i < username.length(); i++) {
        output.push_back(username[i]);
    }
    //push password
    output.push_back('\0');
    for (int i = 0; i < password.length(); i++) {
        output.push_back(password[i]);
    }
    output.push_back('\0');

    return output;
}

//"red kind of command"
std::vector<char> ConnectionHandler::createCharVector1Vrb(std::vector<std::string> &myInfo, short opcode) {
    std::vector<char> output;
    //push 2 bytes of short opcode
    char bytes[2];
    shortToBytes(opcode, bytes);
    output.push_back(bytes[0]);
    output.push_back(bytes[1]);

    return output;
}

std::vector<char> ConnectionHandler::createCharVector2Vrb(std::vector<std::string> &myInfo, short opcode) {
    std::vector<char> output;
    //push 2 bytesOpcode of short opcode
    char bytesOpcode[2];
    shortToBytes(opcode, bytesOpcode);
    output.push_back(bytesOpcode[0]);
    output.push_back(bytesOpcode[1]);

    //push course number
        //"pink" kind of command, opcodes: 5,6,7,9,10
    if (opcode != 8) {
        string courseNumString = myInfo.at(1);
        int courseNumInt = stoi(courseNumString);
        short courseNum = (short) courseNumInt;
        char bytesCourseNum[2];
        shortToBytes(courseNum, bytesCourseNum);
        output.push_back(bytesCourseNum[0]);
        output.push_back(bytesCourseNum[1]);

    }
        //studentStat user name
        //"blue" kind of command
    else {
        string username = myInfo.at(1);
        for (int i = 0; i < username.length(); i++) {
            output.push_back(username[i]);
        }
        output.push_back('\0');
    }
    return output;

}
// run method read from cin, terminates if  LOGOUT received from cin
void ConnectionHandler::run(){
    while(true){
        // init byte (char) buffer
        const short buffSize = 1024;
        char buff[buffSize];
        //read from screen and put it as a string
        std::cin.getline(buff, buffSize);
        std::string line(buff);
        //send message(encode is included)
        if (!sendLine(line)){
            cout<<"Disconnected Exiting...\n" << endl;
            break;
        }

        if(line=="logout")
            break;
    }
}











