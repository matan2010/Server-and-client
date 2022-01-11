//myClass

package bgu.spl.net.srv;

import java.util.ArrayList;
import java.util.List;

public class Student extends User {

    private List<Integer> registeredCourseList;

    public Student( String username, String password){
        super(username,password);
        this.registeredCourseList = new ArrayList<>();
    }

    public List<Integer> getRegisteredCourseList() {
        return registeredCourseList;
    }
    public void addCourseToRegisteredCoursesList(Course course){
        registeredCourseList.add(course.getCourseNum());
    }

    public boolean isRegisteredToCourse(Course course){
        return (this.registeredCourseList.contains(course.getCourseNum()));
    }
    public void deleteCourseFromRegisteredCoursesList(Course course){
        registeredCourseList.remove(course.getCourseNum());
    }
    public String getType(){
        return "Student";
    }
}
