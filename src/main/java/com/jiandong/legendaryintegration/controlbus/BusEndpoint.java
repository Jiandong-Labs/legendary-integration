package com.jiandong.legendaryintegration.controlbus;

import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.stereotype.Component;

@Component
class BusEndpoint {

	@ManagedOperation
	public void callInternalFlow() {

	}

	@ManagedOperation
	public String callInternalFlow(String input) {
		return input;
	}

}
