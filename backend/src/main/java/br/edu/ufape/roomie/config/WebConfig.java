package br.edu.ufape.roomie.config;

import java.nio.file.Paths;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  @Value("${app.storage.upload-dir:uploads/imoveis}")
  private String uploadDir;

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    String uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize().toString();

    registry.addResourceHandler("/images/**").addResourceLocations("file:" + uploadPath + "/");
  }
}
