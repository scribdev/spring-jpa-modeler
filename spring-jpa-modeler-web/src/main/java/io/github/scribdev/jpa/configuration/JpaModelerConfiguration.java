package io.github.scribdev.jpa.configuration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import io.github.scribdev.jpa.model.JpaModelMappingContext;
import io.github.scribdev.jpa.web.JpaModelerController;

@Configuration
@ConditionalOnWebApplication
@ComponentScan(basePackageClasses = { JpaModelerController.class, JpaModelMappingContext.class })
public class JpaModelerConfiguration {

}
