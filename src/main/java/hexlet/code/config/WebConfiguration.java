package hexlet.code.config;

import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

@Configuration
@Slf4j
public class WebConfiguration implements WebMvcConfigurer {

    private final String baseUrl;

    public WebConfiguration(@Value("${base-url}") String baseUrl) {
        log.info("Initializing WebConfiguration with baseApiPath: {}", baseUrl);
        this.baseUrl = baseUrl;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        log.info("Adding resource handlers");
        registry
                .addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/static/");

        registry
                .addResourceHandler("/*.*")
                .addResourceLocations("classpath:/static/");

        registry
                .addResourceHandler("/", "/**")
                .setCachePeriod(0)
                .addResourceLocations("classpath:/static/index.html")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(@NotNull String resourcePath, @NotNull Resource location) {
                        if (resourcePath.startsWith(baseUrl) || resourcePath.startsWith(baseUrl.substring(1))) {
                            return null;
                        }
                        return location.exists() && location.isReadable() ? location : null;
                    }
                });
    }
}
