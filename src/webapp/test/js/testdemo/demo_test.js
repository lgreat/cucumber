PersonTest = TestCase("PersonTest");

PersonTest.prototype.setUp = function() {
  this.person = new GS.Person();
};

PersonTest.prototype.testSpeaks = function() {
  assertEquals("Should returned Hellow World!", "Hello World!", this.person.speak("Hello World!"));
  //assertNull("Should have been null", person.speak("Hello World!")); this test will fail
};

PersonTest.prototype.testUsingGoodBrowser = function() {
  assertTrue("Only a good browser is supported!", this.person.usingGoodBrowser());
};
