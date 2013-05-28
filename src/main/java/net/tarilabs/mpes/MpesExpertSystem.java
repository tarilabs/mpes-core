package net.tarilabs.mpes;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;


import org.drools.ClockType;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseConfiguration;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderError;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.conf.EventProcessingOption;
import org.drools.io.ResourceFactory;
import org.drools.runtime.KnowledgeSessionConfiguration;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.conf.ClockTypeOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Startup
public class MpesExpertSystem {
	
	private static final Logger logger = LoggerFactory.getLogger(MpesExpertSystem.class);
	
	private KnowledgeBase kbase;
	private StatefulKnowledgeSession ksession;

	@PostConstruct
	public void init() {
		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

		logger.info("Adding Rules...");
		kbuilder.add(ResourceFactory.newClassPathResource("XBeeRules.drl"), ResourceType.DRL);	

		if (kbuilder.hasErrors()) {
			StringBuilder sb = new StringBuilder();
			for (KnowledgeBuilderError err : kbuilder.getErrors()) {
				sb.append(err+"\n");
			}
			throw new IllegalArgumentException("KnowledgeBuilder error(s):\n"+sb.toString());
		}
		
		logger.info("KBConfiguration...");
		KnowledgeBaseConfiguration kbaseConf = KnowledgeBaseFactory.newKnowledgeBaseConfiguration();
		kbaseConf.setOption(EventProcessingOption.STREAM);
		kbase = KnowledgeBaseFactory.newKnowledgeBase(kbaseConf);
		kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
		
		logger.info("KSConfiguration...");
		KnowledgeSessionConfiguration ksConfig = KnowledgeBaseFactory.newKnowledgeSessionConfiguration();
		ksConfig.setOption( ClockTypeOption.get(ClockType.REALTIME_CLOCK.getId()) );
		ksession = kbase.newStatefulKnowledgeSession(ksConfig, null);
		
		ksession.fireAllRules();
	}
	
	public void insert(Object o) {
		logger.info("About to insert "+o);
		ksession.insert(o);
		ksession.fireAllRules();
	}
}
