package nelon.arrive.nelonshift.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken implements Serializable {
	
	@Serial
	private static final long serialVersionUID = 1L;
	
	private String token;
	private UUID userId;
	private String userEmail;
	private Instant expiryDate;
	private Instant createdAt;
	private String ipAddress;
	private String userAgent;
	
	public boolean isExpired() {
		return Instant.now().isAfter(this.expiryDate);
	}
}