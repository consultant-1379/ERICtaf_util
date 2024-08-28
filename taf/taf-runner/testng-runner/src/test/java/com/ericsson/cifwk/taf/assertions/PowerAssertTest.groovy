package com.ericsson.cifwk.taf.assertions
import org.junit.Test

class PowerAssertTest {

    Map map1 = ["key1": "value1", "key2": "value2", "key3": "value3"]
    Map map2 = ["key1": "value1", "key2": "value2", "key3": "value3"]
    Map map3 = ["key1": "value1", "key3": "value3", "key2": "value2"]
    List list1 = ["entry1", "entry2", "entry3", "entry4"]
    List list2 = ["entry1", "entry2", "entry3", "entry4"]
    List list3 = ["entry1", "entry3", "entry4", "entry2"]
    List list4 = ["entry1", "entry2", "entry3"]

    TafAsserts tcHelper = new TafAsserts() {}

    @Test
    public void testAssertEquals() {
        tcHelper.assertEquals(map1, map2)
        tcHelper.assertEquals(list1, list2)
        try {
            tcHelper.assertEquals(map1, map3)
            TafAsserts.assertTrue(false)
        } catch (AssertionError e) {
        }
    }

    @Test
    public void testAssertEqualsOrderNotImportant() {
        tcHelper.assertEqualsOrderNotImportant(list2, list3)
        tcHelper.assertEqualsOrderNotImportant(list2, list1)
        try {
            tcHelper.assertEqualsOrderNotImportant(list1, list4)
            TafAsserts.assertTrue(false)
        } catch (AssertionError e) {
        }
    }

    @Test
    public void testAssertObjectNotNull() {
        tcHelper.assertNotNull(map1)
        tcHelper.assertNotNull(list1)
        try {
            tcHelper.assertNotNull(null)
            TafAsserts.assertTrue(false)
        } catch (AssertionError e) {
        }
    }

    @Test
    public void testAssertObjectIsNull() {
        tcHelper.assertNull(null)
        try {
            tcHelper.assertNull(map1)
            TafAsserts.assertTrue(false)
        } catch (AssertionError e) {
        }
    }

}
