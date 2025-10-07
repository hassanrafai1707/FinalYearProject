package com.FinalYearProject.FinalYearProject.Domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Getter
@Setter
@Entity
@Table(name = "Conformation")
public class Conformation {
    @Id
    @SequenceGenerator(
            name= "ConformationSequence",
            sequenceName = "ConformationSequence",
            allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE,generator="ConformationSequence")
    public Long Id;
    public String token;
    @Temporal(TemporalType.TIMESTAMP)
    @CreatedDate
    public LocalDateTime createDate;
    @OneToOne(targetEntity = User.class,fetch = FetchType.EAGER)
    @JoinColumn(nullable = false,name = "User_Id")
    private User user;

    public Conformation(User user) {
        this.user = user;
        this.createDate=LocalDateTime.now();
        this.token= UUID.randomUUID().toString();
    }

    public User getUser() {
        return user;
    }

    public String getToken() {
        return token;
    }
}
