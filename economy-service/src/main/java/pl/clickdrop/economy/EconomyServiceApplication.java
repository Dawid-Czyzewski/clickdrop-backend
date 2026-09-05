package pl.clickdrop.economy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import pl.clickdrop.economy.config.JwtProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class EconomyServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(EconomyServiceApplication.class, args);
	}

}
