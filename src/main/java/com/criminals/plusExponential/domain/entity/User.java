package com.criminals.plusExponential.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User extends BaseTimeEntity {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
//    @NotBlank
    private String username;


    @Column(nullable = false, unique = true)
//    @NotBlank(message = "이메일은 필수 항목입니다.")
//    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;


    @Column(nullable = false)
//    @NotBlank(message = "비밀번호는 필수 항목입니다.")
//    @JsonIgnore
    private String password;


    @Enumerated(EnumType.STRING)
//    @NotBlank(message = "역할은 필수 항목입니다.")
    private Role role;

    @OneToOne(mappedBy = "user")
    private UnmatchedPath unmatchedPath;

    @ManyToOne
    @JoinColumn(name="matched_path_id")
    private MatchedPath matchedPath;
}
