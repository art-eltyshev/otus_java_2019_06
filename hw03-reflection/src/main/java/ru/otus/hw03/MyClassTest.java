package ru.otus.hw03;

import ru.otus.hw03.annotations.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MyClassTest {
    private MyClass myClass;

    @BeforeAll
    public static void beforeAll() {

    }

    @AfterAll
    public static void afterAll() {

    }

    @Before
    public void before() {
        this.myClass = new MyClass();
    }

    @After
    public void after() {
        this.myClass = null;
    }

    @Test
    public void sumTest() {
        assertEquals(3, this.myClass.sum(1, 2));
    }

    @Test
    public void negativeSumTest() {
        assertEquals(-1, this.myClass.sum(1, -2));
    }

    @Test
    public void wrongSumTest() {
        assertEquals(0, this.myClass.sum(1, 3));
    }
}
