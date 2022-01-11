package bgu.spl.net.impl.BGRSServer.Tester;

import bgu.spl.net.impl.BGRSServer.TPCMain;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

public class Tests implements Runnable {

    private final int         numThreads = 20;
    private ArrayList<Course> courses    = new ArrayList<>();

    public ArrayList<CommandProcessor> initiateProcesses(int numProcesses) {
        ArrayList<CommandProcessor> commandsProcessors = new ArrayList<>();

        for (int i = 0; i < numProcesses; i++) {
            CommandProcessor cmdProcess = new CommandProcessor();
            cmdProcess.initialize();
            commandsProcessors.add(cmdProcess);
        }
        return commandsProcessors;
    }

    public String testRegistrationSameUser() {
        String response = "";
        try {
            ArrayList<CommandProcessor> commandsProcessors = initiateProcesses(numThreads);
            ConcurrentLinkedQueue<String> outputs = new ConcurrentLinkedQueue<>();

            CountDownLatch threadsEnded = new CountDownLatch(numThreads);
            for (int i = 0; i < numThreads; i++) {
                int tempI = i;
                new Thread(() -> {
                    try {
                        outputs.add(commandsProcessors.get(tempI).sendCommand("STUDENTREG RON RON"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        commandsProcessors.get(tempI).kill();
                        threadsEnded.countDown();
                    }
                }).start();
            }
            threadsEnded.await();
            int numTimesSuccess = 0;
            for (String s : outputs) {
                if (s.equals("ACK 2"))
                    numTimesSuccess++;
            }
            if (numTimesSuccess == 1)
                response = ("Registration With Same User Test Success!");
            else
                response = ("Registration With Same User Test Failed!");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    public String testLoginMultipleSameUser() {
        String response = "";
        try {
            ArrayList<CommandProcessor> commandsProcessors = initiateProcesses(numThreads);
            ConcurrentLinkedQueue<String> outputs = new ConcurrentLinkedQueue<>();

            CountDownLatch threadsEnded = new CountDownLatch(numThreads);
            for (int i = 0; i < numThreads; i++) {
                int tempI = i;
                new Thread(() -> {
                    try {
                        outputs.add(commandsProcessors.get(tempI).sendCommand("LOGIN RON RON"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        commandsProcessors.get(tempI).kill();
                        threadsEnded.countDown();
                    }
                }).start();
            }
            threadsEnded.await();
            int numTimesSuccess = 0;
            for (String s : outputs) {
                if (s.contains("ACK"))
                    numTimesSuccess++;
            }
            if (numTimesSuccess == 1)
                response = ("Login Test Same User Success!");
            else
                response = ("Login Test Same User Failed!");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    public String testRegisLoginLogoutMultipleDifferentUser() {
        String response = "";
        try {
            ArrayList<CommandProcessor> commandsProcessors = initiateProcesses(numThreads);
            ConcurrentLinkedQueue<String> outputs = new ConcurrentLinkedQueue<>();

            CountDownLatch threadsEnded = new CountDownLatch(numThreads);
            for (int i = 0; i < numThreads; i++) {
                int tempI = i;
                new Thread(() -> {
                    try {
                        String username = "RON" + tempI;
                        outputs.add(commandsProcessors.get(tempI).sendCommand("STUDENTREG " + username + " " + username));
                        outputs.add(commandsProcessors.get(tempI).sendCommand("LOGIN " + username + " " + username));
                        outputs.add(commandsProcessors.get(tempI).sendCommand("LOGOUT"));

                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        commandsProcessors.get(tempI).kill();
                        threadsEnded.countDown();
                    }
                }).start();
            }
            threadsEnded.await();
            int numTimesSuccess = 0;
            for (String s : outputs) {
                if (s.contains("ACK"))
                    numTimesSuccess++;
            }
            if (numTimesSuccess == numThreads * 3)
                response = ("Registration & Login  Test Passed");
            else
                response = ("Registration & Login  Test Failed");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    String tryBulkStudentLogicalTests() {
        String response = "";
        try {
            Course p = null;
            for (Course s : courses) {
                if (s.getNumCourseKdam() == 0)  //Choose an active course I can try and get course stat for as a student
                    p = s;
            }
            ArrayList<CommandProcessor> commandsProcessors = initiateProcesses(numThreads);
            ConcurrentLinkedQueue<String> outputs = new ConcurrentLinkedQueue<>();
            CountDownLatch threadsEnded = new CountDownLatch(numThreads);
            for (int i = 0; i < numThreads; i++) {
                int tempI = i;
                Course tempP = p;
                new Thread(() -> {
                    try {
                        String username = "RON" + tempI;

                        outputs.add(commandsProcessors.get(tempI).sendCommand("LOGIN " + username + " " + username));
                        outputs.add(commandsProcessors.get(tempI).sendCommand("COURSESTAT " + tempP.getCourseNum())); //Cant access admin commands
                        outputs.add(commandsProcessors.get(tempI).sendCommand("STUDENTREG L" + tempI + " L" + tempI)); //Shouldn't work after you are already logged in
                        outputs.add(commandsProcessors.get(tempI).sendCommand("ADMINREG L" + tempI + " L" + tempI)); //Shouldn't work after you are already logged in
                        outputs.add(commandsProcessors.get(tempI).sendCommand("STUDENTSTAT RON" + (tempI - 1))); //ADMIN COMMAND
                        outputs.add(commandsProcessors.get(tempI).sendCommand("LOGOUT"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        commandsProcessors.get(tempI).kill();
                        threadsEnded.countDown();
                    }
                }).start();
            }
            threadsEnded.await();
            int numTimesSuccess = 0;
            for (String s : outputs) {
                if (s.equals("ACK 2") || s.equals("ACK 1")) { //STUDENT REG SHALL FAIL SINCE WE ALREADY LOGGED
                    response = ("STUDENTREG OR ADMINREG Commands Work After Login | Invalid Behaviour....");
                    numTimesSuccess++;
                    break;
                }
                if (s.equals("ACK 3") || s.equals("ACK 4"))
                    numTimesSuccess++;
            }

            if (numTimesSuccess == numThreads * 2)
                response = ("Student Logical Tests Test Passed!");
            else
                response = ("Student Logical Tests Test Failed!");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }


    String tryBulkAdminLogicalTests() {
        String response = "";
        try {
            Course p = getCourseWithNoKdams();
            ArrayList<CommandProcessor> commandsProcessors = initiateProcesses(numThreads);
            ConcurrentLinkedQueue<String> outputs = new ConcurrentLinkedQueue<>();
            CountDownLatch threadsEnded = new CountDownLatch(numThreads);
            for (int i = 0; i < numThreads; i++) {
                int tempI = i;
                new Thread(() -> {
                    try {
                        String username = "AdminRON" + tempI;

                        outputs.add(commandsProcessors.get(tempI).sendCommand("ADMINREG " + username + " " + username));
                        outputs.add(commandsProcessors.get(tempI).sendCommand("LOGIN " + username + " " + username));
                        outputs.add(commandsProcessors.get(tempI).sendCommand("COURSEREG " + p.getCourseNum())); //Shouldn't work since admin
                        outputs.add(commandsProcessors.get(tempI).sendCommand("KDAMCHECK " + p.getCourseNum())); //Shouldn't work since admin
                        outputs.add(commandsProcessors.get(tempI).sendCommand("MYCOURSES")); //Shouldn't work since admin
                        outputs.add(commandsProcessors.get(tempI).sendCommand("UNREGISTER " + p.getCourseNum())); //Shouldn't work since admin
                        outputs.add(commandsProcessors.get(tempI).sendCommand("ISREGISTERED " + p.getCourseNum()));//Shouldn't work since admin
                        outputs.add(commandsProcessors.get(tempI).sendCommand("LOGOUT"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        commandsProcessors.get(tempI).kill();
                        threadsEnded.countDown();
                    }
                }).start();
            }
            threadsEnded.await();
            int numTimesSuccess = 0;
            for (String s : outputs) {
                if (s.equals("ACK 2") || s.equals("ACK 6") || s.equals("ACK 5") || s.equals("ACK 10") || s.equals("ACK 9")) { //STUDENT REG SHALL FAIL SINCE WE ALREADY LOGGED
                    response = ("STUDENTREG Command Works For Admin | Invalid Behaviour....");
                    numTimesSuccess++;
                    break;
                }
                if (s.equals("ACK 3") || s.equals("ACK 4") || s.equals("ACK 1"))
                    numTimesSuccess++;
            }
            if (numTimesSuccess == numThreads * 3)
                response = ("Admin Logical Tests Test Passed!");
            else
                response = ("Admin Logical Tests Test Failed!");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    public String testRegisterCourseWithoutAnyKdam() {
        String response = "";
        try {
            Course p = null;
            for (Course s : courses) {
                if (s.getNumCourseKdam() > 0)
                    p = s;
            }
            ArrayList<CommandProcessor> commandsProcessors = initiateProcesses(numThreads);
            ConcurrentLinkedQueue<String> outputs = new ConcurrentLinkedQueue<>();
            CountDownLatch threadsEnded           = new CountDownLatch(numThreads);
            for (int i = 0; i < numThreads; i++) {
                int tempI = i;
                Course tempP = p;
                new Thread(() -> {
                    try {
                        String username = "RON" + tempI;
                        outputs.add(commandsProcessors.get(tempI).sendCommand("LOGIN " + username + " " + username));
                        outputs.add(commandsProcessors.get(tempI).sendCommand("COURSEREG " + tempP.getCourseNum()));
                        outputs.add(commandsProcessors.get(tempI).sendCommand("LOGOUT"));

                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        commandsProcessors.get(tempI).kill();
                        threadsEnded.countDown();
                    }
                }).start();
            }
            threadsEnded.await();
            int numTimesSuccess = 0;
            for (String s : outputs) {
                if (s.equals("ACK 3"))
                    numTimesSuccess++;
                if (s.equals("ACK 5"))
                    numTimesSuccess--;
            }
            if (numTimesSuccess != numThreads)
                response = "CourseReg Test - Kdam Test 1 Failed (Trying to register course that has kdam without kdam)";
            else
                response = "CourseReg Test - Kdam Test 1 Passed";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }
    public String testRegisterCourseWithKdam(){
        String response = "";
        try {
            Course p= getCourseWithKdam();
            int numOfSpotsInCourse  = p.getNumOfAvailbleSpots();
            int numOfSpotsInKdam    = p.getKdamCourses().get(0).getNumOfAvailbleSpots();

            int actualSpots         = Math.min(numOfSpotsInCourse,numOfSpotsInKdam);
            ArrayList<CommandProcessor>   commandsProcessors = initiateProcesses(actualSpots+2);
            ConcurrentLinkedQueue<String> outputs            = new ConcurrentLinkedQueue<>();
            CountDownLatch threadsEnded = new CountDownLatch(actualSpots+2);
            for (int i = 0; i < actualSpots+2; i++) {
                int    finalI = i;
                Course finalP = p;
                new Thread(() -> {
                    try {
                        String username = "RON" + finalI;
                        outputs.add(commandsProcessors.get(finalI).sendCommand("LOGIN " + username + " " + username));
                        outputs.add(commandsProcessors.get(finalI).sendCommand("ISREGISTERED " + finalP.getCourseNum()));
                        outputs.add(commandsProcessors.get(finalI).sendCommand("COURSEREG " + finalP.getKdamCourses().get(0).getCourseNum()));
                        outputs.add(commandsProcessors.get(finalI).sendCommand("COURSEREG " + finalP.getCourseNum()));
                        outputs.add(commandsProcessors.get(finalI).sendCommand("ISREGISTERED " + finalP.getCourseNum()));
                        outputs.add(commandsProcessors.get(finalI).sendCommand("UNREGISTER " + finalP.getKdamCourses().get(0).getCourseNum()));
                        outputs.add(commandsProcessors.get(finalI).sendCommand("UNREGISTER " + finalP.getCourseNum()));
                        outputs.add(commandsProcessors.get(finalI).sendCommand("LOGOUT"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        commandsProcessors.get(finalI).kill();
                        threadsEnded.countDown();
                    }
                }).start();
            }
            threadsEnded.await();

            int numTimesSuccess       = 0;
            int numTimesRegistered    = 0;
            int numTimesNotRegistered = 0;
            int numTimeSucUnregister=0;
            int numTimesUnsuccessful=0;

            for (String s : outputs) {
                switch (s) {
                    case "ACK 3":
                    case "ACK 5":
                    case "ACK 4":
                        numTimesSuccess++;
                        break;
                    case "ERROR 10":
                    case "ERROR 5":
                        numTimesUnsuccessful++;
                        break;
                    case "ACK 10":
                        numTimeSucUnregister++;
                        break;
                    case "REGISTERED":
                        numTimesRegistered++;
                        break;
                    case "NOT REGISTERED":
                        numTimesNotRegistered++;
                        break;
                }
            }
            int numberOfExtraSpacesInKdam=Math.min(p.getKdamCourses().get(0).numOfMaxStudents-actualSpots,2);
            if(numTimesSuccess != (int)(actualSpots*4+4+numberOfExtraSpacesInKdam) || numTimesRegistered != actualSpots||numTimesUnsuccessful!=(8-numberOfExtraSpacesInKdam*2)
                    || numTimesNotRegistered != actualSpots+4||numTimeSucUnregister!=(int)(actualSpots*2+numberOfExtraSpacesInKdam))
                     response = "CourseReg Test - Kdam Test 2 Failed (Registering Kdam Courses And Then Course)";
            else     response = "CourseReg Test - Kdam Test 2 Passed";
        }catch(Exception e){
            e.printStackTrace();
        }
        return response;
    }

    public String testKdamCheck() {
        CommandProcessor commandProcessor = new CommandProcessor();
        commandProcessor.initialize();
        String login_ron_ron = commandProcessor.sendCommand("LOGIN RON0 RON0");
        if (login_ron_ron.equals("ACK 3")) {
            for (Course course : courses) {
                String output = commandProcessor.sendCommand("KDAMCHECK " + course.getCourseNum());

                if (!output.equals(course.getKdamCoursesAsString()))
                    return "KDAMCHECK Test -Failed the output in not equal to the text file";
            }
        } else return "KDAMCHECK Test -Failed | Failed to Login";
        String logout = commandProcessor.sendCommand("LOGOUT");
        if (logout.equals("ACK 4"))
            return "KDAMCHECK Test - Passed";
        else return "KDAMCHECK Test - Failed | Failed to logout";
    }

    public String testMyCourses() {
        CommandProcessor commandProcessor = new CommandProcessor();
        commandProcessor.initialize();
        commandProcessor.sendCommand("LOGIN RON0 RON0");
        ArrayList<Course> courses1 = registerToCourses(commandProcessor);
        String courseList          = listToString(courses1);
        String myCourses           = commandProcessor.sendCommand("MYCOURSES");
        unregisterFromCourses(commandProcessor,courses1);
        if (!myCourses.equals(courseList)) {
            commandProcessor.sendCommand("LOGOUT");
            return "MYCOURSES Test - Failed 1";
        }
        myCourses=commandProcessor.sendCommand("MYCOURSES");
        commandProcessor.sendCommand("LOGOUT");
        commandProcessor.kill();
        if (!myCourses.equals("[]"))
            return "MYCOURSES Test - Failed 2";
        else return "MYCOURSES Test - Passed";
    }
    public String testStudentStat(){
        CommandProcessor commandProcessorOfStudent=new CommandProcessor();
        commandProcessorOfStudent.initialize();
        CommandProcessor commandProcessorOfAdmin=new CommandProcessor();
        commandProcessorOfAdmin.initialize();
        commandProcessorOfStudent.sendCommand("ADMINREG A A");
        commandProcessorOfStudent.sendCommand("LOGIN RON0 RON0");
        commandProcessorOfAdmin.sendCommand("LOGIN A A");
        ArrayList<Course> courses1 = registerToCourses(commandProcessorOfStudent);
        String courseList="Courses: "+listToString(courses1);
        String output=commandProcessorOfAdmin.sendCommand("STUDENTSTAT RON0");
        String [] tokens=output.split("\n");
        String ack        =tokens[0];
        String studentName=tokens[1];
        String coursesL   =tokens[2];
        unregisterFromCourses(commandProcessorOfStudent,courses1);
        if (!ack.equals("ACK 8")||!studentName.equals("Student: RON0")||!coursesL.equals(courseList)) {
            commandProcessorOfAdmin.sendCommand("LOGOUT");
            commandProcessorOfStudent.sendCommand("LOGOUT");
            return "StudentStat Test - Failed 1";
            //check unregister
        } String output1=commandProcessorOfAdmin.sendCommand("STUDENTSTAT RON0");
        commandProcessorOfAdmin  .sendCommand("LOGOUT");
        commandProcessorOfStudent.sendCommand("LOGOUT");
        String [] tokens1  = output1.split("\n");
        String ack1        = tokens1[0];
        String studentName1= tokens1[1];
        String coursesL1   = tokens1[2];
        if (!ack1.equals("ACK 8")||!studentName1.equals("Student: RON0")||!coursesL1.equals("Courses: []"))
            return "StudentStat Test - Failed 2";
        return "StudentStat Test -Passed";
    }
    public String testCourseStat(){
        Course p=getCourseWithNoKdams();
        int numOfThreads=p.numOfMaxStudents;
        registerStudentsToCourse(p,numOfThreads);
        ArrayList<String> studentList=new ArrayList<>();
        for (int i=0;i<numOfThreads;i++){
            studentList.add("RON"+i);
        }
        studentList.sort(Comparator.naturalOrder());
        String students="Students Registered: "+studentList.toString().replaceAll(" ","");
        CommandProcessor commandProcessorOfAdmin=new CommandProcessor();
        commandProcessorOfAdmin.initialize();
        commandProcessorOfAdmin.sendCommand("LOGIN A A");
        String output     = commandProcessorOfAdmin.sendCommand("COURSESTAT "+p.getCourseNum());
        String [] tokens  = output.split("\n");
        String ack        = tokens[0];
        String courseName = tokens[1];
        String seats      = tokens[2];
        String studentsL  = tokens[3];

        if (!ack.equals("ACK 7")||!courseName.equals("Course: ("+p.getCourseNum()+") "+p.getCourseName())
                ||!seats.equals("Seats Available: "+"0/"+numOfThreads)||!studentsL.equals(students))
            return "CourseStat Test - Failed";
        CommandProcessor commandProcessorOfStudent=new CommandProcessor();
        commandProcessorOfStudent.initialize();
        commandProcessorOfStudent.sendCommand("LOGIN RON0 RON0");
        commandProcessorOfStudent.sendCommand("UNREGISTER "+p.getCourseNum());
        String output1= commandProcessorOfAdmin.sendCommand("COURSESTAT "+p.getCourseNum());
        String [] tokens1=output1.split("\n");
        String ack1=tokens1[0];
        String courseName1=tokens1[1];
        String seats1=tokens1[2];
        String studentsL1=tokens1[3];
        students=students.substring(0,22)+students.substring(27);
        commandProcessorOfAdmin.sendCommand("LOGOUT");
        if (!ack1.equals("ACK 7")||!courseName1.equals("Course: ("+p.getCourseNum()+") "+p.getCourseName())
                ||!seats1.equals("Seats Available: "+"1/"+numOfThreads)||!studentsL1.equals(students))
            return "CourseStat Test - Failed";
        return "CourseStat Test - Passed";

    }
    public ConcurrentLinkedQueue<String> registerStudentsToCourse(Course p,int numThreads) {
        ConcurrentLinkedQueue<String> outputs  = new ConcurrentLinkedQueue<>();
        try {
            ArrayList<CommandProcessor> commandsProcessors = initiateProcesses(numThreads);
            CountDownLatch threadsEnded = new CountDownLatch(numThreads);
            for (int i = 0; i < numThreads; i++) {
                int    tempI = i;
                new Thread(() -> {
                    try {
                        String username = "RON" + tempI;
                        outputs.add(commandsProcessors.get(tempI).sendCommand("LOGIN " + username + " " + username));
                        outputs.add(commandsProcessors.get(tempI).sendCommand("COURSEREG "+ p.getCourseNum()));
                        outputs.add(commandsProcessors.get(tempI).sendCommand("LOGOUT"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        commandsProcessors.get(tempI).kill();
                        threadsEnded.countDown();
                    }
                }).start();
            }
            threadsEnded.await();
        }catch(Exception e){
            e.printStackTrace();
        }
        return outputs;
    }
    private Course getCourseWithKdam(){
        Course p = null;
        for (Course s: courses){
            if(s.getNumCourseKdam()>0)
                p = s;
        }
        return p;
    }

    private Course getCourseWithNoKdams(){
        Course p = null;
        for (Course s: courses){
            if(s.getNumCourseKdam()==0)
                p = s;
        }
        return p;
    }
    private ArrayList<Course> registerToCourses(CommandProcessor commandProcessor){
        ArrayList<Course> courses1 = new ArrayList<>();
        for (Course s : courses)
            if (s.getNumCourseKdam()< 1)
                courses1.add(s);
        courses1.sort(new CourseComparator());
        for (Course course:courses1){
            commandProcessor.sendCommand("COURSEREG "+course.getCourseNum());
        }
        return courses1;
    }
    private void unregisterFromCourses(CommandProcessor commandProcessor,ArrayList<Course> courses1){
        for (Course course:courses1){
            commandProcessor.sendCommand("UNREGISTER "+course.getCourseNum());
        }
    }
    private String listToString(ArrayList<Course> courses1){
        String courseList="[";
        for (int i=0;i< courses1.size();i++)
            if (i==courses1.size()-1)
                courseList=courseList+courses1.get(i).getCourseNum();
            else courseList=courseList+courses1.get(i).getCourseNum()+',';
        courseList=courseList+']';
        return courseList;
    }
    private static class CourseComparator implements Comparator<Tests.Course> {
        @Override
        public int compare(Tests.Course course, Tests.Course course1) {
            return course.getNumInArray()-course1.getNumInArray();
        }
    }
    private static class Course{
        private final int    courseNum;
        private final String courseName;
        private final int    numOfMaxStudents;
        private final int    numInArray;
        private final ArrayList<Course> kdamCourses = new ArrayList<>();
        private final CourseComparator  courseComparator= new CourseComparator();

        public Course(int courseNum, String courseName, int numOfMaxStudents,int numInArray){
            this.courseNum  = courseNum;
            this.courseName = courseName;
            this.numOfMaxStudents = numOfMaxStudents;
            this.numInArray=numInArray;
        }
        public String getCourseName(){return courseName;}
        public int getNumInArray(){return numInArray;}

        public void addKdamCourse(Course c){
            if(!kdamCourses.contains(c) && c.getCourseNum() != getCourseNum())
                kdamCourses.add(c);
        }
        public void sortKdamCourses(){kdamCourses.sort(courseComparator);}
        public int getNumCourseKdam(){
            return kdamCourses.size();
        }
        public int getCourseNum(){
            return courseNum;
        }
        public boolean containsKdam(Course c){
            return kdamCourses.contains(c);
        }
        public int getNumOfAvailbleSpots(){
            return numOfMaxStudents;
        }
        public ArrayList<Course> getKdamCourses(){
            return kdamCourses;
        }
        public String getKdamCoursesAsString(){
            String kdamList="[";
            for (int i=0;i< kdamCourses.size();i++)
                if (i==kdamCourses.size()-1)
                    kdamList=kdamList+kdamCourses.get(i).getCourseNum();
                else kdamList=kdamList+kdamCourses.get(i).getCourseNum()+',';
            kdamList=kdamList+']';
            return kdamList;
        }
        @Override
        public String toString(){
            StringBuilder kdamNumbers = new StringBuilder();
            for(int i = 0; i < kdamCourses.size(); i++) {
                if(i != kdamCourses.size()-1)
                    kdamNumbers.append(kdamCourses.get(i).getCourseNum()).append(",");
                else
                    kdamNumbers.append(kdamCourses.get(i).getCourseNum());
            }
            return courseNum+"|"+courseName+"|["+kdamNumbers+"]"+"|"+numOfMaxStudents;
        }
    }

    public boolean atLeastOneCourseHasKdam(){
        int numKdam = 0;
        for(Course s : courses)
            numKdam += s.getNumCourseKdam();
        return numKdam > 0;
    }
    public void generateCourses(){
        try {
            ArrayList<Course> nonShuffledCourseList;

            final int numCoursesToGenerate = 7;
            for (int i = 0; i < numCoursesToGenerate; i++) {
                int generatedCourseNum = ((int) (Math.random() * 2000) + 1337);
                String courseName = UUID.randomUUID().toString().substring(0, 8);
                int numOfMaxStudents = ((int) (Math.random() * (8) + 5));
                courses.add(new Course(generatedCourseNum, courseName, numOfMaxStudents,i));
            }
            nonShuffledCourseList = new ArrayList<Course>(courses);

            for (Course value : nonShuffledCourseList) {
                do {
                    int addKdamCoinToss = ((int) (Math.random() * 5) + 1);
                    if (addKdamCoinToss > 2) {
                        Collections.shuffle(courses);
                        Course tempCourse = courses.get(0);
                        if (!tempCourse.containsKdam(value) && tempCourse.getNumCourseKdam() == 0)
                            value.addKdamCourse(tempCourse);
                    }
                } while (!atLeastOneCourseHasKdam());
                value.sortKdamCourses();
            }
            courses = nonShuffledCourseList;
            ArrayList<String> coursesLines = new ArrayList<>();
            for (Course course : courses) {
                System.out.println(course.toString());
                coursesLines.add(course.toString());
            }
            Files.write(Paths.get("./Courses.txt"), coursesLines, Charset.defaultCharset());
            Thread.sleep(1000);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    public void run() {
        try {
            System.out.println("Generating Course File....\r\n-----------------------");
            generateCourses();
            System.out.println("-----------------------");

            System.out.println("Be patient...");
            new Thread(() -> {
                TPCMain.main(new String[]{"7777"}); //You can change it to reactor also
            }).start();

            ArrayList<String> testAnswers = new ArrayList<>();
            testAnswers.add(testRegistrationSameUser());
            testAnswers.add(testLoginMultipleSameUser());
            testAnswers.add(testRegisLoginLogoutMultipleDifferentUser());
            testAnswers.add(tryBulkStudentLogicalTests());
            testAnswers.add(tryBulkAdminLogicalTests());
            testAnswers.add(testRegisterCourseWithoutAnyKdam());
            testAnswers.add(testRegisterCourseWithKdam());
            testAnswers.add(testKdamCheck());
            testAnswers.add(testMyCourses());
            testAnswers.add(testStudentStat());
            testAnswers.add(testCourseStat());

            System.out.println("\r\n\r\n----------------------------------");
            for (String testAnswer : testAnswers) System.out.println(testAnswer);
            System.out.println("----------------------------------");
            System.exit(0);
        } catch (Exception testException) {
            testException.printStackTrace();
        }
    }
}
