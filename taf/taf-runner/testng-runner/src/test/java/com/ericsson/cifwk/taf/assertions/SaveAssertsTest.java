package com.ericsson.cifwk.taf.assertions;

import org.junit.After;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;


public class SaveAssertsTest {

    List<String> list1 = new ArrayList<>();
    List<String> list2 = new ArrayList<>();
    List<String> list3 = new ArrayList<>();
    List<String> list4 = new ArrayList<>();
    
    public SaveAssertsTest(){
        
        list1.add("entry1");
        list1.add("entry2");
        list1.add("entry3");
        list1.add("entry4");
        
        list2.add("entry1");
        list2.add("entry2");
        list2.add("entry3");
        list2.add("entry4");
        
        list3.add("entry1");
        list3.add("entry3");
        list3.add("entry4");
        list3.add("entry2");
        
        list1.add("entry1");
        list2.add("entry2");
        list3.add("entry3");
        
    }

    @After
    public void tearDown() {
        try {
            // Need to call it explicitly to reset the error container to make sure other tests are not affected
            SaveAsserts.assertAll();
        } catch (AssertionError e) {
            // IGNORE
        }
    }

    @Test
    public void SaveAssertEquals(){
        SaveAsserts.saveAssertEquals(list1, list2);
        SaveAsserts.saveAssertEquals(list1, list4);
    }

    @Test
    public void SaveAssertEqualsOrderNotImportant(){
        SaveAsserts.saveAssertEqualsOrderNotImportant(list1, list3);
    }

    @Test
    public void SaveAssertEqualsObject(){
        SaveAsserts.saveAssertNotNull(list3);
        SaveAsserts.saveAssertNotNull(null);

        SaveAsserts.saveAssertIsNull(list3);
        SaveAsserts.saveAssertIsNull(null);
    }
}
