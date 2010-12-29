package org.springframework.batch.admin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.rules.TestWatchman;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.SystemPropertyUtils;
import org.springframework.web.client.RestTemplate;

public class ServerRunning extends TestWatchman {

	private static Log logger = LogFactory.getLog(ServerRunning.class);

	private boolean serverOnline = true;

	private final String url;

	/**
	 * @return a new rule that assumes an existing running broker
	 */
	public static ServerRunning isRunning(String url) {
		return new ServerRunning(SystemPropertyUtils.resolvePlaceholders(url));
	}

	private ServerRunning(String url) {
		this.url = url;
	}

	@Override
	public Statement apply(Statement base, FrameworkMethod method, Object target) {

		// Check at the beginning, so this can be used as a static field
		Assume.assumeTrue(serverOnline);

		try {

			RestTemplate template = new RestTemplate();
			ResponseEntity<String> result = template.exchange(url + "/home.json", HttpMethod.GET, null,
					String.class);
			String body = result.getBody();
			Assert.assertTrue("No home page found", body != null && body.length() > 0);

		}
		catch (Exception e) {
			logger.warn("Not executing tests because basic connectivity test failed", e);
			serverOnline = false;
			Assume.assumeNoException(e);
		}

		return super.apply(base, method, target);

	}

	public String getUrl() {
		return this.url;
	}

}
