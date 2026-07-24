package com.jiandong.legendaryintegration;

import com.jiandong.legendaryintegration.config.ActivemqConfig;
import com.jiandong.legendaryintegration.config.DataSourceConfig;
import com.jiandong.legendaryintegration.config.RedisConfig;
import com.jiandong.legendaryintegration.testcontainer.ActivemqContainerTest;
import com.jiandong.legendaryintegration.testcontainer.PostgresContainerTest;
import com.jiandong.legendaryintegration.testcontainer.RedisContainerTest;
import org.junit.jupiter.api.Test;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest
@Import(value = {ActivemqConfig.class, RedisConfig.class, DataSourceConfig.class})
@DirtiesContext
class LegendaryIntegrationApplicationTests implements ActivemqContainerTest, RedisContainerTest, PostgresContainerTest {

	@Test
	void contextLoads() {
	}

}
