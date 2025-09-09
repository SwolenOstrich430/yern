package com.yern.model.user;

//import com.yern.model.provider.ServiceProvider;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="user_authentications")
@Getter
@Setter
public class UserAuthentication {

    public UserAuthentication(){}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;

    @JoinColumn
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @Column
    private String username;

    @Column
    private String password;

    public UserAuthentication(User user, String username, String password) {
        this.user = user;
        this.username = username;
        this.password = password;
    }
}
