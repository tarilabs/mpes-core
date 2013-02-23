package net.tarilabs.mpes.test;

import java.lang.reflect.Method;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

public class SimpleDroolsTestWatcher extends TestWatcher {
	private boolean annotationsProcessedOK;
	private String theMethod;
	private String[] simpleRuleFilters;
	private String[] drlFiles;
	
	@Override
	protected void starting(Description description) {
		super.starting(description);
		theMethod = description.getMethodName();
		annotationsProcessedOK = false;
		try {
			WithSimpleDrlFiles atClassLevel = description.getTestClass().getAnnotation(WithSimpleDrlFiles.class);
			drlFiles = atClassLevel.value();
			
			Method method = description.getTestClass().getMethod(description.getMethodName());
			WithSimpleRuleFilter waf = method.getAnnotation(WithSimpleRuleFilter.class);
			if (waf != null) {
				simpleRuleFilters = waf.value();
			} else {
				simpleRuleFilters = null;
			}
			WithSimpleDrlFiles atMethodLevel = method.getAnnotation(WithSimpleDrlFiles.class);
			if (atMethodLevel != null) {
				drlFiles = atMethodLevel.value();
			}
			
			annotationsProcessedOK = true;
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
	}

	public String[] getSimpleRuleFilters() {
		return simpleRuleFilters;
	}

	public String[] getDrlFiles() {
		return drlFiles;
	}

	public boolean isAnnotationsProcessedOK() {
		return annotationsProcessedOK;
	}

	public String getTheMethod() {
		return theMethod;
	}
}
