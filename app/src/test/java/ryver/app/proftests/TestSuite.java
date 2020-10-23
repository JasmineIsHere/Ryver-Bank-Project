package ryver.app.profTests;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

/** Auto-run all the test files sequentially - use Java Test Runner to run. */

@RunWith(JUnitPlatform.class)
@SelectClasses( { A_CustomerTest.class, B_ContentTest.class, C_AccountTest.class,
                D_TradeTest.class} )
public class TestSuite {
    
}
