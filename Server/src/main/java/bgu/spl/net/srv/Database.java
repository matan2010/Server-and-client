//myClass

package bgu.spl.net.srv;


import bgu.spl.net.impl.rci.Command;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Passive object representing the Database where all courses and users are stored.
 * <p>
 * This class must be implemented safely as a thread-safe singleton.
 * You must not alter any of the given public methods of this class.
 * <p>
 * You can add private fields and methods to this class as you see fit.
 */
public class Database {

    private ConcurrentHashMap<String, User> usernameHashMap;
    public ConcurrentHashMap<Integer, Course> courseHashMap;

    private HashMap<Integer, Object> opcodeHashMap;

    //singleton constructor-getInstance
    private static class SingletonDatabase {
        private static Database instance = null;

        static {
            try {
                instance = new Database();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Database() throws IOException {
        //initiate fields.
        this.usernameHashMap = new ConcurrentHashMap<>();
        this.courseHashMap = new ConcurrentHashMap<>();
        initialize("//home//spl211//Downloads//myzipped-1-10-2021-4-55-05-pm//sundayEvening//spl-net//Courses.txt");
    }

    /**
     * Retrieves the single instance of this class.
     */
    public static Database getInstance() {
        return SingletonDatabase.instance;
    }

    /**
     * loades the courses from the file path specified
     * into the Database, returns true if successful.
     */
    private boolean initialize(String coursesFilePath) throws IOException {
        //getting data from courses.txt
        File file = new File(coursesFilePath);
        BufferedReader br = new BufferedReader(new FileReader(file));
        String st;
        Course courseToAdd = null;
        while ((st = br.readLine()) != null) {
            courseToAdd = txtLineToCourseConverter(st);
            courseHashMap.put(courseToAdd.getCourseNum(), courseToAdd);
        }
        return false;
    }

    public boolean registerAdmin(String username, String password) {
        if (password == null) {

            return false;
        }
        //how do we check that he's really an admin?
        Admin admin = new Admin(username, password);
        User user = usernameHashMap.putIfAbsent(username, admin);
        if (user != null) {

            return false;
        }
        admin.setUsername(username);
        admin.setPassword(password);

        return true;

    }

    public boolean registerStudent(String username, String password) {
        if (password == null) {

            return false;
        }
        //how do we check that he's really an admin?
        Student student = new Student(username, password);
        User user = usernameHashMap.putIfAbsent(username, student);
        if (user != null) {

            return false;
        }
        student.setUsername(username);
        student.setPassword(password);

        return true;
    }

    //login block
    public boolean login(String username, String password) {
        //if there is no username like that in the usernameHM
        if (!usernameHashMap.containsKey(username)) {
            return false;
        }
        //password is legal and matches
        if (password == null || !(usernameHashMap.get(username).getPassword().equals(password))) {
            return false;
        }
        //check if the user is already logged in
        if (!usernameHashMap.get(username).logUserIn()) {
            return false;
        }

        //username exsits, password legal and matches, and user is currently not logged in
        return true;
    }

    //logout block

    public boolean logout(String username) {
        //if there is no username like that in the usernameHM
        if (!usernameHashMap.containsKey(username)) {
            return false;
        }
        //check if the user is already logged out
        if (!usernameHashMap.get(username).logUserOut()) {
            return false;
        }
        //username exsits, and user is currently logged in
        return true;
    }

    public boolean registerToCourse(Integer courseNum, String userName) {
        //if course not exsits
        if (!courseHashMap.containsKey(courseNum)) {
            System.out.println("course does not exist");
            return false;
        }
        Course courseToBeRegisteredTo = courseHashMap.get(courseNum);
        //if username doesn't exist or belong to an admin
        if (!usernameHashMap.containsKey(userName) || usernameHashMap.get(userName) instanceof Admin) {
            System.out.println("username does not exist in database , or username is an admin");
            return false;
        }
        Student student = (Student) usernameHashMap.get(userName);
        //no seats available in the course
        if (courseToBeRegisteredTo.getCurrentSize() >= courseToBeRegisteredTo.getNumOfMaxStudents()) {
            System.out.println("there is a student over flow for this course");
            return false;
        }
        //student does not have all kdam courses required

        if (!(student.getRegisteredCourseList().containsAll(courseToBeRegisteredTo.getKdamCourses()))) {
            return false;
        }

        courseToBeRegisteredTo.increaseByOneCurrentSize();
        courseToBeRegisteredTo.addStudentToCourseRegisteredStudentsList(student);

        student.addCourseToRegisteredCoursesList(courseHashMap.get(courseToBeRegisteredTo.getCourseNum()));


        return true;
    }

    //check it again
    public String checkKdamCourse(int courseNum) {        //return value List<Course>???
        if (!courseHashMap.containsKey(courseNum))
            return null;
        String kdamCourseListString = "[";
        Course course = courseHashMap.get(courseNum);
        for (Integer currentKdamCourse : course.getKdamCourses()) {
            kdamCourseListString = kdamCourseListString + currentKdamCourse.toString() + ", ";
        }
        if (course.getKdamCourses().size() > 0) {
            kdamCourseListString = kdamCourseListString.substring(0, kdamCourseListString.length() - 2);
        }
        kdamCourseListString = kdamCourseListString + "]";

        return kdamCourseListString;
    }

    //admin message
    public String printCourseStat(int courseNum) {
        // if course does not exists in DB's course list
        if (!courseHashMap.containsKey(courseNum)) {
            return null;
        }
        String output;

        //if course does exists in DB's course list
        Course courseToPrint = courseHashMap.get(courseNum);
        System.out.println("Course: (" + courseNum + ") " + courseToPrint.getCourseName());
        System.out.println("Seats Available: " + (courseToPrint.getNumOfMaxStudents() - courseToPrint.getCurrentSize())
                + "/" + courseToPrint.getNumOfMaxStudents());
        String studentRegisteredString = "Student Registered: [";
        Course course = courseHashMap.get(courseNum);
        for (Student registeredStudent : course.getRegisteredStudents()) {
            studentRegisteredString = studentRegisteredString + registeredStudent.getUsername() + ", ";
        }
        if (course.getRegisteredStudents().size() > 0) {
            studentRegisteredString = studentRegisteredString.substring(0, studentRegisteredString.length() - 2);
        }
        studentRegisteredString = studentRegisteredString + "]";

        output = "\n" + "Course: (" + courseNum + ") " + courseToPrint.getCourseName() + "\n" +
                "Seats Available: " + (courseToPrint.getNumOfMaxStudents() - courseToPrint.getCurrentSize())
                + "/" + courseToPrint.getNumOfMaxStudents() + "\n" + studentRegisteredString;
        return output;
    }

    //admin message
    public String printStudentStat(String username) {
        //if student doesn't exists in username HM , or its an admin type of user
        String output;
        if (!usernameHashMap.containsKey(username) || (usernameHashMap.get(username) instanceof Admin)) {
            return null;
        }
        Student student = (Student) usernameHashMap.get(username);
        System.out.println("Student: " + student.getUsername());
        String coursesString = "[";
        for (Integer course : student.getRegisteredCourseList()) {
            coursesString = coursesString + course + ", ";
        }
        if (student.getRegisteredCourseList().size() > 0) {
            coursesString = coursesString.substring(0, coursesString.length() - 2);
        }
        coursesString = coursesString + "]";

        output= "\n" + "Student: " + student.getUsername() + "\n"
                + coursesString;
        return output;
    }

    public String isStudentRegisteredToCourse(String username, Integer courseNum) {
        //if username doesn't exists OR username type is Admin
        if ((!usernameHashMap.containsKey(username)) || (usernameHashMap.get(username) instanceof Admin)) {
            return null;
        }
        //if course is not exists
        if (!courseHashMap.containsKey(courseNum)) {
            return null;
        }
        Student student = (Student) usernameHashMap.get(username);
        Course wantedCourse = courseHashMap.get(courseNum);
        if (student.getRegisteredCourseList().contains(wantedCourse.getCourseNum())) {
            return "REGISTERED";
        } else {
            return "NOT REGISTERED";
        }
    }

    public boolean unregisterFromCourse(String username, Integer courseNum) {
        //check if username doesn't exists OR username type is Admin
        if (!(usernameHashMap.containsKey(username)) || (usernameHashMap.get(username) instanceof Admin)) {
            return false;
        }
        //check if course is not exists
        if (!courseHashMap.containsKey(courseNum)) {
            return false;
        }
        Student student = (Student) usernameHashMap.get(username);
        Course course = courseHashMap.get(courseNum);
        //check if student is currently registered to course
        if (!student.isRegisteredToCourse(course)) {
            return false;
        }

        //unregister student from course
        //delete course from student's registered courses list
        student.deleteCourseFromRegisteredCoursesList(course);
        //delete student from course's registered students list and decrease participents number by 1
        course.deleteStudentFromRegisteredStudentsList(student);
        course.decreaseByOneCurrentSize();
        return true;
    }

    //TODO: check it again
    public String printMyCurrentCourses(String username) {
        String coursesString = null;
        if (usernameHashMap.containsKey(username) && usernameHashMap.get(username) instanceof Student) {
            Student student = (Student) usernameHashMap.get(username);
            coursesString = "[";
            for (Integer course : student.getRegisteredCourseList()) {
                coursesString = coursesString + course + ", ";
            }
            if (student.getRegisteredCourseList().size() > 0) {
                coursesString = coursesString.substring(0, coursesString.length() - 2);
            }
            coursesString = coursesString + "]";
        }
        return coursesString;
    }


    public ConcurrentHashMap<Integer, Course> getCourseHashMap() {
        return courseHashMap;
    }

    public Course getCourseByGivingCourseNum(Integer courseNum) {
        if (courseHashMap.containsKey(courseNum)) {
            return courseHashMap.get(courseNum);
        }
        return null;
    }

    public boolean isRegisteredToCourse(String username, Integer courseNum) {
        //if course not exsits
        if (!courseHashMap.containsKey(courseNum)) {
            return false;
        }
        Course courseToBeRegisteredTo = courseHashMap.get(courseNum);
        //if username doesn't exist or belong to an admin
        if (!usernameHashMap.containsKey(username) || usernameHashMap.get(username) instanceof Admin) {
            return false;
        }
        Student student = (Student) usernameHashMap.get(username);
        return student.isRegisteredToCourse(courseHashMap.get(courseNum));
    }

    private Course txtLineToCourseConverter(String txtLine) {      //change return value back to Course.
        int length = txtLine.length();
        int currIndex = 0;

        //reading and converting courseNum
        String courseNumString = "";
        Integer courseNum = 0;
        while ((txtLine.charAt(currIndex)) != '|' && currIndex != length) {
            courseNumString += txtLine.charAt(currIndex);
            currIndex++;
        }
        courseNum = Integer.parseInt(courseNumString);
        currIndex++;

        //reading and converting courseName
        String courseName = "";
        while ((txtLine.charAt(currIndex)) != '|' && currIndex != length) {
            courseName += txtLine.charAt(currIndex);
            currIndex++;
        }
        currIndex++;

        //reading, converting and initiating kdamCoursesList
        List<Integer> kdamCourseList = new ArrayList<>();
        String courseIntegerList = "";
        while ((txtLine.charAt(currIndex)) != '|' && currIndex != length) {
            if ((txtLine.charAt(currIndex)) != '[' && (txtLine.charAt(currIndex)) != ']') {
                courseIntegerList += txtLine.charAt(currIndex);
            }
            currIndex++;
        }
        currIndex++;
        //converting string to list
        if (courseIntegerList != "") {
            List<String> myList = new ArrayList<String>(Arrays.asList(courseIntegerList.split(",")));
            for (String numString : myList) {
                Integer currentCourseNum = Integer.parseInt(numString);
                kdamCourseList.add(currentCourseNum);
            }
        }


        //reading and converting numOfMaxStudents
        Integer numOfMaxStudents = 0;
        String numOfMaxStudentsString = "";
        while (currIndex != length && (txtLine.charAt(currIndex)) != '|') {
            numOfMaxStudentsString += txtLine.charAt(currIndex);
            currIndex++;
        }
        numOfMaxStudents = Integer.parseInt(numOfMaxStudentsString);

        //after collecting all course parameters - initialize course return value;
        return new Course(courseNum, courseName, numOfMaxStudents, kdamCourseList);
    }

    public User getUser(String username) {
        if (!usernameHashMap.containsKey(username)) {
            return null;
        }
        return usernameHashMap.get(username);
    }

    public static void main(String[] args) {
        System.out.println("hello world");
    }
}
