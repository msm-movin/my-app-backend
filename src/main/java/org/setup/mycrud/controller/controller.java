package org.setup.mycrud.controller;

import org.setup.mycrud.model.User;
import org.setup.mycrud.service.imp.UserServiceImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
public class controller {
    @Autowired
    private UserServiceImp userServiceImp;

    @PostMapping(value = "/signup", consumes = {"multipart/form-data"})
    public ResponseEntity<?> createUser(@ModelAttribute User user, @RequestParam(value = "file", required = false) MultipartFile file) {
        return new ResponseEntity<>(userServiceImp.UserSave(user, file), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@ModelAttribute User user) {
        return new ResponseEntity<>(userServiceImp.Login(user), HttpStatus.OK);
    }


    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        User user = userServiceImp.GetUserById(id);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }


    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        // PageRequest.of(page, size) बैकएंड को बताएगा कि कौन सा पेज और कितना डेटा चाहिए
        List<User> users = userServiceImp.GetAllUsers(page, size);

        if (users.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(users, HttpStatus.OK);
    }


    @PutMapping(value = "/update", consumes = {"multipart/form"})
    public ResponseEntity<?> updateUser(@ModelAttribute User user, @RequestParam(value = "file", required = false) MultipartFile file) {
        return new ResponseEntity<>(userServiceImp.UpdateUser(user, file), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        return new ResponseEntity<>(userServiceImp.DeleteUser(id), HttpStatus.OK);
    }

    // =========================================================================
    // PATCH USER API (PARTIAL UPDATE)
    // =========================================================================
    @PatchMapping("/patch")
    public ResponseEntity<?> patchUser(@RequestBody User user) {
        // Service layer ko call kiya aur status 200 OK ke sath response bheja
        return new ResponseEntity<>(userServiceImp.PartialUpdate(user), HttpStatus.OK);
    }

}


