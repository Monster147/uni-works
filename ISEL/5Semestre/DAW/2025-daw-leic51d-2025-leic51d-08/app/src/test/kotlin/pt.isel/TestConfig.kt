package pt.isel

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component
import pt.isel.daw.repo.TransactionManager
import pt.isel.daw.repo.mem.TransactionManagerInMem

@Component
class TestConfig {
    @Bean
    @Primary
    fun trxManagerInMem(): TransactionManager = TransactionManagerInMem()
}
