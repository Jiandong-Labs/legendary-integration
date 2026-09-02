package com.jiandong.legendaryintegration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.annotation.AliasFor;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.test.annotation.DirtiesContext;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest
@EnableIntegration
@DirtiesContext
public @interface IntegrationBootTest {

	@AliasFor(annotation = SpringBootTest.class, attribute = "value")
	String[] value() default {};

	@AliasFor(annotation = SpringBootTest.class, attribute = "classes")
	Class<?>[] classes() default {};

}
