package br.com.pokertracker.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "tb_usuario")
@Data
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    @Column(unique = true)
    private String email;

    private String senha;

    private String role;

    // ==========================================================
    // MÉTODOS OBRIGATÓRIOS DO SPRING SECURITY (USER DETAILS)
    // ==========================================================

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Puxa a role do banco (ex: "ADMIN"). Se estiver vazio, define como "USER"
        String permissao = (this.role != null && !this.role.isEmpty()) ? this.role : "USER";
        return List.of(new SimpleGrantedAuthority("ROLE_" + permissao));
    }

    @Override
    public String getPassword() {
        return this.senha; // Retorna a senha criptografada do banco
    }

    @Override
    public String getUsername() {
        return this.email; // No nosso sistema, o "username" que o Spring procura é o e-mail
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}