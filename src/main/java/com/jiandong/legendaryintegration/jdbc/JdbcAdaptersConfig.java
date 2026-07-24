package com.jiandong.legendaryintegration.jdbc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.jdbc.dsl.Jdbc;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

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
						.rowMapper(new BeanPropertyRowMapper<>(Payment.class)), e -> e
						.id("paymentPoller")
						.autoStartup(false)
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

	public static class Payment {

		private Long id;

		private String pmtNo;

		private BigDecimal pmtAmt;

		private String currency;

		private OffsetDateTime createdAt;

		private OffsetDateTime updatedAt;

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public String getPmtNo() {
			return pmtNo;
		}

		public void setPmtNo(String pmtNo) {
			this.pmtNo = pmtNo;
		}

		public BigDecimal getPmtAmt() {
			return pmtAmt;
		}

		public void setPmtAmt(BigDecimal pmtAmt) {
			this.pmtAmt = pmtAmt;
		}

		public String getCurrency() {
			return currency;
		}

		public void setCurrency(String currency) {
			this.currency = currency;
		}

		public OffsetDateTime getCreatedAt() {
			return createdAt;
		}

		public void setCreatedAt(OffsetDateTime createdAt) {
			this.createdAt = createdAt;
		}

		public OffsetDateTime getUpdatedAt() {
			return updatedAt;
		}

		public void setUpdatedAt(OffsetDateTime updatedAt) {
			this.updatedAt = updatedAt;
		}

		@Override
		public String toString() {
			return "Payment{" +
					"id=" + id +
					", pmtNo='" + pmtNo + '\'' +
					", pmtAmt=" + pmtAmt +
					", currency='" + currency + '\'' +
					", createdAt=" + createdAt +
					", updatedAt=" + updatedAt +
					'}';
		}

	}

}
