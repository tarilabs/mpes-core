package net.tarilabs.mpes.test;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderError;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.ResourceFactory;
import org.drools.logger.KnowledgeRuntimeLogger;
import org.drools.logger.KnowledgeRuntimeLoggerFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.rule.AgendaFilter;
import org.junit.After;
import org.junit.Before;

public class SimpleDroolsTestSupport {
	
	/**
	 * I make use of this timeout in the @Test annotation to have an additional mechanism to limit chances of continous loops
	 */
	public final static long JUNIT_TIMEOUT = 5*1000;

	@org.junit.Rule
	public SimpleDroolsTestWatcher simpleDroolsTestWatcher = new SimpleDroolsTestWatcher(); 
	
	private KnowledgeBase kbase;
	private StatefulKnowledgeSession ksession;
	private KnowledgeRuntimeLogger krlogger;

	@Before
	public void setup() {
		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
		
		String[] drlFiles = simpleDroolsTestWatcher.getDrlFiles();
		if (drlFiles == null || drlFiles.length == 0) {
			throw new IllegalArgumentException("No DRL files specified to be used. Have you specified them with the proper WithSimpleDrlFiles annotation?");
		} else {
			for (String drlFile : drlFiles) {
				kbuilder.add(ResourceFactory.newClassPathResource(drlFile), ResourceType.DRL);	
			}
		}
		if (kbuilder.hasErrors()) {
			StringBuilder sb = new StringBuilder();
			for (KnowledgeBuilderError err : kbuilder.getErrors()) {
				sb.append(err+"\n");
			}
			throw new IllegalArgumentException("KnowledgeBuilder error(s):\n"+sb.toString());
		}
		kbase = KnowledgeBaseFactory.newKnowledgeBase();
		kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
		
		AgendaFilter af = null;
		if (simpleDroolsTestWatcher.getSimpleRuleFilters() != null) {
			af = new SimpleDroolsTestAgendaFilterFromList(simpleDroolsTestWatcher.getSimpleRuleFilters());
		}
		
		StatefulKnowledgeSession theTrueKS = kbase.newStatefulKnowledgeSession();
		ksession = SimpleDroolsTestStatefulKSProxy.newInstance(theTrueKS, af);
		
		krlogger = KnowledgeRuntimeLoggerFactory.newFileLogger(theTrueKS, simpleDroolsTestWatcher.getTheMethod());
	}
	
	@After
	public void after() {
		krlogger.close();
	}

	public KnowledgeBase getKbase() {
		return kbase;
	}

	public StatefulKnowledgeSession getKsession() {
		return ksession;
	}

	public KnowledgeRuntimeLogger getKrlogger() {
		return krlogger;
	}
	
	
}
