package bangbang.gourmet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class GourmetApplication {

	public static void main(String[] args) {
		SpringApplication.run(GourmetApplication.class, args);
	}

}
