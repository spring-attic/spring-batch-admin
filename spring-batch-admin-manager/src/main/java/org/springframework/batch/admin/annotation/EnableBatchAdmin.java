package org.springframework.batch.admin.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.ImportResource;

/**
 * This annotation is responsible for bootstrapping Spring Batch Admin
 * within an existing web application.
 *
 * @author Michael Minella
 * @since 2.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ImportResource({"classpath*:/META-INF/spring/batch/bootstrap/**/*.xml",
		"classpath*:/META-INF/spring/batch/override/**/*.xml",
		"classpath*:/org/springframework/batch/admin/web/resources/servlet-config.xml"})
public @interface EnableBatchAdmin {
}
