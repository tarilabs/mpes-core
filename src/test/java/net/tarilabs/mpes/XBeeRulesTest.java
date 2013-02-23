package net.tarilabs.mpes;

import static org.junit.Assert.*;

import java.io.IOException;

import net.tarilabs.mpes.test.SimpleDroolsTestSupport;
import net.tarilabs.mpes.test.WithSimpleDrlFiles;
import net.tarilabs.mpes.test.WithSimpleRuleFilter;

import org.junit.Test;

import com.rapplogic.xbee.api.XBeeAddress64;
import com.rapplogic.xbee.api.zigbee.ZNetRxIoSampleResponse;
import com.rapplogic.xbee.util.IIntArrayInputStream;

@WithSimpleDrlFiles("XBeeRules.drl")
public class XBeeRulesTest extends SimpleDroolsTestSupport {
	
	// Test rule Filter01 in isolation
	@Test(timeout=JUNIT_TIMEOUT)
	@WithSimpleRuleFilter("Filter01")
	public void testFilter01() throws IOException {
		ZNetRxIoSampleResponse r = new ZNetRxIoSampleResponse();
		r.setRemoteAddress64(new XBeeAddress64(0x00,0x13,0xa2,0x00,0x40,0x68,0xe0,0x00));
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
		r.setAnalog1(30);
		
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

		ZNetRxIoSampleResponse r = new ZNetRxIoSampleResponse();
		r.setRemoteAddress64(new XBeeAddress64(0x00,0x13,0xa2,0x00,0x40,0x68,0xe0,0x00));
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
		r.setAnalog1(30);
		
		//bad, as it does not contains any analog reading.
		ZNetRxIoSampleResponse r2 = new ZNetRxIoSampleResponse();
		r2.setRemoteAddress64(new XBeeAddress64(0x00,0x13,0xa2,0x00,0x40,0x68,0xe0,0x00));

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
}
