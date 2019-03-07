package com.rulex.dba;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class BmwDbaApplicationTests {

    public class Student {
        String name;
        int age;
        Integer tall;

        public Student(String name, int age, Integer tall) {
            this.name = name;
            this.age = age;
            this.tall = tall;
        }

        @Override
        public String toString() {
            return "Student{" +
                    "name='" + name + '\'' +
                    ", age=" + age +
                    ", tall=" + tall +
                    '}';
        }
    }


    @Test
    public void contextLoads() {
        BmwDbaApplicationTests dba = new BmwDbaApplicationTests();
        String a = new String("a");
        dba.change(a);
        Integer b = new Integer(2);
        dba.change(b);
        Student s = new Student("a", 1, 1);
        dba.change(s);
        StringBuffer d = new StringBuffer("asdf");
        dba.change(d);
        System.out.println(a);
        System.out.println(b);
        System.out.println(s.toString());
        System.out.println(d);
    }


    public void change(String s) {
        s = "abc";
    }

    public void change(StringBuffer s) {
        s.append("g");
        s = new StringBuffer("fdsa");
    }

    public void change(int s) {
        s = 100;
    }

    public void change(Student s) {
        s.age = 100;
        s.name = "b";
        s.tall = 100;
    }

}
