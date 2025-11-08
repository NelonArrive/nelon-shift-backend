package nelon.arrive.nelonshift.repositories;

import nelon.arrive.nelonshift.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
