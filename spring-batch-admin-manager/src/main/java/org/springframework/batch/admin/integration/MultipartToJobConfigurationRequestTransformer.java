package org.springframework.batch.admin.integration;

import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.Transformer;

@MessageEndpoint
public class MultipartToJobConfigurationRequestTransformer {

	@Transformer
	public JobConfigurationRequest transform(MultipartJobConfigurationRequest payload) {
		 return payload.getJobConfigurationRequest();
	}

}
