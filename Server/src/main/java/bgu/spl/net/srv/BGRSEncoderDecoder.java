package bgu.spl.net.srv;

import bgu.spl.net.api.MessageEncoderDecoder;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

public class BGRSEncoderDecoder implements MessageEncoderDecoder<String[]> {
    private byte[] bytes = new byte[1 << 10];
    private int len = 0;
    private int numberOfZeros = 0;
    private short currOpcode;
    private boolean isCurrOpcodeAvailable = false;
    private int bytesCounter = 0;
    private boolean allowReturnPopString = false;


    @Override
    public String[] decodeNextByte(byte nextByte) {
        bytesCounter++;
        if (nextByte == '\0') {
            numberOfZeros++;
        }

        if (!isCurrOpcodeAvailable) {
            pushByte(nextByte);
        }

        //switch cases - opcode is available, from this byte handle with data
        if (isCurrOpcodeAvailable) {
            //yellow case
            if (currOpcode == 1 || currOpcode == 2 || currOpcode == 3) {
                continueDecodingYellowCase(nextByte);
            }
            //pink case
            else if (currOpcode == 5 || currOpcode == 6 || currOpcode == 7 || currOpcode == 9 || currOpcode == 10) {
                continueDecodingPinkCase(nextByte);
            }
            //red case
//            else if (currOpcode == 4 || currOpcode == 11) {
//                continueDecodingRedCase(nextByte);
//            }
            //blue case
            else if (currOpcode == 8) {
                continueDecodingBlueCase(nextByte);
            }
        }

        //figuring opcode
        if (bytesCounter == 2) {
            byte[] opcodeByte = new byte[2];
            opcodeByte[0] = bytes[0];
            opcodeByte[1] = bytes[1];
            currOpcode = bytesToShort(opcodeByte);
            isCurrOpcodeAvailable = true;
            if (currOpcode == 4 || currOpcode == 11) {
                allowReturnPopString = true;
            }
        }

        if (allowReturnPopString) {
            clearFields();
            return popString();
        }

        return null;
    }

    private void continueDecodingYellowCase(byte nextByte) {
        // number of zeros limit = 3
        pushByte(nextByte);
        if (numberOfZeros == 3) {
            allowReturnPopString = true;
        }
    }

    private void continueDecodingPinkCase(byte nextByte) {
        // number of zeros limit = 2
        pushByte(nextByte);
        if (bytesCounter == 4) {
            allowReturnPopString = true;
        }
    }

//    private void continueDecodingRedCase(byte nextByte) {
//        // number of zeros limit = 1
//        pushByte(nextByte);
//        allowReturnPopString = true;
//    }

    private void continueDecodingBlueCase(byte nextByte) {
        // number of zeros limit = 2
        pushByte(nextByte);
        if (numberOfZeros == 2)
            allowReturnPopString = true;
    }

    private void clearFields() {
        numberOfZeros = 0;
        isCurrOpcodeAvailable = false;

        allowReturnPopString = false;
    }

    // we can return only ack or err
    @Override
    public byte[] encode(String[] message) {
        //opcode: from string to short
        short ACK = 12;
        short ERR = 13;
        byte[] ackOrErrOpcodeBytesArray;
        byte[] commandOpcpdeBytesArray;

        if (message[0].equals("ACK")) {
            ackOrErrOpcodeBytesArray = shortToBytes(ACK);
        }
        //(message[0].equals("ERR"))
        else {
            ackOrErrOpcodeBytesArray = shortToBytes(ERR);
        }

        short commandOpcpde = Short.parseShort(message[1]);
        commandOpcpdeBytesArray = shortToBytes(commandOpcpde);

        ArrayList<Byte> arrayListOfBytes = new ArrayList<Byte>();
        byte[] output;

        arrayListOfBytes.add(ackOrErrOpcodeBytesArray[0]);
        arrayListOfBytes.add(ackOrErrOpcodeBytesArray[1]);

        arrayListOfBytes.add(commandOpcpdeBytesArray[0]);
        arrayListOfBytes.add(commandOpcpdeBytesArray[1]);

        //add optional data here!!!
        if (message.length == 3) {
            byte[] additionalData = message[2].getBytes(StandardCharsets.UTF_8);
            for (int i = 0; i < additionalData.length; i++) {
                arrayListOfBytes.add(additionalData[i]);
            }
        }
        output = new byte[arrayListOfBytes.size() + 1];
        for (int i = 0; i < arrayListOfBytes.size(); i++) {
            output[i] = arrayListOfBytes.get(i);
        }
        arrayListOfBytes.add((byte) 0);
        return output;
    }

