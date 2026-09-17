package pt.ipma.recrutamento.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pt.ipma.recrutamento.domain.enums.Role;

import java.time.LocalDateTime;

@Entity
@Table(name = "app_user")
@Getter
@Setter
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private Role role;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "create_date", nullable = false)
    private LocalDateTime createDate = LocalDateTime.now();
}
