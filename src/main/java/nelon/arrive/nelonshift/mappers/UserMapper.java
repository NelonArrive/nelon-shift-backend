package nelon.arrive.nelonshift.mappers;

import nelon.arrive.nelonshift.dto.UserDto;
import nelon.arrive.nelonshift.entity.User;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {
	
	UserDto toDto(User user);
	
	User toEntity(UserDto userDto);
	
	List<UserDto> toDtoList(List<User> users);
}
