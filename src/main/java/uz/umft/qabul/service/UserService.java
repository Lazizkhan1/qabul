package uz.umft.qabul.service;

import org.springframework.stereotype.Service;
import uz.umft.qabul.repository.UserRepository;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    // TODO: Implement user management logic
}
