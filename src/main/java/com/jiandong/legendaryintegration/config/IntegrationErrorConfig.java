package com.jiandong.legendaryintegration.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.context.IntegrationContextUtils;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.ErrorMessage;

import static com.jiandong.legendaryintegration.util.Constants.ROUTER_KEY;
import static com.jiandong.legendaryintegration.util.Constants.ROUTER_VAL_RATE_LIMIT;
import static com.jiandong.legendaryintegration.util.Constants.ROUTER_VAL_RATE_LIMIT_CHANNEL;
import static com.jiandong.legendaryintegration.util.Constants.ROUTER_VAL_RETRY;
import static com.jiandong.legendaryintegration.util.Constants.ROUTER_VAL_RETRY_CHANNEL;

@Configuration(proxyBeanMethods = false)
public class IntegrationErrorConfig {

	private static final Logger log = LoggerFactory.getLogger(IntegrationErrorConfig.class);

	@Bean(IntegrationContextUtils.ERROR_CHANNEL_BEAN_NAME)
	PublishSubscribeChannel errorChannel() {
		PublishSubscribeChannel errorChannel = new PublishSubscribeChannel(true);
		errorChannel.setIgnoreFailures(true);
		return errorChannel;
	}

	@Bean
	IntegrationFlow globalErrorHandlingFlow() {
		return IntegrationFlow.from(IntegrationContextUtils.ERROR_CHANNEL_BEAN_NAME)
				.route(Message.class, msg -> {
							Object routeKey = msg.getHeaders().get(ROUTER_KEY);
							if (routeKey == null && msg instanceof ErrorMessage errMsg && errMsg.getOriginalMessage() != null) {
								return errMsg.getOriginalMessage().getHeaders().get(ROUTER_KEY);
							}
							return routeKey;
						}, e -> e
								.id("headerValueRouter")
								.resolutionRequired(false)
								.channelMapping(ROUTER_VAL_RATE_LIMIT, ROUTER_VAL_RATE_LIMIT_CHANNEL)
								.channelMapping(ROUTER_VAL_RETRY, ROUTER_VAL_RETRY_CHANNEL)
								.defaultOutputToParentFlow()
				)
				.<MessagingException>handle((p, h) -> {
					log.info("Unexpected Exception: {}", p.getMessage(), p);
					return null;
				}, e -> e
						.id("globalErrorHandler"))
				.get();
	}

}
