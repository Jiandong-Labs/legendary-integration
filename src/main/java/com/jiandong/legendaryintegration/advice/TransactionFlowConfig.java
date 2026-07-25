package com.jiandong.legendaryintegration.advice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.transaction.PseudoTransactionManager;
import org.springframework.integration.transaction.TransactionInterceptorBuilder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;

@Configuration(proxyBeanMethods = false)
class TransactionFlowConfig {

	private static final Logger log = LoggerFactory.getLogger(TransactionFlowConfig.class);

	@Bean
	PseudoTransactionManager pseudoTransactionManager() {
		return new PseudoTransactionManager();
	}

	@Bean
	IntegrationFlow transactionFlow(PlatformTransactionManager transactionManager) {
		return flow -> flow
				.handle(msg -> log.info("this is inside a transaction"), e -> e
						.id("transactionalHandler")
						.advice(new TransactionInterceptorBuilder()
								.propagation(Propagation.REQUIRED)
								.isolation(Isolation.READ_COMMITTED)
								.transactionManager(transactionManager)
								.build())
				);
	}

}
