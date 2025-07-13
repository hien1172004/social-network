package backend.example.mxh.security;

import backend.example.mxh.security.PostSecurity;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import static org.mockito.Mockito.mock;

@TestConfiguration
@EnableMethodSecurity
public class PostsControllerTestConfig {

    @Bean
    @Primary
    public PostSecurity postSecurity() {
        return mock(PostSecurity.class);
    }
} 