package com.spring.backend.domain.user;

import com.spring.backend.domain.enums.UserRole;
import com.spring.backend.domain.shared.AggregateRoot;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class User extends AggregateRoot {

    private String username;
    private String password;
    private String name;
    private Integer age;
    private LocalDate birthDate;
    private String email;
    private String cardId;
    private String phone;
    private String address;
    private String gender;
    private UserRole role;
    private Boolean isActive;
    private String avatar;
    private List<Token> tokens;

    private User() {
        this.tokens = new ArrayList<>();
    }

    public static User reconstitute(Long id, String username, String email,
                                    String password, UserRole role, Boolean isActive) {
        User u = new User();
        u.id = id;
        u.username = username;
        u.email = email;
        u.password = password;
        u.role = role;
        u.isActive = isActive;
        return u;
    }

    public String getUsername()      { return username; }
    public String getPassword()      { return password; }
    public String getName()          { return name; }
    public Integer getAge()          { return age; }
    public LocalDate getBirthDate()  { return birthDate; }
    public String getEmail()         { return email; }
    public String getCardId()        { return cardId; }
    public String getPhone()         { return phone; }
    public String getAddress()       { return address; }
    public String getGender()        { return gender; }
    public UserRole getRole()        { return role; }
    public Boolean getIsActive()     { return isActive; }
    public String getAvatar()        { return avatar; }

    public List<Token> getTokens() {
        return Collections.unmodifiableList(tokens);
    }
}
