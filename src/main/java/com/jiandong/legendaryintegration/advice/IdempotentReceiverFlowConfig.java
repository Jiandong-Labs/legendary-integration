package com.jiandong.legendaryintegration.advice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.handler.advice.IdempotentReceiverInterceptor;
import org.springframework.integration.selector.MetadataStoreSelector;

@Configuration(proxyBeanMethods = false)
public class IdempotentReceiverFlowConfig {

	private static final Logger log = LoggerFactory.getLogger(IdempotentReceiverFlowConfig.class);

	@Bean
	public IdempotentReceiverInterceptor idempotentReceiverInterceptor() {
		var messageSelector = new MetadataStoreSelector(m ->
				(String) m.getHeaders().get("unique_key"));
		var receiverInterceptor = new IdempotentReceiverInterceptor(messageSelector);
		receiverInterceptor.setThrowExceptionOnRejection(true);
		return receiverInterceptor;
	}

	@Bean
	public IntegrationFlow idempotentFlow(IdempotentReceiverInterceptor idempotentReceiverInterceptor) {
		return flow -> flow
				.<String>handle((p, h) -> {
					log.info("payload: {}", p);
					return null;
				}, e -> e
						.id("idempotentHandler")
						.advice(idempotentReceiverInterceptor));
	}

}
