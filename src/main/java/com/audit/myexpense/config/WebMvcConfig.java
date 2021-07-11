/* Copyright (C) 2021 Manikandan Narasimhan - All Rights Reserved
 * You may use, distribute and modify this code.
 */

package com.audit.myexpense.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author Manikandan Narasimhan
 *
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {

		registry.addResourceHandler("/jquery/**") //
				.addResourceLocations("classpath:/META-INF/resources/webjars/jquery/3.3.1-1/");

		registry.addResourceHandler("/bootstrap/**") //
				.addResourceLocations("classpath:/META-INF/resources/webjars/bootstrap/4.1.1/");
		
	}
}
