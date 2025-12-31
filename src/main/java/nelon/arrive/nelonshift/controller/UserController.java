package nelon.arrive.nelonshift.controller;

import lombok.RequiredArgsConstructor;
import nelon.arrive.nelonshift.dto.UserDto;
import nelon.arrive.nelonshift.request.UpdateUserRequest;
import nelon.arrive.nelonshift.response.ApiResponse;
import nelon.arrive.nelonshift.response.JwtResponse;
import nelon.arrive.nelonshift.security.user.CustomUserDetails;
import nelon.arrive.nelonshift.services.interfaces.IUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/users")
public class UserController {
	
	private final IUserService userService;
	
	@GetMapping("/all")
	public ResponseEntity<ApiResponse> getAllUsers() {
		List<UserDto> users = userService.getAllUsers();
		return ResponseEntity.ok(new ApiResponse("Success", users));
	}
	
	@GetMapping("/{userId}")
	public ResponseEntity<ApiResponse> getUserById(@PathVariable UUID userId) {
		UserDto user = userService.getUserById(userId);
		return ResponseEntity.ok(new ApiResponse("Success", user));
	}
	
	@PutMapping("/{userId}")
	public ResponseEntity<ApiResponse> updateUser(
		@RequestBody UpdateUserRequest request,
		@PathVariable UUID userId
	) {
		UserDto user = userService.updateUser(request, userId);
		return ResponseEntity.ok(new ApiResponse("Update user success", user));
	}
	
	@DeleteMapping("/{userId}")
	public ResponseEntity<ApiResponse> deleteUser(@PathVariable UUID userId) {
		userService.deleteUser(userId);
		return ResponseEntity.ok(new ApiResponse("Delete user success", null));
	}
	
	@GetMapping("/me")
	public ResponseEntity<ApiResponse> getCurrentUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
		
		return ResponseEntity.ok(new ApiResponse("Success", new JwtResponse(userDetails.getId(), null)));
	}
}
