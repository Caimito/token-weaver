package net.caimito.tokenweaver;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountPrincipalRepository extends MongoRepository<AccountPrincipal<?>, String> {

  Optional<AccountPrincipal<?>> findByEmail(String email);

  Optional<AccountPrincipal<?>> findByMagicId(String magicId);

}
