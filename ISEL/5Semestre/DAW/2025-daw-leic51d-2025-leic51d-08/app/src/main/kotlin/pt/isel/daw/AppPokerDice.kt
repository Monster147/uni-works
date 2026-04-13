package pt.isel.daw

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import pt.isel.daw.http.AuthenticatedUserArgumentResolver
import pt.isel.daw.http.AuthenticationInterceptor
import pt.isel.daw.repo.jdbc.TransactionManagerJdbc
import pt.isel.daw.repo.mem.TransactionManagerInMem
import java.time.Clock
import java.time.Duration

@Configuration
class PipelineConfigurer(
    val authenticationInterceptor: AuthenticationInterceptor,
    val authenticatedUserArgumentResolver: AuthenticatedUserArgumentResolver,
) : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(authenticationInterceptor)
    }

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(authenticatedUserArgumentResolver)
    }
}

@Configuration
@Profile("mem")
class InMemoryConfig {
    @Bean
    fun trxManager(): TransactionManagerInMem = TransactionManagerInMem()
}

@Configuration
@Profile("jdbc")
class JdbcConfig {
    @Bean
    fun trxManager(): TransactionManagerJdbc = TransactionManagerJdbc()
}

@SpringBootApplication
class AppPokerDice {
    @Bean
    fun passwordEncoder() = BCryptPasswordEncoder()

    @Bean
    fun tokenEncoder() = Sha256TokenEncoder()

    @Bean
    fun clock(): Clock = Clock.systemUTC()

    @Bean
    fun usersDomainConfig() =
        UsersDomainConfig(
            tokenSizeInBytes = 256 / 8,
            tokenTtl = Duration.ofHours(24),
            tokenRollingTtl = Duration.ofHours(1),
            maxTokensPerUser = 3,
        )
}

fun main() {
    runApplication<AppPokerDice>()
}
