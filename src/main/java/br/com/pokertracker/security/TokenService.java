package br.com.pokertracker.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class TokenService {

    // Em um sistema real em produção, essa chave fica escondida nas variáveis de ambiente!
    // Mas para o nosso desenvolvimento, vamos fixar uma aqui.
    private final String secret = "minha_chave_secreta_poker_tracker_2026";

    // Método que GERA a pulseira quando o usuário acerta e-mail e senha
    public String gerarToken(String emailUsuario) {
        try {
            Algorithm algoritmo = Algorithm.HMAC256(secret);
            return JWT.create()
                    .withIssuer("PokerTracker_API")
                    .withSubject(emailUsuario) // O "dono" do token será o e-mail do usuário
                    .withExpiresAt(gerarDataExpiracao()) // Token dura 2 horas
                    .sign(algoritmo);
        } catch (JWTCreationException exception) {
            throw new RuntimeException("Erro ao gerar token JWT", exception);
        }
    }

    // Método que LÊ a pulseira toda vez que o JS pede algum dado
    public String validarToken(String token) {
        try {
            Algorithm algoritmo = Algorithm.HMAC256(secret);
            return JWT.require(algoritmo)
                    .withIssuer("PokerTracker_API")
                    .build()
                    .verify(token)
                    .getSubject(); // Devolve o e-mail se o token for válido
        } catch (JWTVerificationException exception) {
            return ""; // Se for falso ou expirado, devolve vazio e a requisição é barrada
        }
    }

    // Define que a pessoa é deslogada automaticamente após 2 horas de inatividade
    private Instant gerarDataExpiracao() {
        return LocalDateTime.now().plusHours(2).toInstant(ZoneOffset.of("-03:00"));
    }
}