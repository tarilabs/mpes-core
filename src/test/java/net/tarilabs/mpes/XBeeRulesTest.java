package net.tarilabs.mpes;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import net.tarilabs.mpes.model.CurStatusPrevStatus;
import net.tarilabs.mpes.model.MpesSentence;
import net.tarilabs.mpes.test.SimpleDroolsTestSupport;
import net.tarilabs.mpes.test.WithSimpleDrlFiles;
import net.tarilabs.mpes.test.WithSimpleRuleFilter;

import org.drools.runtime.rule.QueryResults;
import org.drools.time.SessionPseudoClock;
import org.junit.Before;
import org.junit.Test;

import com.rapplogic.xbee.api.XBeeAddress64;
import com.rapplogic.xbee.api.zigbee.ZNetRxIoSampleResponse;
import com.rapplogic.xbee.util.IIntArrayInputStream;

@WithSimpleDrlFiles("XBeeRules.drl")
public class XBeeRulesTest extends SimpleDroolsTestSupport {
	
	@Before
	public void init() throws Exception {
		System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.naming.java.javaURLContextFactory");
		System.setProperty(Context.URL_PKG_PREFIXES, "org.apache.naming");
		System.setProperty(CamelBootstrap.ROUTES, "net.tarilabs.mpes.CamelRoutesMpesTest");
		InitialContext ctx = new InitialContext();
		try {
			ctx.lookup("java:global");
		} catch (NamingException e) {
			ctx.createSubcontext("java:");
			ctx.createSubcontext("java:global");
			ctx.createSubcontext("java:global/mpes-core");

			CamelBootstrap cb = new CamelBootstrap();
			cb.init();
			ctx.bind("java:global/mpes-core/CamelBootstrap", cb);
		}
	}
	
	// Test rule Filter01 in isolation
	@Test(timeout=JUNIT_TIMEOUT)
	@WithSimpleRuleFilter("Filter01")
	public void testFilter01() throws IOException {
		ZNetRxIoSampleResponse r = makeAnalogResponse(30);
		
		//bad
		ZNetRxIoSampleResponse r2 = new ZNetRxIoSampleResponse();
		r2.setRemoteAddress64(new XBeeAddress64(0x00,0x13,0xa2,0x00,0x40,0x68,0xe0,0x99));
		r2.setAnalog1(30);

		getKsession().insert(r);
		getKsession().insert(r2);
		getKsession().fireAllRules();
		
		assertNotNull("I was expecting to still find r in the working memory.", getKsession().getFactHandle(r));
		assertNull("I was expecting that r2 would have been retracted.", getKsession().getFactHandle(r2));
	}
	
	// Test rule Filter01 along with other rules
	@Test(timeout=JUNIT_TIMEOUT)
	public void testFilter01Integration() throws IOException {
		testFilter01(); // the same expectations.
	}
	
	@Test(timeout=JUNIT_TIMEOUT)
	@WithSimpleRuleFilter("Filter02")
	public void testFilter02() throws IOException {

		ZNetRxIoSampleResponse r = makeAnalogResponse(30);
		
		//bad, as it does not contains any analog reading.
		ZNetRxIoSampleResponse r2 = new ZNetRxIoSampleResponse();
		r2.setRemoteAddress64(new XBeeAddress64(0x00,0x13,0xa2,0x00,0x40,0x68,0xe0,0x95));

		getKsession().insert(r);
		getKsession().insert(r2);
		getKsession().fireAllRules();
		
		assertNotNull("I was expecting to still find r in the working memory.", getKsession().getFactHandle(r));
		assertNull("I was expecting that r2 would have been retracted.", getKsession().getFactHandle(r2));
	}
	
	// Test rule Filter02 along with other rules
	@Test(timeout=JUNIT_TIMEOUT)
	public void testFilter02Integration() throws IOException {
		testFilter02(); // the same expectations.
	}
	
	@Test(timeout=JUNIT_TIMEOUT)
	@WithSimpleRuleFilter("Create01")
	public void testCreate01() throws IOException {
		getKsession().fireAllRules();
		
		QueryResults results = getKsession().getQueryResults("CurStatusPrevStatus Home Toothbrush");
		assertTrue("I was expecting only one CurPrev for the Home Toothbrush", results.size() == 1);
		assertTrue("Still no sensor reading in the working mem, so should still be curStatus null", queryCurStatusPrevStatus().getCurStatus() == null);
	}
	
