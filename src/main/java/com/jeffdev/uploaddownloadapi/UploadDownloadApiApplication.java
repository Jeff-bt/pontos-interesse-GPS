package com.jeffdev.uploaddownloadapi;

import com.jeffdev.uploaddownloadapi.service.FileService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class UploadDownloadApiApplication  /*implements CommandLineRunner*/ {

	@Resource
	FileService fileService;

	public static void main(String[] args) {
		SpringApplication.run(UploadDownloadApiApplication.class, args);
	}

//	@Override
//	public void run(String... arg) throws Exception {
//		fileService.init();
//	}
}
