package com.shotball.project;

import com.shotball.project.utils.TextUtil;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void emailChecker() {
        assertTrue(TextUtil.validateEmail("example@gmail.com"));
        assertTrue(TextUtil.validateEmail("example@yandex.ru"));
        assertFalse(TextUtil.validateEmail("example@yandex"));
    }
}