	@Test(timeout=JUNIT_TIMEOUT)
	public void testCreate01Integration() throws IOException {
		testCreate01(); // same expectations
	}
	
	@Test(timeout=JUNIT_TIMEOUT)
	public void testExpiration() throws IOException {
		ZNetRxIoSampleResponse r = makeAnalogResponse(30);

		getKsession().insert(r);
		getKsession().fireAllRules();
		assertNotNull("I was expecting to still find r in the working memory.", getKsession().getFactHandle(r));
		
		SessionPseudoClock clock = getKsession().getSessionClock();
		clock.advanceTime(120, TimeUnit.MINUTES);
		getKsession().fireAllRules();
		
		assertNull("I was expect r retracted for expiration.", getKsession().getFactHandle(r));
	}
	
	/**
	 * There cannot be a test for Detect Undocked without integration
	 * @throws IOException
	 */
	@Test(timeout=JUNIT_TIMEOUT)
	public void testDetectUndockedIntegration() throws IOException {
		getKsession().fireAllRules();
		
		QueryResults results = getKsession().getQueryResults("CurStatusPrevStatus Home Toothbrush");
		assertTrue("I was expecting only one CurPrev for the Home Toothbrush", results.size() == 1);
		assertTrue("Still no sensor reading in the working mem, so should still be curStatus null", queryCurStatusPrevStatus().getCurStatus() == null);
		
		advanceTime(10, TimeUnit.SECONDS);
		getKsession().insert(makeAnalogResponse(1023));
		getKsession().fireAllRules();
		assertTrue("Not yet enough sensor reading to tell the status", queryCurStatusPrevStatus().getCurStatus() == null);
		
		advanceTime(1, TimeUnit.SECONDS);
		getKsession().insert(makeAnalogResponse(181));
		getKsession().fireAllRules();
		assertTrue("Not yet enough sensor reading to tell the status", queryCurStatusPrevStatus().getCurStatus() == null);
		
		advanceTime(1, TimeUnit.SECONDS);
		getKsession().insert(makeAnalogResponse(181));
		getKsession().fireAllRules();
		assertTrue("Not yet enough sensor reading to tell the status", queryCurStatusPrevStatus().getCurStatus() == null);
		
		advanceTime(1, TimeUnit.SECONDS);
		getKsession().insert(makeAnalogResponse(181));
		getKsession().fireAllRules();
		assertTrue("Should be detected as UNDOCKED now.", queryCurStatusPrevStatus().getCurStatus().equals("UNDOCKED"));
	}
	
	/**
	 * There cannot be a test for Detect Docked without integration
	 * @throws IOException
	 */
	@Test(timeout=JUNIT_TIMEOUT)
	public void testDetectDockedIntegration() throws IOException {
		getKsession().fireAllRules();
		
		QueryResults results = getKsession().getQueryResults("CurStatusPrevStatus Home Toothbrush");
		assertTrue("I was expecting only one CurPrev for the Home Toothbrush", results.size() == 1);
		assertTrue("Still no sensor reading in the working mem, so should still be curStatus null", queryCurStatusPrevStatus().getCurStatus() == null);
		
		advanceTime(10, TimeUnit.SECONDS);
		getKsession().insert(makeAnalogResponse(181));
		getKsession().fireAllRules();
		assertTrue("Not yet enough sensor reading to tell the status", queryCurStatusPrevStatus().getCurStatus() == null);
		
		advanceTime(1, TimeUnit.SECONDS);
		getKsession().insert(makeAnalogResponse(1023));
		getKsession().fireAllRules();
		assertTrue("Not yet enough sensor reading to tell the status", queryCurStatusPrevStatus().getCurStatus() == null);
		
		advanceTime(1, TimeUnit.SECONDS);
		getKsession().insert(makeAnalogResponse(1023));
		getKsession().fireAllRules();
		assertTrue("Not yet enough sensor reading to tell the status", queryCurStatusPrevStatus().getCurStatus() == null);
		
		advanceTime(1, TimeUnit.SECONDS);
		getKsession().insert(makeAnalogResponse(1023));
		getKsession().fireAllRules();
		assertTrue("Should be detected as DOCKED now.", queryCurStatusPrevStatus().getCurStatus().equals("DOCKED"));
	}
	
