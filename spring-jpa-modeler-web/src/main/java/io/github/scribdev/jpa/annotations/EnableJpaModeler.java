package io.github.scribdev.jpa.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import io.github.scribdev.jpa.configuration.JpaModelerConfiguration;

/**
 * Indicates that Jpa Modeler support should be enabled.
 *
 * This should be applied to a Spring java config and should have an
 * accompanying '@Configuration' annotation.
 *
 * Loads all required beans defined in @see JpaExplorerConfiguration
 *
 */
@Retention(value = java.lang.annotation.RetentionPolicy.RUNTIME)
@Target(value = { java.lang.annotation.ElementType.TYPE })
@Documented
@Import({ JpaModelerConfiguration.class })
public @interface EnableJpaModeler {

}
