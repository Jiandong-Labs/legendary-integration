package com.jiandong.legendaryintegration.jdbc;

import java.math.BigDecimal;

import com.jiandong.legendaryintegration.config.DataSourceConfig;
import com.jiandong.legendaryintegration.testcontainer.PostgresContainerTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.endpoint.SourcePollingChannelAdapter;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@EnableIntegration
@SpringJUnitConfig(classes = {JdbcAdaptersConfig.class, DataSourceConfig.class})
@DirtiesContext
class JdbcAdaptersConfigTests implements PostgresContainerTest {

	@Autowired
	@Qualifier("jdbcOutboundFlow.input")
	MessageChannel jdbcInputChannel;

	@Autowired
	@Qualifier("paymentPoller")
	SourcePollingChannelAdapter paymentPoller;

	@Autowired
	QueueChannel jdbcOutputChannel;

	@Test
	void happyFlow() {
		JdbcAdaptersConfig.Payment payment = new JdbcAdaptersConfig.Payment(null, "123", BigDecimal.TEN, "CNY", null, null);
		jdbcInputChannel.send(MessageBuilder.withPayload(payment).build());

		paymentPoller.start();
		Message<?> message = jdbcOutputChannel.receive(10000);
		Assertions.assertThat(message).extracting(Message::getPayload)
				.isInstanceOfSatisfying(JdbcAdaptersConfig.Payment.class, pmt -> {
					Assertions.assertThat(pmt.pmtNo()).isEqualTo(payment.pmtNo());
					Assertions.assertThat(pmt.pmtAmt()).isEqualByComparingTo(payment.pmtAmt());
					Assertions.assertThat(pmt.currency()).isEqualTo(payment.currency());
					Assertions.assertThat(pmt.id()).isNotNull();
					Assertions.assertThat(pmt.createdAt()).isNotNull();
					Assertions.assertThat(pmt.updatedAt()).isNotNull();
				});

		paymentPoller.stop();
	}

}
