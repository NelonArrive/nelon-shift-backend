package nelon.arrive.nelonshift.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponse {
	
	private String accessToken;
	private String refreshToken;
	private String tokenType = "Bearer";
	private UUID id;
	private String email;
	private String name;
	private Set<String> roles;
	
	public JwtResponse(String accessToken, UUID id, String email, String name, Set<String> roles) {
		this.accessToken = accessToken;
		this.id = id;
		this.email = email;
		this.name = name;
		this.roles = roles;
	}
}