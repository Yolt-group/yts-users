package nl.ing.lovebird.user.repository.postgres;

import nl.ing.lovebird.user.repository.postgres.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@TestContainerDataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    @Test
    void shouldStoreUser() {

        //Given
        final var userId = UUID.randomUUID();
        final var user = new User(
                userId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                null,
                null,
                new Date().toInstant(),
                true,
                null);

        //When
        userRepository.save(user);

        //Then
        final var storedUser = testEntityManager.find(User.class, userId);
        assertThat(storedUser)
                .usingRecursiveComparison()
                .isEqualTo(user);
    }
}
