package nelon.arrive.nelonshift.security.oauth2;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nelon.arrive.nelonshift.entities.Role;
import nelon.arrive.nelonshift.entities.User;
import nelon.arrive.nelonshift.repositories.RoleRepository;
import nelon.arrive.nelonshift.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
	
	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;
	
	@Override
	public OAuth2User loadUser(
		OAuth2UserRequest userRequest
	) throws OAuth2AuthenticationException {
		OAuth2User oauth2User = super.loadUser(userRequest);
		
		String provider = userRequest.getClientRegistration().getRegistrationId();
		String email = oauth2User.getAttribute("email");
		String name = oauth2User.getAttribute("name");
		
		if (email == null) {
			throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
		}
		
		log.info("OAuth2 login: provider={}, email={}", provider, email);
		
		// Ищем или создаём пользователя
		User user = userRepository.findByEmail(email)
			.orElseGet(() -> registerNewOAuth2User(email, name, provider));
		
		// Возвращаем OAuth2User с нашими данными
		return new CustomOAuth2User(oauth2User, user);
	}
	
	private User registerNewOAuth2User(String email, String name, String provider) {
		log.info("Registering new OAuth2 user: email={}, provider={}", email, provider);
		
		// Создаём пользователя
		User user = User.builder()
			.email(email)
			.name(name != null ? name : email.split("@")[0])
			.password(passwordEncoder.encode(UUID.randomUUID().toString())) // Случайный пароль
			.build();
		
		// Назначаем роль USER
		Role userRole = roleRepository.findByName("ROLE_USER")
			.orElseThrow(() -> new RuntimeException("Role ROLE_USER not found"));
		
		Set<Role> roles = new HashSet<>();
		roles.add(userRole);
		user.setRoles(roles);
		
		return userRepository.save(user);
	}
	
}
