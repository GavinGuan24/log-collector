package org.gavin.log.collector;

import org.gavin.log.collector.config.YmlConfig;
import org.gavin.log.collector.config.applicationListener.ApplicationReadyEventListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@EnableScheduling
@RestController
@SpringBootApplication
public class LogcollectorApplication {

	@Autowired
	private YmlConfig ymlConfig;

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(LogcollectorApplication.class);
		app.addListeners(new ApplicationReadyEventListener());
		app.run(args);
	}

	@RequestMapping(value = "/", method = {RequestMethod.GET, RequestMethod.POST})
	public Object root() throws Exception {

	    return "Log Collector is Running ─=≡Σ(((つ•̀ω•́)つ";
	}

	@RequestMapping(value = "/debugInfo", method = {RequestMethod.GET, RequestMethod.POST})
	public Object debugInfo() throws Exception {

		return ymlConfig.getContext();
	}
}
