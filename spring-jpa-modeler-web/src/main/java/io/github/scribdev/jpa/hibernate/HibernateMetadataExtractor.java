package io.github.scribdev.jpa.hibernate;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.Iterator;

import org.hibernate.boot.Metadata;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.Table;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.hbm2ddl.SchemaExport.Action;
import org.hibernate.tool.schema.TargetType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

@Service
public class HibernateMetadataExtractor {

	@Autowired
	private ApplicationContext applicationContext;

	private Logger logger = LoggerFactory.getLogger(HibernateMetadataExtractor.class);

	public void extractJPAInfo() {

		Metadata metadata = getMetadata();

		for (PersistentClass persistentClass : metadata.getEntityBindings()) {

			Table table = persistentClass.getTable();

			logger.info("Entity: {} is mapped to table: {}", persistentClass.getClassName(), table.getName());

			for (Iterator<?> propertyIterator = persistentClass.getPropertyIterator(); propertyIterator.hasNext();) {
				Property property = (Property) propertyIterator.next();

				for (Iterator<?> columnIterator = property.getColumnIterator(); columnIterator.hasNext();) {
					Column column = (Column) columnIterator.next();

					logger.info("Property: {} is mapped on table column: {} of type: {}", property.getName(),
							column.getName(), column.getSqlType());
				}
			}
		}
	}

	private Metadata getMetadata() {
		return MetadataExtractorIntegrator.INSTANCE.getMetadata();
	}

	public String exportDllSchema() throws IOException {

		Metadata metadata = getMetadata();

		String id = applicationContext.getId();
		File tempSchemaExportFile = File.createTempFile(id + "_", "_hibernate-schema.sql");
		tempSchemaExportFile.deleteOnExit();

		SchemaExport schemaExport = new SchemaExport();
		schemaExport.setHaltOnError(true);
		schemaExport.setFormat(true);
		schemaExport.setDelimiter(";");
		schemaExport.setOutputFile(tempSchemaExportFile.getAbsolutePath());

		schemaExport.execute(EnumSet.of(TargetType.SCRIPT), Action.CREATE, metadata);

		return StreamUtils.copyToString(new FileInputStream(tempSchemaExportFile), StandardCharsets.UTF_8);
	}
}