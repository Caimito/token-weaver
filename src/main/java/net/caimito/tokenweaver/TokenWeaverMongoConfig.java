package net.caimito.tokenweaver;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@ConditionalOnClass(MongoRepository.class) // Ensures this config loads only if MongoRepository is available
@EnableMongoRepositories(basePackages = "net.caimito.tokenweaver")
public class TokenWeaverMongoConfig {
}
