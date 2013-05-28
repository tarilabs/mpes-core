package net.tarilabs.mpes;

import org.apache.camel.builder.RouteBuilder;

import com.rapplogic.xbee.api.zigbee.ZNetRxIoSampleResponse;

public class CamelRoutesMpes extends RouteBuilder {

	@Override
	public void configure() throws Exception {
		from("xbeeapi://?baud=9600&tty=/dev/tty.usbserial-A4004CwJ")
			.to("seda:xbeeAsyncBuffer")
			;
	
		from("seda:xbeeAsyncBuffer")
			.choice()
	    		.when(body().isInstanceOf(ZNetRxIoSampleResponse.class))
	    			.log("route log ${body.getAnalog1()}")
	    			.to("ejb:java:global/mpes-core/MpesExpertSystem?method=insert")
	    		.otherwise()
	    			.log("I don't know what to do with this packet from the XBee mesh: ${body}")
	    	;
		
		from("direct:MpesSentence")
			.log("direct:MpesSentence> ${body}")
			.to("ejb:java:global/mpes-core/FacebookConnMgr?method=post")
			;
	}

}
