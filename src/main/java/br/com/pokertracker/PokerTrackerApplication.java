package br.com.pokertracker;

import br.com.pokertracker.model.Usuario;
import br.com.pokertracker.repository.UsuarioRepository;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

@SpringBootApplication
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class PokerTrackerApplication {

	public static void main(String[] args) {
		SpringApplication.run(PokerTrackerApplication.class, args);
	}

	@Bean
	public CommandLineRunner criarAdmin(UsuarioRepository repository) {
		return args -> {
			if (repository.findByEmail("admin").isEmpty()) {
				Usuario admin = new Usuario();
				admin.setNome("ADMIN");
				admin.setEmail("admin");

				String senhaCriptografada = BCrypt.hashpw("123456", BCrypt.gensalt());
				admin.setSenha(senhaCriptografada);

				admin.setRole("ADMIN");
				repository.save(admin);
				System.out.println("Usuário ADMIN criado com sucesso e senha protegida!");
			}
		};
	}
}