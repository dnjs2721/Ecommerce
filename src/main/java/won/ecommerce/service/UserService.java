package won.ecommerce.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import won.ecommerce.entity.User;
import won.ecommerce.repository.UserRepository;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    @Transactional
    public Long join(User user) {
        userRepository.save(user);
        return user.getId();
    }

    public void validateDuplicateUser(String email) {
        Optional<User> findUser = userRepository.findByEmail(email);

        if (findUser.isPresent()) {
            throw new IllegalStateException();
        }
    }
}
