package net.tarilabs.mpes.test;

import org.drools.runtime.rule.Activation;
import org.drools.runtime.rule.AgendaFilter;

public class SimpleDroolsTestAgendaFilterFromList implements AgendaFilter {
	
	private String[] simpleRuleFilters;
	
	public SimpleDroolsTestAgendaFilterFromList(String[] simpleRuleFilters) {
		super();
		if (simpleRuleFilters == null || simpleRuleFilters.length == 0) {
			throw new IllegalArgumentException("simpleRuleFilters cannot be null nor empty.");
		}
		this.simpleRuleFilters = simpleRuleFilters;
	}

	@Override
	public boolean accept(Activation activation) {
		System.out.print("You are asking me if to activate rule \""+activation.getRule().getName()+"\"");
		for (String s : simpleRuleFilters) {
			if (activation.getRule().getName().equals(s)) {
				// the Activation matches the filter I wanted to activate
				System.out.println(" --> YES.");
				return true;
			}
		}
		System.out.println(" --> NO.");
		return false;
	}

}
