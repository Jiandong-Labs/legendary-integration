package com.jiandong.legendaryintegration.jdbc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import javax.sql.DataSource;

import org.jspecify.annotations.Nullable;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.jdbc.dsl.Jdbc;
import org.springframework.jdbc.core.DataClassRowMapper;

@Configuration(proxyBeanMethods = false)
class JdbcAdaptersConfig {

	@Bean
	IntegrationFlow jdbcOutboundFlow(DataSource dataSource) {
		return flow -> flow
				.handle(Jdbc.outboundAdapter(dataSource, "INSERT INTO PAYMENTS (PMT_NO, PMT_AMT, CURRENCY)  VALUES (:pmtNo, :pmtAmt, :currency)")
						.keysGenerated(true)
						.usePayloadAsParameterSource(true));
	}

	@Bean
	IntegrationFlow jdbcInboundFlow(DataSource dataSource) {
		return IntegrationFlow.from(Jdbc.inboundAdapter(dataSource, "SELECT * FROM PAYMENTS")
						.rowMapper(new DataClassRowMapper<>(Payment.class)), e -> e
						.id("paymentPoller")
						.autoStartup(true)
						.poller(Pollers
								.fixedDelay(10000)
								.transactional()))
				.split()
				.<Payment>handle((p, h) -> {
					System.out.println(p);
					return p;
				})
				.channel("jdbcOutputChannel")
				.get();

	}

	@Bean
	QueueChannel jdbcOutputChannel() {
		return new QueueChannel();
	}

	public record Payment(
			@Nullable Long id,
			String pmtNo, BigDecimal pmtAmt, String currency,
			@Nullable OffsetDateTime createdAt, @Nullable OffsetDateTime updatedAt) {

	}

}
