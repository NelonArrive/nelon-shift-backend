package nelon.arrive.nelonshift.services.interfaces;

import nelon.arrive.nelonshift.dto.UserDto;
import nelon.arrive.nelonshift.entity.User;
import nelon.arrive.nelonshift.request.UpdateUserRequest;

import java.util.List;
import java.util.UUID;

public interface IUserService {
	List<UserDto> getAllUsers();
	
	UserDto getUserById(UUID userId);
	
	UserDto updateUser(UpdateUserRequest request, UUID userId);
	
	void deleteUser(UUID userId);
	
	User getAuthenticatedUser();
	
}
