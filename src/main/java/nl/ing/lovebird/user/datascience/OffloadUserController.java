package nl.ing.lovebird.user.datascience;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/batch/sync-user-data")
public class OffloadUserController {

    private final UserSyncService userSyncService;

    @PostMapping("/start")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void syncAllEntityUsers() {
        userSyncService.syncAllUsers();
    }

}
