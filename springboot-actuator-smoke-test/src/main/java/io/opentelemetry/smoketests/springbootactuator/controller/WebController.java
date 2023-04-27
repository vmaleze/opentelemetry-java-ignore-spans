package io.opentelemetry.smoketests.springbootactuator.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WebController {
  private static final Logger logger = LoggerFactory.getLogger(WebController.class);

  @RequestMapping("/ping")
  public String greeting() {
    logger.info("HTTP request received");
    return "pong";
  }

}
