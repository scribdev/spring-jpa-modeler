package io.github.scribdev.jpa.model;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import javax.persistence.EntityManagerFactory;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;

import org.hibernate.engine.query.spi.HQLQueryPlan;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.data.util.StreamUtils;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Component
public class JpaModelMappingContext {

	private ApplicationContext beanFactory;

	public JpaModelMappingContext(ApplicationContext applicationContext) {
		this.beanFactory = applicationContext;
	}

	public JsonNode getModel() {
		ObjectMapper mapper = new ObjectMapper();
		return initModelNode(mapper);

	}

	private ObjectNode initModelNode(ObjectMapper mapper) {
		ObjectNode modelNode = mapper.createObjectNode();

		Stream<EntityType<?>> entityStream = getMetamodels().stream().flatMap(it -> it.getEntities().stream());

		ArrayNode entityArray = mapper.createArrayNode();

		entityStream.map(it -> {

			ObjectNode entityNode = mapper.createObjectNode();

			entityNode.put("name", it.getName());
			entityNode.put("javaType", it.getJavaType().getName());
			entityNode.put("bindableJavaType", it.getBindableJavaType().getName());
			entityNode.put("bindableType", it.getBindableType().name());
			entityNode.put("persistenceType", it.getPersistenceType().name());

			ArrayNode attributeArray = mapper.createArrayNode();
			for (Attribute<?, ?> attribute : it.getAttributes()) {

				ObjectNode attributeNode = mapper.createObjectNode();

				attributeNode.put("name", attribute.getName());
				attributeNode.put("persistentAttributeType", attribute.getPersistentAttributeType().name());
				attributeNode.put("javaMember", attribute.getJavaMember().getName());
				attributeNode.put("javaType", attribute.getJavaType().getName());
				attributeNode.put("isCollection", attribute.isCollection());
				attributeNode.put("isAssociation", attribute.isAssociation());

				attributeArray.add(attributeNode);

			}
			entityNode.set("attributes", attributeArray);

			return entityNode;
		}).forEach(it -> entityArray.add(it));

		modelNode.set("entities", entityArray);

		return modelNode;
	}

	public ObjectNode toSelectAllSqlQuery(String className) {

		ObjectMapper objectMapper = new ObjectMapper();
		ObjectNode sqlQueryNode = objectMapper.createObjectNode();

		for (EntityManagerFactory entityManagerFactory : getEntityManagerFactories()) {
			Optional<EntityType<?>> entityTypeOpt = entityManagerFactory.getMetamodel().getEntities().stream()
					.filter(it -> it.getJavaType().getName().contentEquals(className)).findAny();
			if (entityTypeOpt.isPresent()) {
				EntityType<?> entityType = entityTypeOpt.get();
				String jpql = "SELECT a FROM " + entityType.getName() + " a";
				SessionFactoryImplementor sessionFactoryImplementor = entityManagerFactory
						.unwrap(SessionFactoryImplementor.class);
				HQLQueryPlan hqlQueryPlan = sessionFactoryImplementor.getQueryPlanCache().getHQLQueryPlan(jpql, false,
						Collections.emptyMap());

				sqlQueryNode.put("jpql", hqlQueryPlan.getSourceQuery());

				ArrayNode sqlStatementArray = objectMapper.createArrayNode();
				for (String sqlStatement : hqlQueryPlan.getSqlStrings()) {
					sqlStatementArray.add(sqlStatement);
				}
				sqlQueryNode.set("sqlStatements", sqlStatementArray);

			}

		}

		return sqlQueryNode;
	}

	/**
	 * Obtains all {@link Metamodel} instances of the current
	 * {@link ApplicationContext}.
	 *
	 * @return
	 */
	private Set<Metamodel> getMetamodels() {

		if (beanFactory == null) {
			throw new IllegalStateException("BeanFactory must not be null!");
		}

		Collection<EntityManagerFactory> factories = getEntityManagerFactories();

		return factories.stream() //
				.map(EntityManagerFactory::getMetamodel) //
				.collect(StreamUtils.toUnmodifiableSet());
	}

	private Collection<EntityManagerFactory> getEntityManagerFactories() {
		Collection<EntityManagerFactory> factories = BeanFactoryUtils
				.beansOfTypeIncludingAncestors(beanFactory, EntityManagerFactory.class).values();
		return factories;
	}

}
