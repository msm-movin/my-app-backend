package org.setup.mycrud.model;

import jakarta.persistence.*;
import lombok.Data;


@Entity()
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String password;
    @Column(unique = true, nullable = false)
    private String username;
    private String email;
    private String ProfilePic;
}
