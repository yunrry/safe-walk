// SwaggerConfig.java
package yys.safewalk.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SafeWalk API")
                        .description("관광 안전 지도 서비스 API")
                        .version("v1.0")
                        .contact(new Contact()
                                .name("SafeWalk Team")
                                .email("contact@safewalk.com")))
                .components(new Components()
                        .addSecuritySchemes("noauth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("basic")));
    }
}