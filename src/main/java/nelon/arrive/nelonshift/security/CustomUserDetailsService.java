package nelon.arrive.nelonshift.security;

import lombok.RequiredArgsConstructor;
import nelon.arrive.nelonshift.exceptions.ResourceNotFoundException;
import nelon.arrive.nelonshift.repositories.UserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
	private final UserRepository userRepository;
	
	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String email) throws ResourceNotFoundException {
		User user = userRepository.findByEmail(email)
			.orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
		
		return CustomUserDetails.build(user);
	}
}
