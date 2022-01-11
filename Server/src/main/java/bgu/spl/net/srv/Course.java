//myClass

package bgu.spl.net.srv;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Course {

    private final Integer courseNum;
    private String courseName;

    private List<Integer> kdamCourses;
    private List<Student> registeredStudents;
    private final Integer numOfMaxStudents;
    private Integer currentSize;

    //constructor for course with at least one kdamCourse
    public Course(Integer courseNum,String courseName, Integer numOfMaxStudents, List<Integer> kdamCourses) {
        this.courseNum = courseNum;
        this.courseName=courseName;
        this.numOfMaxStudents =numOfMaxStudents;
        this.kdamCourses = kdamCourses;
        this.registeredStudents= new ArrayList<>();
        this.currentSize=0;
    }

    //constructors - start

    //constructor for course without kdamCourses
    public Course(Integer courseNum,String courseName, Integer limit) {
        this.courseNum = courseNum;
        this.numOfMaxStudents =limit;
        this.kdamCourses = new ArrayList<>();
        this.registeredStudents= new ArrayList<>();
        this.currentSize=0;
    }

    //constructors end

    //getters
    public Integer getCourseNum() {
        return courseNum;
    }

    public String getCourseName() {
        return courseName;
    }

    public List<Integer> getKdamCourses() {
        return kdamCourses;
    }

    public Integer getNumOfMaxStudents() {
        return numOfMaxStudents;
    }

    public Integer getCurrentSize() {
        return currentSize;
    }

    public List<Student> getRegisteredStudents() {
        return registeredStudents;
    }

    //setters

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public void setCurrentSize(Integer currentSize) {
        this.currentSize = currentSize;
    }

    public void increaseByOneCurrentSize(){
        this.currentSize++;
    }
    public void decreaseByOneCurrentSize (){
        this.currentSize--;
    }



    public void addStudentToCourseRegisteredStudentsList(Student student){
        this.registeredStudents.add(student);
    }
    public void deleteStudentFromRegisteredStudentsList(Student student){
        registeredStudents.remove(student);
    }


}
