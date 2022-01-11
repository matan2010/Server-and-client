package bgu.spl.net.srv;

import bgu.spl.net.api.MessagingProtocol;

//TODO: check the (T) casting if needed

public class BGRSProtocol<T> implements MessagingProtocol<T> {
    private boolean shouldTerminate = false;
    private boolean isLogin = false;
    private Database database = Database.getInstance();
    private User user = null;



    public T process(T msg) {
        String[] myInfo = (String[]) msg;
        short opcode = Short.parseShort(myInfo[0]);


        //Register Admin
        if (opcode == 1) {

            String username = myInfo[1];
            String password = myInfo[2];
            if (database.registerAdmin(username, password)) {
                setUser(database.getUser(username));
                return (T) ack(opcode);
            } else {
                return (T) err(opcode);
            }
        }

        //Register Student
        if (opcode == 2) {
            String username = myInfo[1];
            String password = myInfo[2];
            if (database.registerStudent(username, password)) {
                setUser(database.getUser(username));
                return (T) ack(opcode);
            } else {
                return (T) err(opcode);
            }
        }

        //login
        if (opcode == 3) {
            String username = myInfo[1];
            String password = myInfo[2];
            if (database.login(username, password)) {
                setLogin(true);
                setUser(database.getUser(username));
                return (T)ack(opcode);
            } else {
                return (T)err(opcode);
            }
        }

        //Logout
        if (opcode == 4) {
            if (isLogin) {
                database.logout(this.user.getUsername()); //maybe not needed
                setLogin(false);
                shouldTerminate = true;
                return (T) ack(opcode);
            } else {
                return (T) err(opcode);
            }
        }

        //Register To Course
        if (opcode == 5) {

            String courseNum = myInfo[1];
            if (isLogin) {                      //gor here "(" instead of "40"
                if (database.registerToCourse(Integer.parseInt(courseNum), user.getUsername())) {
                    return (T) ack(opcode);
                }
                return (T) err(opcode);
            } else {
                return (T) err(opcode);
            }
        }

        //Check Kdam Course
        if (opcode == 6) {
            String courseNum = myInfo[1];
            String kdamCourseString = null;
            if (isLogin) {
                kdamCourseString = database.checkKdamCourse(Integer.parseInt(courseNum));
            }
            if (kdamCourseString != null) {
                return (T) ack(opcode, kdamCourseString);
            }
            return (T) err(opcode);
        }

        //Print Course Status
        if (opcode == 7) {
            String courseNum = myInfo[1];
            String courseStat = database.printCourseStat(Integer.parseInt(courseNum));

            if (isLogin) {
                if (courseStat != null) {
                    return (T) ack(opcode, courseStat);
                }
            }
            return (T) err(opcode);
        }

        //Print Student Status
        if (opcode == 8) {
            String userName = myInfo[1];
            String studentStat = database.printStudentStat(userName);
            if (isLogin) {
                if (user instanceof Admin) {
                    if (studentStat != null) {
                        return (T) ack(opcode, studentStat);
                    }
                }
            }
            return (T) err(opcode);
        }

        //Check If User Is Registered To Course
        if (opcode == 9) {
            if (isLogin) {
                String courseNum = myInfo[1];
                String ans = database.isStudentRegisteredToCourse(user.getUsername(), Integer.parseInt(courseNum));
                if (ans != null) {
                    return (T) ack(opcode, ans);
                }
            }
            return (T) err(opcode);
        }

        //Unregister From Course
        if (opcode == 10) {
            if (isLogin) {
                String courseNum = myInfo[1];
                if (database.unregisterFromCourse(user.getUsername(), Integer.parseInt(courseNum))) {
                    return (T) ack(opcode);
                }
            }
            return (T) err(opcode);
        }

        //Check My Current Courses
        if (opcode == 11) {
            if (isLogin) {
                String ans = database.printMyCurrentCourses(user.getUsername());
                if (ans != null) {
                    return (T) ack(opcode, ans);
                }
            }
        }
        System.out.println("no opcode recognized: opcode is: " + opcode);
        return null;
    }

    public boolean shouldTerminate() {
        return shouldTerminate;
    }

    private void setUser(User user) {
        this.user = user;
    }

    private String[] ack(short opcode) {
        String[] output = new String[2];
        output[0] = "ACK";
        output[1] = opcode + "";
        System.out.println(output[0] + output[1]);
        return output;
    }

    private String[] ack(short opcode, String additionalData) {
        String[] output = new String[3];
        output[0] = "ACK";
        output[1] = opcode + "";
        output[2] = " " + additionalData;
        System.out.println(output[0] + output[1] + output[2]);
        return output;
    }

    private String[] err(short opcode) {
        String[] output = new String[2];
        output[0] = "ERROR";
        output[1] = opcode + "";
        System.out.println(output[0] + output[1]);
        return output;
    }


    private void setLogin(boolean turnTo) {
        this.isLogin = turnTo;
    }


}
