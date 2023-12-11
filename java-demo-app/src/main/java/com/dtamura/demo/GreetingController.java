package com.dtamura.demo;

import java.util.concurrent.atomic.AtomicLong;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;

@RestController
public class GreetingController {

   private static final String template = "Hello, %s!";
   private final AtomicLong counter = new AtomicLong();

   private Tracer tracer;
   private Meter meter;
   private LongCounter longCounter;

   private Logger logger = LogManager.getLogger(GreetingController.class.getName());

   public GreetingController(OpenTelemetry openTelemetry) {
      this.tracer = openTelemetry.getTracer(GreetingController.class.getName(), "0.1.0");
      // メトリクスの設定
      this.meter = openTelemetry.getMeterProvider().get(GreetingController.class.getName());
      this.longCounter = meter.counterBuilder("greeting_requests")
                     .setDescription("Total number of greeting requests")
                     .setUnit("1")
                     .build();
   }

   @GetMapping("/greeting")
   public Greeting greeting(@RequestParam(value = "name", defaultValue = "World") String name) {

      logger.info("start greeting");

      Span span = tracer.spanBuilder("hello").startSpan();
      try (Scope scope = span.makeCurrent()) {
         // span
         hoge();
      } finally {
         span.end();
      }

      // カウンターを増やす
      longCounter.add(1);

      logger.info("end greeting");
      return new Greeting(counter.incrementAndGet(), String.format(template, name));
   }

   public void hoge() {
      // logger.info("start hoge");
      Span span = tracer.spanBuilder("hoge").startSpan();
      try (Scope scope = span.makeCurrent()) {
         logger.info("hoge");
      } finally {
         span.end();
      }

      logger.info("end hoge");
   }
}
