package com.jiandong.legendaryintegration.testcontainer;

import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * The base contract for JUnit tests based on the container for MQTT Mosquitto broker.
 * The Testcontainers 'reuse' option must be disabled, so, Ryuk container is started
 * and will clean all the containers up from this test suite after JVM exit.
 * Since the Mosquitto container instance is shared via static property, it is going to be
 * started only once per JVM, therefore the target Docker container is reused automatically.
 *
 * @author Jiandong Ma
 *
 * @since 7.2
 */
@Testcontainers(disabledWithoutDocker = true)
public interface MqttContainerTest {

	GenericContainer<?> MQTT_CONTAINER = new GenericContainer<>("eclipse-mosquitto:2.1.2-alpine")
			.withCommand("mosquitto -c /mosquitto-no-auth.conf")
			.withExposedPorts(1883);

	@BeforeAll
	static void startContainer() {
		MQTT_CONTAINER.start();
	}

}
