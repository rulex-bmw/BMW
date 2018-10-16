package com.rulex.bsb;

import com.rulex.bsb.pojo.RulexBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BsbApplication {

	public static void main(String[] args) {
		SpringApplication.run(BsbApplication.class, args);
		RulexBean.One.newBuilder().setAge(1).setBuy("买手机").setTall(180).setUsername("zzf").build();
	}
}
