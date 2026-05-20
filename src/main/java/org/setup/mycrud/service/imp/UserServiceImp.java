package org.setup.mycrud.service.imp;

import org.setup.mycrud.model.User;
import org.setup.mycrud.repositry.UserRepository;
import org.setup.mycrud.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceImp implements UserService {

    @Autowired
    private UserRepository userRepository;

    // [FIX 1]: Standard Linux/Windows path bina aakhiri '/' ke
    private final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads";

    // =========================================================================
    // UPDATE PROFILE PICTURE (HELPER METHOD)
    // =========================================================================
    @Override
    public String UpdateProfilePicture(User user, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            // [FIX 2]: Pehle check karo folder bana hai ya nahi, nahi toh banao
            java.nio.file.Path uploadPath = java.nio.file.Paths.get(UPLOAD_DIR);
            if (!java.nio.file.Files.exists(uploadPath)) {
                java.nio.file.Files.createDirectories(uploadPath);
                System.out.println("Success: 'uploads' folder pehli baar automatic bana diya gaya.");
            }

            // Photo ka unique naam
            String uniqueFileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

            // [FIX 3]: String '+' karne ki jagah .resolve() lagaya Linux path issue fix karne ke liye
            java.nio.file.Path finalPath = uploadPath.resolve(uniqueFileName);

            // File ko physically copy karo folder mein
            java.nio.file.Files.copy(file.getInputStream(), finalPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            user.setProfilePic(uniqueFileName);
            return uniqueFileName;

        } catch (Exception e) {
            System.out.println("WARNING: Technical issue while uploading image. Proceeding without image. Error: " + e.getMessage());
            return null;
        }
    }

    // =========================================================================
    // USER SAVE METHOD (CREATE - WITH OR WITHOUT PIC)
    // =========================================================================
    @Override
    public User UserSave(User user, org.springframework.web.multipart.MultipartFile file) {
        try {
            if (user == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User data is null");
            }

            if (UpdateProfilePicture(user, file) == null) {
                System.out.println("User bina photo ke register ho raha hai.");
            } else {
                System.out.println("Photo successfully saved for user: " + user.getProfilePic());
            }
            return userRepository.save(user);
        } catch (CannotCreateTransactionException e) {
            System.out.println("Database Connection Fail:" + e.getMessage());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Database error: Server se connection nahi ho pa raha hai!");
        } catch (DataIntegrityViolationException e) {
            System.out.println("Duplicate data error: " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This user is already registered!");
        } catch (Exception e) {
            System.out.println("internal error: " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error: " + e.getMessage());
        }
    }

    // =========================================================================
    // GET USER BY ID METHOD (READ - SINGLE)
    // =========================================================================
    @Override
    public User GetUserById(Long id) {
        try {
            return userRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "internal error" + e.getMessage());
        }
    }

    // =========================================================================
    // GET ALL USERS METHOD (READ - LIST)
    // =========================================================================
    @Override
    public List<User> GetAllUsers() {
        List<User> list = new ArrayList<>();
        try {
            list = userRepository.findAll();
            if (list.isEmpty()) {
                System.out.println("Users list is empty");
            }
        } catch (Exception e) {
            System.out.println("Internal server error :" + e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "internal error" + e.getMessage());
        }
        return list;
    }

    // =========================================================================
    // UPDATE USER METHOD (UPDATE)
    // =========================================================================
    @Override
    public User UpdateUser(User user, MultipartFile file) {
        try {
            String PicUpdate = UpdateProfilePicture(user, file);
            if (PicUpdate != null) {
                System.out.println("Photo successfully saved for user: " + user.getProfilePic());
            }
            userRepository.findById(user.getId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
            return userRepository.save(user);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            System.out.println("Internal server error :" + e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "internal error" + e.getMessage());
        }
    }

    // =========================================================================
    // DELETE USER METHOD (DELETE)
    // =========================================================================
    @Override
    public User DeleteUser(Long id) {
        User user = null;
        try {
            user = userRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

            String profilePicName = user.getProfilePic();
            if (profilePicName != null && !profilePicName.isEmpty()) {
                try {
                    // Yahan bhi fixed path use ho raha hai
                    java.nio.file.Path imagePath = java.nio.file.Paths.get(UPLOAD_DIR).resolve(profilePicName);
                    boolean isDeleted = java.nio.file.Files.deleteIfExists(imagePath);

                    if (isDeleted) {
                        System.out.println("Success: User ki photo folder se delete kar di gayi.");
                    } else {
                        System.out.println("Note: Photo ka naam DB mein tha par file folder mein nahi mili.");
                    }
                } catch (Exception e) {
                    System.out.println("WARNING: User ki photo delete nahi ho payi: " + e.getMessage());
                }
            }

            userRepository.deleteById(id);
            System.out.println("Success: User database se successfully delete ho gaya.");

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            System.out.println("Internal server error :" + e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error: " + e.getMessage());
        }
        return user;
    }

    // =========================================================================
    // PATCH USER METHOD (PARTIAL UPDATE)
    // =========================================================================
    @Override
    public User PartialUpdate(User newUser) {
        try {
            User existingUser = userRepository.findById(newUser.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User nahi mila! Update nahi kiya ja sakta."));

            if (newUser.getName() != null) {
                existingUser.setName(newUser.getName());
            }
            if (newUser.getEmail() != null) {
                existingUser.setEmail(newUser.getEmail());
            }
            if (newUser.getAge() != null) {
                existingUser.setAge(newUser.getAge());
            }
            if (newUser.getProfilePic() != null) {
                existingUser.setProfilePic(newUser.getProfilePic());
            }

            return userRepository.save(existingUser);

        } catch (ResponseStatusException e) {
            throw e;
        } catch (DataIntegrityViolationException e) {
            System.out.println("Duplicate email error during patch: " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Update nahi ho paya: Yeh email pehle se kisi aur ka hai!");
        } catch (Exception e) {
            System.out.println("Patch karne mein error: " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "internal error: " + e.getMessage());
        }
    }
}