    private void pushByte(byte nextByte) {
        if (len >= bytes.length) {
            bytes = Arrays.copyOf(bytes, len * 2);
        }

        bytes[len++] = nextByte;
    }

    private String[] popString() {
        //notice that we explicitly requesting that the string will be decoded from UTF-8
        //this is not actually required as it is the default encoding in java.
        //String result = new String(bytes, 0, len, StandardCharsets.UTF_8);//

        //got bytes array, now make it String[]
        String[] ans;

        //  red case, for opcodes: 4,11
        if (currOpcode == 4 | currOpcode == 11) {
            ans = new String[1];
            ans[0] = currOpcode + "";
        }

        // yellow case, for opcodes: 1,2,3
        else if (currOpcode == 1 | currOpcode == 2 | currOpcode == 3) {
            // get username from bytes
            // // finding index of the first '/0' after username
            int endOfUsernameBytesIndex = 0;
            for (int i = 2; i < bytesCounter; i++) {

                if (bytes[i] == 0) {
                    endOfUsernameBytesIndex = i;
                    break;
                }
            }
            // // get username in bytes
            byte[] usernameInBytes = new byte[endOfUsernameBytesIndex - 2];
            for (int i = 0; i < endOfUsernameBytesIndex - 2; i++) {
                usernameInBytes[i] = bytes[i + 2];
            }
            // // convert username into a String
            String username = new String(usernameInBytes, StandardCharsets.UTF_8);

            // get password from bytes
            // // get password in bytes
            byte[] passwordInBytes = new byte[bytesCounter - 4 - usernameInBytes.length];
            int currIndex = 0;
            for (int i = endOfUsernameBytesIndex + 1; i < bytesCounter - 1; i++) {
                passwordInBytes[currIndex] = bytes[i];
                currIndex++;
            }
            // // convert password into a String
            String password = new String(passwordInBytes, StandardCharsets.UTF_8);

            //preparing ans return value
            ans = new String[3];
            ans[0] = currOpcode + "";
            ans[1] = username;
            ans[2] = password;
            // pink case, for opcodes: 5,6,7,9,10
        } else if (currOpcode == 5 || currOpcode == 6 || currOpcode == 7 || currOpcode == 9 || currOpcode == 10) {

            // getting course number bytes from bytes[]
            byte[] courseNumBytes;
            courseNumBytes = new byte[2];
            courseNumBytes[0] = bytes[2];
            courseNumBytes[1] = bytes[3];

            // converting courseNumBytes into a short courseNum
            short courseNum = 0;
            courseNum= bytesToShort(courseNumBytes);
            ans = new String[2];
            ans[0] = currOpcode + "";
            ans[1] = courseNum + "";

        }
        // blue case, for opcode: 8
        else {
            // // finding index of the first '/0' after username
            int endOfUsernameBytesIndex = 0;
            for (int i = 2; i < bytesCounter; i++) {
                if (bytes[i] == 0) {
                    endOfUsernameBytesIndex = i;
                    break;
                }
            }
            // // get username in bytes
            byte[] usernameInBytes = new byte[endOfUsernameBytesIndex - 2];
            for (int i = 0; i < endOfUsernameBytesIndex - 2; i++) {
                usernameInBytes[i] = bytes[i + 2];
            }
            // // convert username into a String
            String username = new String(usernameInBytes, StandardCharsets.UTF_8);

            // preparing ans return value
            ans= new String[2];
            ans[0] = currOpcode+"";
            ans[1] = username;
        }
        len = 0;
        currOpcode = 0;
        bytesCounter = 0;
        //need to clear bytes!!!!
        bytes = new byte[1 << 10];
        return ans;
    }

    public short bytesToShort(byte[] byteArr) {
        short result = (short) ((byteArr[0] & 0xff) << 8);
        result += (short) (byteArr[1] & 0xff);
        return result;
    }

    public byte[] shortToBytes(short num) {
        byte[] bytesArr = new byte[2];
        bytesArr[0] = (byte) ((num >> 8) & 0xFF);
        bytesArr[1] = (byte) (num & 0xFF);
        return bytesArr;
    }

    private short decodeOpcode() {
        byte[] opcodeInBytes = new byte[2];
        opcodeInBytes[0] = bytes[0];
        opcodeInBytes[1] = bytes[1];
        return bytesToShort(opcodeInBytes);
    }
}
