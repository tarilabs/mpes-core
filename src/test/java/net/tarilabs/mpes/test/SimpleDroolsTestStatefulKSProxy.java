package net.tarilabs.mpes.test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.rule.AgendaFilter;

/**
 * This class will Proxy a {@link StatefulKnowledgeSession}, and then every time a fire* method is invoked, will check if {@link AgendaFilter} has been specified during the invocation: if not, it will "force" an addition the AgendaFilter, specified at construction time of this proxy, to that fire* method invocation.
 * 
 * @author tari
 *
 */
public class SimpleDroolsTestStatefulKSProxy implements InvocationHandler {
	
	private AgendaFilter af;
	private StatefulKnowledgeSession ks;

    public static StatefulKnowledgeSession newInstance(StatefulKnowledgeSession ks, AgendaFilter af) {
        return (StatefulKnowledgeSession) java.lang.reflect.Proxy.newProxyInstance(
            StatefulKnowledgeSession.class.getClassLoader(),
            new Class[] { StatefulKnowledgeSession.class },
            new SimpleDroolsTestStatefulKSProxy(ks, af));
    }

    private SimpleDroolsTestStatefulKSProxy(StatefulKnowledgeSession ks, AgendaFilter af) {
        super();
    	this.ks = ks;
        this.af = af;
    }


	@Override
	public Object invoke(Object p, Method m, Object[] args) throws Throwable {
		Object result;
        try {
        	// I intercept any fire* methods, and provided you actually requested an AgendaFilter with this proxy.
        	Object[] args2 = null;
        	if (m.getName().startsWith("fire") && af != null) {
        		// I check if any AgendaFilter has been defined, in such case I won't specify any
        		boolean hasAgendaBeenSpecified = false;
        		if (args != null && args.length > 0) {
        			for (Object o : args) {
        				if (o instanceof AgendaFilter) {
        					hasAgendaBeenSpecified = true;
        				}
        			}
        		}
        		if (!hasAgendaBeenSpecified) {
        			// the AgendaFilter was not specified so I add the one I'm Proxy-ing for :)
        			
        			if (args != null && args.length > 0) {
        				args2 = new Object[args.length + 1];
        				System.arraycopy(args, 0, args2, 0, args.length);
        				args2[args.length] = af;
        			} else {
        				// if args == null || args.length == 0 I can safely just put AgendaFilter anyway.
        				args2 = new Object[] { af };
        			}
        			
        			// After having identified the new arguments as args2, I need to identify the actual method to be invoked with the correct parameters
        			Method m2 = null;
        			if (m.getName().equals("fireUntilHalt")) {
        				m2 = getKs().getClass().getMethod("fireUntilHalt", AgendaFilter.class);
        			} else if (m.getName().equals("fireAllRules")) {
        				if (args == null || args.length == 0) {
        					m2 = getKs().getClass().getMethod("fireAllRules", AgendaFilter.class);
        				} else if (args.length == 1) {
        					m2 = getKs().getClass().getMethod("fireAllRules", AgendaFilter.class, int.class);
        				}
        			}
        			System.out.println("bypassing and invoking with "+args2);
        			result = m2.invoke(getKs(), args2);
        			
        		} else {
        			// the AgendaFilter was indeed specified, so do nothing actually.
        			result = m.invoke(getKs(), args);
        		}
        	} else {
        		result = m.invoke(getKs(), args);
        	}
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        } catch (Exception e) {
        	e.printStackTrace();
            throw new RuntimeException("unexpected invocation exception: " + e.getMessage());
        } 
        return result;
	}

	/**
	 * I need this getter to get the enclosed instance by this Proxy to pass it to the KnoledgeLogger
	 * @return
	 */
	protected StatefulKnowledgeSession getKs() {
		return ks;
	}

}
