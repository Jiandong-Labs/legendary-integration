package com.jiandong.legendaryintegration.controlbus;

import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

@Component
@ManagedResource
class CustomBusEndpoint {

	@ManagedOperation
	public void callInternalFlow() {

	}

	@ManagedOperation
	public String callInternalFlow(String input) {
		return input;
	}

}
