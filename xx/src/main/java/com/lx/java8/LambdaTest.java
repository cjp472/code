package com.lx.java8;//说明:

import com.lx.util.LX;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toSet;

/**
 * 创建人:游林夕/2019/3/26 14 04
 */
public class LambdaTest {
    public static void main(String [] args){

    }
    @Test
    public void map(){
        Map<String,String> map = new HashMap();
        map.put("a","1");
        map.put("b","2");
        map.put("c","3");
        map.forEach((k,v)->System.out.println(k+":"+v));
    }
    @Test
    public void list(){
        List<String> list = Arrays.asList(new String[]{"1","21","3"});
        list.forEach(System.out::println);

        List<Person> javaProgrammers = new ArrayList<Person>() {
            {
                add(new Person("Elsdon", "Jaycob", "Java programmer", "male", 43, 2000));
                add(new Person("Tamsen", "Brittany", "Java programmer", "female", 23, 1500));
                add(new Person("Floyd", "Donny", "Java programmer", "male", 33, 1800));
                add(new Person("Sindy", "Jonie", "Java programmer", "female", 32, 1600));
                add(new Person("Vere", "Hervey", "Java programmer", "male", 22, 1200));
                add(new Person("Maude", "Jaimie", "Java programmer", "female", 27, 1900));
                add(new Person("Shawn", "Randall", "Java programmer", "male", 30, 2300));
                add(new Person("Jayden", "Corrina", "Java programmer", "female", 35, 1700));
                add(new Person("Palmer", "Dene", "Java programmer", "male", 33, 2000));
                add(new Person("Addison", "Pam", "Java programmer", "female", 34, 1300));
            }
        };
        javaProgrammers.stream().filter((p)->p.getSalary()<1500).forEach((p)->p.setSalary(p.getSalary()*15/10));
        javaProgrammers.sort((p1,p2)->p1.getSalary()-p2.getSalary());
        javaProgrammers.sort(Person::compareAge);
        javaProgrammers.stream().limit(3).forEach(System.out::println);
        javaProgrammers.stream().max((p1,p2)->p1.getSalary()-p2.getSalary());//找最大的
        Set s = javaProgrammers.stream().map(Person::getSalary).collect(toSet());
        System.out.println(s);
        String str = javaProgrammers.stream().map(Person::getLastName).collect(joining(","));
        System.out.println(str);
        TreeSet<Integer> javaAge = javaProgrammers.stream().map(Person::getAge).collect(toCollection(TreeSet::new));
        int salary = javaProgrammers.stream().mapToInt(Person::getSalary).sum();
        System.out.println(salary);

        IntSummaryStatistics is = javaAge.stream().mapToInt(x->x).summaryStatistics();
        System.out.println("最大值:"+is.getMax()+",平均值:"+is.getAverage()+",合计:"+is.getSum());

    }
    @Test
    public void thread(){
        new Thread(()->System.out.println("1")).start();
    }
    @Test
    public void sort(){
        List<Integer> ls = new ArrayList<>();
        Random random = new Random();
        for (int i=0;i<10;i++){
            ls.add(random.nextInt(100));
        }
        ls.sort((i1,i2)->i2-i1);
        ls.forEach(System.out::println);

        String[] players = {"Rafael Nadal", "Novak Djokovic","Stanislas Wawrinka", "David Ferrer"};
        Arrays.sort(players, (String s1, String s2) -> (s1.compareTo(s2)));
        Arrays.sort(players, (String s1, String s2) -> (s1.charAt(s1.length() - 1) - s2.charAt(s2.length() - 1)));

        Integer[] arr = {1,4,56,21,3,1,1};
        Arrays.sort(arr,(Integer i1,Integer i2)->(i1-i2));

    }

    @Test
    public void array(){
        long[] arrayOfLong = new long [ 20000 ];

        Arrays.parallelSetAll( arrayOfLong,
                index -> ThreadLocalRandom.current().nextInt( 1000000 ) );
        Arrays.stream( arrayOfLong ).limit( 10 ).forEach(
                i -> System.out.print( i + " " ) );
        System.out.println();

        Arrays.parallelSort( arrayOfLong );
        Arrays.stream( arrayOfLong ).limit( 10 ).forEach(
                i -> System.out.print( i + " " ) );
        System.out.println();
    }

}
class Person {

    private String firstName, lastName, job, gender;
    private int salary, age;

    public Person(String firstName, String lastName, String job,
                  String gender, int age, int salary) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.age = age;
        this.job = job;
        this.salary = salary;
    }

    public int compareAge(Person p){
        return this.getAge()-p.getAge();
    }
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public int getSalary() {
        return salary;
    }

    public void setSalary(int salary) {
        this.salary = salary;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    @Override
    public String toString() {
        return "Person{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", job='" + job + '\'' +
                ", gender='" + gender + '\'' +
                ", salary=" + salary +
                ", age=" + age +
                '}';
    }
}