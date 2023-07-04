package nl.ing.lovebird.user.datascience;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.clienttokens.ClientToken;
import nl.ing.lovebird.user.repository.postgres.UserRepository;
import nl.ing.lovebird.user.repository.postgres.model.User;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Stream;

import static nl.ing.lovebird.user.configuration.ApplicationConfiguration.ASYNC_BATCH_EXECUTOR;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserSyncService {

    private final UserRepository userRepository;

    private final UserDataProducer userDataProducer;

    public void syncUser(ClientToken clientToken, User user) {
        UserData userData = new UserData(user.getClientId(), user.getUserId(), user.getClientUserId(), user.getBlocked(), user.getBlockedReason(), user.getCreated(), user.isOneOffAis(), user.getDeleted());
        userDataProducer.sendMessage(clientToken, userData);
    }

    @Async(ASYNC_BATCH_EXECUTOR)
    @Transactional
    public void syncAllUsers() {
        try (Stream<User> stream = userRepository.streamAllBy()) {
            stream.forEach(user -> {
                UserData userData = new UserData(user.getClientId(), user.getUserId(), user.getClientUserId(), user.getBlocked(), user.getBlockedReason(), user.getCreated(), user.isOneOffAis(), user.getDeleted());
                userDataProducer.sendMessage(null, userData);
            });
        }
    }
}
