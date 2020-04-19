package io.github.scribdev.jpa.model;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.EntityManagerFactory;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;

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

	private static final String JPQL = "jpql";

	private static final String SQL_STATEMENTS = "sqlStatements";

	private static final String VALUE = "value";

	private static final String VALUES = "values";

	private static final String TYPE = "type";

	private static final String ENTITIES = "entities";

	private static final String ATTRIBUTES = "attributes";

	private static final String CLASS = "class";

	private static final String IS_ASSOCIATION = "isAssociation";

	private static final String IS_COLLECTION = "isCollection";

	private static final String JAVA_MEMBER = "javaMember";

	private static final String PERSISTENT_ATTRIBUTE_TYPE = "persistentAttributeType";

	private static final String ANNOTATIONS = "annotations";

	private static final String PERSISTENCE_TYPE = "persistenceType";

	private static final String BINDABLE_TYPE = "bindableType";

	private static final String BINDABLE_JAVA_TYPE = "bindableJavaType";

	private static final String JAVA_TYPE = "javaType";

	private static final String NAME = "name";

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

			entityNode.put(NAME, it.getName());
			entityNode.put(JAVA_TYPE, it.getJavaType().getName());
			entityNode.put(BINDABLE_JAVA_TYPE, it.getBindableJavaType().getName());
			entityNode.put(BINDABLE_TYPE, it.getBindableType().name());
			entityNode.put(PERSISTENCE_TYPE, it.getPersistenceType().name());

			ArrayNode entityAnnotationsNode = initAnnotationsNode(mapper, it.getJavaType());
			entityNode.set(ANNOTATIONS, entityAnnotationsNode);

			ArrayNode attributeArray = mapper.createArrayNode();
			for (Attribute<?, ?> attribute : it.getDeclaredAttributes()) {

				ObjectNode attributeNode = mapper.createObjectNode();

				Member javaMember = attribute.getJavaMember();

				attributeNode.put(NAME, attribute.getName());
				attributeNode.put(PERSISTENT_ATTRIBUTE_TYPE, attribute.getPersistentAttributeType().name());
				attributeNode.put(JAVA_MEMBER, javaMember.getName());
				attributeNode.put(IS_COLLECTION, attribute.isCollection());
				attributeNode.put(IS_ASSOCIATION, attribute.isAssociation());
				attributeNode.put(CLASS, attribute.getClass().getName());

				if (attribute instanceof SingularAttribute) {
					SingularAttribute<?, ?> singluarAttribute = (SingularAttribute<?, ?>) attribute;
					attributeNode.put("isId", singluarAttribute.isId());
					attributeNode.put("isOptional", singluarAttribute.isOptional());
					attributeNode.put(JAVA_TYPE, singluarAttribute.getType().getJavaType().getName());
					attributeNode.put(PERSISTENCE_TYPE, singluarAttribute.getType().getPersistenceType().name());
				}

				if (attribute instanceof PluralAttribute) {
					PluralAttribute<?, ?, ?> pluralAttribute = (PluralAttribute<?, ?, ?>) attribute;
					attributeNode.put("collectionType", pluralAttribute.getCollectionType().name());
					attributeNode.put("elementType", pluralAttribute.getElementType().getJavaType().getName());
				}

				ArrayNode attributeAnnotationsNode = initAnnotationsNode(mapper, javaMember);
				attributeNode.set(ANNOTATIONS, attributeAnnotationsNode);

				attributeArray.add(attributeNode);

			}
			entityNode.set(ATTRIBUTES, attributeArray);

			return entityNode;
		}).forEach(it -> entityArray.add(it));

		modelNode.set(ENTITIES, entityArray);

		return modelNode;
	}

	private ArrayNode initAnnotationsNode(ObjectMapper mapper, Object object) {

		ArrayNode annotationsNode = mapper.createArrayNode();

		if (object instanceof AnnotatedElement) {
			List<Annotation> annotations = Arrays.asList(((AnnotatedElement) object).getAnnotations());

			for (Annotation annotation : annotations) {
				Class<? extends Annotation> annotationType = annotation.annotationType();

				ObjectNode annotationNode = mapper.createObjectNode();
				annotationNode.put(TYPE, annotationType.getName());

				ArrayNode annotationValuesNode = mapper.createArrayNode();
				annotationNode.set(VALUES, annotationValuesNode);

				try {
					for (Method method : annotationType.getDeclaredMethods()) {

						Object value = method.invoke(annotation, (Object[]) null);
						Object defaultValue = method.getDefaultValue();
						if (annotationValueEqual(value, defaultValue)) {
							continue;
						}

						ObjectNode annotationValueNode = mapper.createObjectNode();
						annotationValuesNode.add(annotationValueNode);

						annotationValueNode.put(NAME, method.getName());
						annotationValueNode.put(VALUE, annotationValueToString(value));

					}
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				}

				annotationsNode.add(annotationNode);
			}
		}

		return annotationsNode;
	}

	private boolean annotationValueEqual(Object value, Object defaultValue) {

		Object valueObject = normalizeAnnotationValue(value);
		Object defaultValueObject = normalizeAnnotationValue(defaultValue);

		if (valueObject instanceof List<?> && defaultValueObject instanceof List<?>) {
			return valueObject.equals(defaultValueObject);
		}

		return Objects.equals(value, defaultValue);
	}

	private String annotationValueToString(Object annotationValue) {
		Object value = normalizeAnnotationValue(annotationValue);
		if (value instanceof List) {
			return ((List<?>) value).stream().map(arrayValue -> Objects.toString(arrayValue))
					.collect(Collectors.joining(", ", "[", "]"));
		} else {
			return Objects.toString(value);
		}
	}

	private Object normalizeAnnotationValue(Object annotationValue) {
		Object value = null;
		if (annotationValue.getClass().isArray()) {
			List<Object> values = new ArrayList<Object>();
			int length = Array.getLength(annotationValue);
			for (int i = 0; i < length; i++) {
				Object arrayElement = Array.get(annotationValue, i);
				values.add(arrayElement);
			}
			value = values;
		} else {
			value = annotationValue;
		}
		return value;
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

				sqlQueryNode.put(JPQL, hqlQueryPlan.getSourceQuery());

				ArrayNode sqlStatementArray = objectMapper.createArrayNode();
				for (String sqlStatement : hqlQueryPlan.getSqlStrings()) {
					sqlStatementArray.add(sqlStatement);
				}
				sqlQueryNode.set(SQL_STATEMENTS, sqlStatementArray);

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
