package com.guillaumedelente.gradle.play

import com.guillaumedelente.gradle.play.TaskHelper
import org.junit.Test

import static org.junit.Assert.*

class TaskHelperTest {

    private static final File TESTFILE = new File("src/test/fixtures/android_app/src/main/play/en-US/whatsnew");

    @Test
    public void testFilesAreCorrectlyTrimmed() {
        String trimmed = TaskHelper.readAndTrimFile(TESTFILE, 6);

        assertEquals(6, trimmed.length());
    }

    @Test
    public void testShortFilesAreNotTrimmed() {
        String trimmed = TaskHelper.readAndTrimFile(TESTFILE, 100);

        assertEquals(12, trimmed.length());
    }
}
