package io.github.scribdev.jpa.hibernate;

import java.util.Collections;
import java.util.Map;

import org.hibernate.jpa.boot.spi.IntegratorProvider;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.stereotype.Component;

@Component
public class HibernateConfig implements HibernatePropertiesCustomizer {

	@Override
	public void customize(Map<String, Object> hibernateProperties) {
		hibernateProperties.put("hibernate.integrator_provider",
				(IntegratorProvider) () -> Collections.singletonList(MetadataExtractorIntegrator.INSTANCE));
	}
}