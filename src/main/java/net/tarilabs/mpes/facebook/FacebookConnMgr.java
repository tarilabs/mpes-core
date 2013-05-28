package net.tarilabs.mpes.facebook;

import javax.ejb.Singleton;
import javax.ejb.Startup;

import net.tarilabs.mpes.model.MpesSentence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.types.FacebookType;

@Singleton
@Startup
public class FacebookConnMgr {

	private static final Logger logger = LoggerFactory.getLogger(FacebookConnMgr.class);
	
	private String access_token;
	private FacebookClient facebookClient;

	public String getAccess_token() {
		return access_token;
	}

	public void setAccess_token(String access_token) {
		logger.info("Setting access token to: "+access_token);
		this.access_token = access_token;
		facebookClient = new DefaultFacebookClient(access_token);
	}
	
	public void post(MpesSentence ms) {
		if (facebookClient == null) {
			logger.warn("No Facebook client available");
			return;
		}
		
		FacebookType publishMessageResponse = facebookClient.publish("me/feed", FacebookType.class,
				    Parameter.with("message", ms.getMessage()),
				    Parameter.with("link", "http://www.tarilabs.net"),
				    Parameter.with("name", "MPES"),
				    Parameter.with("caption", "Mobile & Pervasive Expert System"),
				    Parameter.with("description", "Coming soon.")
				    );

		logger.info("Published message ID: " + publishMessageResponse.getId());
	}
	
}
