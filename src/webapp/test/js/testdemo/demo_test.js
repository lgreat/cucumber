PersonTest = TestCase("PersonTest");

PersonTest.prototype.testSpeaks = function() {
  var person = new GS.Person();
  assertEquals("Should returned Hellow World!", "Hello World!", person.speak("Hello World!"));
  //assertNull("Should have been null", person.speak("Hello World!")); this test will fail
};

