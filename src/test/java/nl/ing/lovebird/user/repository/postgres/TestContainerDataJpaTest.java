package nl.ing.lovebird.user.repository.postgres;

import nl.ing.lovebird.postgres.test.EnableExternalPostgresTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@DataJpaTest
@ActiveProfiles("test")
@EnableExternalPostgresTestDatabase
public @interface TestContainerDataJpaTest {
}
