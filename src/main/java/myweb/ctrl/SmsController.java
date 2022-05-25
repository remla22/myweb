package myweb.ctrl;

import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import myweb.data.Sms;

@Controller
@RequestMapping(path = "/sms")
public class SmsController {

	private String modelHost;

	private RestTemplateBuilder rest;

	private HelloWorldController hw;

	public SmsController(RestTemplateBuilder rest, Environment env, HelloWorldController hw) {
		this.rest = rest;
		this.hw = hw;
		modelHost = env.getProperty("MODEL_HOST");
	}

	@GetMapping("/")
	public String index(Model m) {
		m.addAttribute("hostname", modelHost);
		return "sms/index";
	}

	@PostMapping("/")
	@ResponseBody
	public Sms predict(@RequestBody Sms sms) {
		hw.addPrediction();

		sms.result = getPrediction(sms);

		if (isCorrect(sms.guess, sms.result)) {
			hw.addCorrect();
		}

		return sms;
	}

	private static boolean isCorrect(String a, String b) {
		a = a.toLowerCase().trim();
		b = b.toLowerCase().trim();
		return a.equals(b);
	}

	private String getPrediction(Sms sms) {
		try {
			var url = new URI(modelHost + "/predict");
			var c = rest.build().postForEntity(url, sms, Sms.class);
			return c.getBody().result.trim();
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}
}