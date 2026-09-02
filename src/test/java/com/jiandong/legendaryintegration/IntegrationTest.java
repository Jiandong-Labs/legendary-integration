package com.jiandong.legendaryintegration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringJUnitConfig
@EnableIntegration
@DirtiesContext
public @interface IntegrationTest {

	@AliasFor(annotation = SpringJUnitConfig.class, attribute = "classes")
	Class<?>[] value() default {};

	@AliasFor(annotation = SpringJUnitConfig.class, attribute = "classes")
	Class<?>[] classes() default {};

}
