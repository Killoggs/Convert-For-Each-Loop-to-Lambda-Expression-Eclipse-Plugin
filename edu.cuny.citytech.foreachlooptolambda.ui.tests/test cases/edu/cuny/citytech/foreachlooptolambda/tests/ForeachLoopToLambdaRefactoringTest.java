package edu.cuny.citytech.foreachlooptolambda.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import java.util.logging.Logger;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.ui.tests.refactoring.Java18Setup;
import org.eclipse.ltk.core.refactoring.Refactoring;

import edu.cuny.citytech.foreachlooptolambda.ui.refactorings.ForeachLoopToLambdaRefactoring;
import edu.cuny.citytech.refactoring.common.tests.RefactoringTest;

@SuppressWarnings("restriction")
public class ForeachLoopToLambdaRefactoringTest extends RefactoringTest {

	private static final Class<ForeachLoopToLambdaRefactoringTest> clazz = ForeachLoopToLambdaRefactoringTest.class;

	private static final Logger logger = Logger.getLogger(clazz.getName());

	private static final String REFACTORING_PATH = "ForeachLoopToLambda/";

	public ForeachLoopToLambdaRefactoringTest(String name) {
		super(name);
	}

	/**
	 * @return
	 */
	public static Test suite() {
		return setUpTest(new TestSuite(clazz));
	}

	/**
	 * @param testSuite
	 * @return
	 */
	public static Test setUpTest(Test test) {
		return new Java18Setup(test);
	}

	/**
	 * @return the refactoringPath
	 */
	@Override
	public String getRefactoringPath() {
		return REFACTORING_PATH;
	}

	@Override
	protected Logger getLogger() {
		return logger;
	}

	@Override
	protected Refactoring getRefactoring(IMethod... methods) {
		return new ForeachLoopToLambdaRefactoring(methods);
	}
	
	public void testLoopWithContinue() throws Exception {
		helperFail(new String[] { "m" }, new String[][] { new String[0] });
	}
	
	public void testLoopWithBreak() throws Exception {
		helperFail(new String[] { "m" }, new String[][] { new String[0] });
	}
	
	public void testLoopWithBreakAndContinue() throws Exception {
		helperFail(new String[] { "m" }, new String[][] { new String[0] });
	}

	
	public void testLoopWithReturn() throws Exception {
		helperFail(new String[] { "m" }, new String[][] { new String[0] });
	}
	
	public void testLoopWithMultipleReturn() throws Exception {
		helperFail(new String[] { "m" }, new String[][] { new String[0] });
	}
	
//	public void testLoopIterateOverCollection() throws Exception {
//		helperFail(new String[] { "m" }, new String[][] { new String[0] });
//	}
//	
//		
//	public void testLoopIterateOverArrayList() throws Exception {
//		helperFail(new String[] { "m" }, new String[][] { new String[0] });
//	}
//	
//	public void testLoopIterateOverList() throws Exception {
//		helperFail(new String[] { "m" }, new String[][] { new String[0] });
//	}
//	
	public void testLoopIterateOverArray() throws Exception {
		helperFail(new String[] { "m" }, new String[][] { new String[0] });
	}
}
