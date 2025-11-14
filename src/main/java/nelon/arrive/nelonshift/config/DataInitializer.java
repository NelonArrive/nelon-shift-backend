package nelon.arrive.nelonshift.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nelon.arrive.nelonshift.entities.Role;
import nelon.arrive.nelonshift.repositories.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {
	
	private final RoleRepository roleRepository;
	
	@Override
	public void run(String... args) {
		// Создаём базовые роли, если их нет
		createRoleIfNotExists("ROLE_USER");
		createRoleIfNotExists("ROLE_ADMIN");
		createRoleIfNotExists("ROLE_MODERATOR");
		
		log.info("Data initialization completed");
	}
	
	private void createRoleIfNotExists(String roleName) {
		if (roleRepository.findByName(roleName).isEmpty()) {
			Role role = new Role(roleName);
			roleRepository.save(role);
			log.info("Created role: {}", roleName);
		}
	}
}
