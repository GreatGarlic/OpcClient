package com.opc.client.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.Contact;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.List;
import java.util.function.Predicate;

import static com.google.common.collect.Lists.newArrayList;
import static springfox.documentation.builders.PathSelectors.regex;

@Configuration
public class SwaggerConfig {
    @Bean
    public Docket createRestApi() {

        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .paths(PathSelectors.regex("/error.*").negate())
                .build()
                .securitySchemes(newArrayList(new ApiKey("Authorization", "Authorization", "header")))
                .securityContexts(newArrayList(securityContexts()));
    }

    private List<SecurityContext> securityContexts() {
        return newArrayList(
                SecurityContext.builder()
                        .securityReferences(defaultAuth())
                        .forPaths(securityPaths())
                        .build()
        );
    }


    private List<SecurityReference> defaultAuth() {
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = new AuthorizationScope("global", "accessEverything");
        return newArrayList(new SecurityReference("Authorization", authorizationScopes));
    }

    private Predicate<String> securityPaths() {
        return regex("/api.*").and(regex("/login").negate());
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("NandCloud Restful APIs")
                .description("接口说明与调试界面")
                .contact(new Contact("NandCloud.com", "", ""))
                .version("1.0")
                .build();
    }
}
