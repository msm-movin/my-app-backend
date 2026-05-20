package org.setup.mycrud.service;

import org.setup.mycrud.model.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {
    User UserSave(User user, MultipartFile file);

    List<User> GetAllUsers();

    User UpdateUser(User user, MultipartFile file);

    User DeleteUser(Long id);

    User GetUserById(Long id);

    User PartialUpdate(User user);

    String UpdateProfilePicture(User user, MultipartFile file);
}
