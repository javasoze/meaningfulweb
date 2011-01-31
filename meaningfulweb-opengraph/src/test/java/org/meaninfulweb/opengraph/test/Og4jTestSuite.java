package org.meaningfulweb.opengraph.test;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

public class Og4jTestSuite {
	public static Test suite(){
        TestSuite suite=new TestSuite();
        suite.addTestSuite(Og4jTestCase.class);
        return suite;
	}
	
	public static void main(String[] args) {
		TestRunner.run(suite());
	}
}
