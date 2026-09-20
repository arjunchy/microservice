package com.api.ApiGateway.filter;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

@Component
@ConfigurationProperties(prefix = "gateway.public")
public class RouteValidator {

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private List<String> paths = List.of();

    public List<String> getPaths() {
        return paths;
    }

    public void setPaths(List<String> paths) {
        this.paths = paths;
    }

    public boolean isPublic(String path) {
        return paths.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

}