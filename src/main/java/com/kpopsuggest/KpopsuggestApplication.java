package com.kpopsuggest;

import org.springframework.boot.SpringApplication;
import com.amazonaws.regions.Regions;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.*;

import Model.Song;
import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
@ComponentScan(basePackageClasses = SongDBController.class)
public class KpopsuggestApplication {


	public static void main(String[] args) {
		SpringApplication.run(KpopsuggestApplication.class, args);
	}

}
