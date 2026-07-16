package com.jiandong.legendaryintegration.gateway;

import java.util.List;
import java.util.Objects;

import com.jiandong.legendaryintegration.controlbus.ControlBusGatewayConfig;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/gateway")
class GatewayController {

	final ObjectProvider<ControlBusGatewayConfig.ControlBusGateway> controlBusGatewayProvider;

	GatewayController(ObjectProvider<ControlBusGatewayConfig.ControlBusGateway> gatewayProvider) {
		this.controlBusGatewayProvider = gatewayProvider;
	}

	@PostMapping("/control-bus/{command}")
	public Object controlBus(@PathVariable String command, @RequestBody(required = false) List<Object> arguments) {
		return controlBusGatewayProvider.getObject().send(command, Objects.requireNonNullElse(arguments, List.of()));
	}

}