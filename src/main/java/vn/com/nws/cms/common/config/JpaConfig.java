package vn.com.nws.cms.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = {
                "vn.com.nws.cms.modules.auth.infrastructure.persistence.repository",
                "vn.com.nws.cms.modules.academic.infrastructure.persistence.repository"
        }
)
public class JpaConfig {
}
