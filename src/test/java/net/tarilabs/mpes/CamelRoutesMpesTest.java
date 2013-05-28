package net.tarilabs.mpes;

import org.apache.camel.builder.RouteBuilder;

public class CamelRoutesMpesTest extends RouteBuilder {

	@Override
	public void configure() {
		
		from("direct:MpesSentence")
			.log("${body}")
			;
	}

}