	/**
	 * Testing only by single rule and manual override insert object into working memory.
	 * @throws IOException
	 */
	@Test(timeout=JUNIT_TIMEOUT)
	@WithSimpleRuleFilter("Home Toothbrush Session")
	public void testHomeToothbrushSessionManualOverride() throws IOException {
		getKsession().fireAllRules();
		
		QueryResults results = getKsession().getQueryResults("CurStatusPrevStatus Home Toothbrush");
		assertTrue("I was expecting NO CurPrev for the Home Toothbrush because of manual override", results.size() == 0);
		
		CurStatusPrevStatus cp = new CurStatusPrevStatus("Home Toothbrush");
		cp.setCurStatus("DOCKED");
		cp.setCurStatusTs( (60+8+1)*1000L);
		cp.setPrevStatus("UNDOCKED");
		cp.setPrevStatusTs(0L);
		
		getKsession().insert(cp);
		getKsession().fireAllRules();
		
		results = getKsession().getQueryResults("MpesSentence for area a", "Home Toothbrush");
		assertTrue("I was expecting only one Home Toothbrush MpesSentence", results.size() == 1);
		
		MpesSentence ms = (MpesSentence) results.iterator().next().get("ms");
		assertTrue(ms.getArea().equals("Home Toothbrush"));
		assertTrue(ms.getMessage().equals("Total time: 1m1s Oscillations: 7726"));
	}
	
	/**
	 * Testing only by single rule and manual override insert object into working memory.
	 * @throws IOException
	 */
	@Test(timeout=JUNIT_TIMEOUT)
	public void testHomeToothbrushSessionIntegration() throws IOException {
		getKsession().fireAllRules();
		
		QueryResults results = getKsession().getQueryResults("CurStatusPrevStatus Home Toothbrush");
		assertTrue("I was expecting only one CurPrev for the Home Toothbrush", results.size() == 1);
		assertTrue("Still no sensor reading in the working mem, so should still be curStatus null", queryCurStatusPrevStatus().getCurStatus() == null);
		
		advanceTime(10, TimeUnit.SECONDS);
		getKsession().insert(makeAnalogResponse(1023));
		getKsession().fireAllRules();
		assertTrue("Not yet enough sensor reading to tell the status", queryCurStatusPrevStatus().getCurStatus() == null);
		
		int i = 0;
		while (i < 30) {
			advanceTime(1, TimeUnit.SECONDS);
			getKsession().insert(makeAnalogResponse(181));
			getKsession().fireAllRules();
			i++;
		}
		
		i = 0;
		while (i < 3) {
			advanceTime(1, TimeUnit.SECONDS);
			getKsession().insert(makeAnalogResponse(1023));
			getKsession().fireAllRules();
			i++;
		}
		
		results = getKsession().getQueryResults("MpesSentence for area a", "Home Toothbrush");
		assertTrue("I was expecting only one Home Toothbrush MpesSentence", results.size() == 1);
		
		MpesSentence ms = (MpesSentence) results.iterator().next().get("ms");
		assertTrue(ms.getArea().equals("Home Toothbrush"));
		assertTrue(ms.getMessage().equals("Total time: 22s Oscillations: 2786"));
	}
	
	@Test(timeout=JUNIT_TIMEOUT)
	public void testStupid() throws IOException, InterruptedException {
		// TODO make a long running session (longer than evt expiration) simulated to check the accumulate function works properly.
	}
	
	/**
	 * Expect the result of the query in the knowledge session have only 1 result.
	 * @return
	 */
	private CurStatusPrevStatus queryCurStatusPrevStatus() {
		QueryResults results = getKsession().getQueryResults("CurStatusPrevStatus Home Toothbrush");
		return (CurStatusPrevStatus) results.iterator().next().get("cp");
	}
	
	private void advanceTime(int i, TimeUnit tu) {
		SessionPseudoClock clock = getKsession().getSessionClock();
		clock.advanceTime(i, tu);
	}

	private ZNetRxIoSampleResponse makeAnalogResponse(int analog1) throws IOException {
		ZNetRxIoSampleResponse r = new ZNetRxIoSampleResponse();
		r.setRemoteAddress64(new XBeeAddress64(0x00,0x13,0xa2,0x00,0x40,0x68,0xe0,0x95));
		r.parse( new IIntArrayInputStream() {
			@Override
			public int read(String arg0) throws IOException {
				if (arg0.equals("ZNet RX IO Sample Size")) {
					return 1;
				}
				if (arg0.equals("ZNet RX IO Sample Analog Channel Mask")) {
					// this should map to analog[1]
					return 2;
				}
				return 0;
			}		
			@Override
			public int read() throws IOException {
				return 0;
			}
		});
		r.setAnalog1(analog1);
		return r;
	}
}
