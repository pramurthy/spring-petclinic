/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.samples.petclinic.system;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import co.elastic.apm.api.ElasticApm;
import co.elastic.apm.api.CaptureSpan;
import co.elastic.apm.api.Span;
import java.lang.String;

@Controller
class WelcomeController {

	Logger logger = LoggerFactory.getLogger(WelcomeController.class);

	@GetMapping("/")
	@CaptureSpan
	public String welcome(HttpSession session) {
		if (session.getAttribute("username") == null) {
			return "login";
		}
		else {
			Span span = ElasticApm.currentSpan();
			span.addLabel("_tag_user", String.valueOf(session.getAttribute("username")));
			logger.info("User:" + session.getAttribute("username") + " made the request GET /");
			logger.info("User:" + session.getAttribute("username") + " logged into Welcome Page");
			return "welcome";
		}
	}

}
