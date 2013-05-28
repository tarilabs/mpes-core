package net.tarilabs.mpes;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Startup
public class CamelBootstrap {
	
	private static final Logger logger = LoggerFactory.getLogger(CamelBootstrap.class);
	
	public static final String ROUTES = "net.tarilabs.mpes.CamelBootstrap.routes";

	protected CamelContext camelContext;
	protected ProducerTemplate producerTemplate;

	public CamelContext getCamelContext() {
		return camelContext;
	}

	public ProducerTemplate getProducerTemplate() {
		return producerTemplate;
	}

	@PostConstruct
	protected void init() throws Exception {
		logger.info("init() starting up..."); 
		camelContext = new DefaultCamelContext();
		
		String routesClazz = System.getProperty(ROUTES);
		if ( routesClazz == null ) {
			camelContext.addRoutes(new CamelRoutesMpes());
		} else {
			RoutesBuilder rb = (RoutesBuilder) Class.forName(routesClazz).getConstructor().newInstance();
			camelContext.addRoutes(rb);
		}
		
		camelContext.start();
		producerTemplate = camelContext.createProducerTemplate();
	}
	
}
