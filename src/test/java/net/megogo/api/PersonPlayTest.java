package net.megogo.api;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PersonPlayTest {
    public static void main(String[] args) {
        List<Person> people = List.of(new Person("alex", 5), new Person("Osle", 15), new Person("Fil", 25), new Person("Irmle", 15));
        var result =  new PersonPlayTest().groupByAge(people);
        System.out.println(result);
    }

    //<14 ignore
    //14 - 18 - YOUNG
    //18 - 60 - ADULT
    //60+ - OLD
    Map<AgeGroup, List<Person>> groupByAge(List<Person> people) {
        Map<AgeGroup, List<Person>> newMap =

        people.stream().filter(p->p.age>=14).collect(Collectors.groupingBy(p->{
            if(p.getAge()<18) return AgeGroup.YOUNG;
            if(p.getAge()<60) return AgeGroup.ADULT;
            return AgeGroup.OLD;
        }, Collectors.toList()));

        return newMap;
    }
}

enum AgeGroup {
    YOUNG, ADULT, OLD
}

class Student extends Person{
    public Student(String name, int age) {
        super(name, age);
    }
}

class Person {
    final String name;
    final int age;

    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }
}