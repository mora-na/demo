package com.example.demo.common.tool;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class XStreamToolTest {

    @Test
    void toXmlAndXmlToBean_roundTrip() {
        Person person = new Person("alice", 30);
        String xml = XStreamTool.toXML(person);
        Person parsed = XStreamTool.xmlToBean(xml, Person.class);

        assertEquals("alice", parsed.name);
        assertEquals(30, parsed.age);
    }

    @Test
    void jsonToXml_buildsUppercaseTags() {
        String xml = XStreamTool.jsonToXML("{\"name\":\"bob\",\"age\":20}");
        assertTrue(xml.contains("<NAME>bob</NAME>"));
        assertTrue(xml.contains("<AGE>20</AGE>"));
    }

    @Test
    void jsonToXml_rejectsBlankInput() {
        assertThrows(IllegalArgumentException.class, () -> XStreamTool.jsonToXML(""));
    }

    private static class Person {
        private String name;
        private int age;

        public Person() {
        }

        public Person(String name, int age) {
            this.name = name;
            this.age = age;
        }
    }
}
