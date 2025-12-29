package bangbang.gourmet.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    private static final String SECURITY_SCHEME_NAME = "JWT";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .components(getComponents())
                .addSecurityItem(getSecurityRequirement());
    }

    private Info apiInfo() {
        return new Info()
                .title("Gourmet API")
                .description("맛집 사이트 Gourmet Swagger입니다.")
                .version("1.0");
    }

    private Components getComponents() {
        return new Components().addSecuritySchemes(SECURITY_SCHEME_NAME, createAPIKeyScheme());
    }

    private static SecurityRequirement getSecurityRequirement() {
        return new SecurityRequirement().addList(SECURITY_SCHEME_NAME);
    }

    private SecurityScheme createAPIKeyScheme(){
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");
    }
}
