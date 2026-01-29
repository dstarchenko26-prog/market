package ua.nulp.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ua_parser.Parser;
import java.io.IOException;

@Configuration
public class UaParserConfig {
    @Bean
    public Parser uaParser() throws IOException {
        return new Parser();
    }
}