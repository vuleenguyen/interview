package edu.mum.coffee.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import edu.mum.coffee.domain.Product;
import edu.mum.coffee.domain.ProductType;
import edu.mum.coffee.domain.User;
import edu.mum.coffee.service.ProductService;
import edu.mum.coffee.service.UserService;

@Controller
public class HomeController {
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private ProductService productService;
	
	@GetMapping({"/", "/index", "/home"})
	public String homePage(Model model, HttpServletRequest request) {
		List<Product> products = productService.getAllProduct();
		Map<ProductType,List<Product>> result = classifyProduct(products);

		List<Product> coffee = new ArrayList<>();
		coffee.addAll(result.get(ProductType.BREAKFAST));
		coffee.addAll(result.get(ProductType.LUNCH));
		coffee.addAll(result.get(ProductType.DINNER));
		
		model.addAttribute("coffee", coffee);
		model.addAttribute("tea", result.get(ProductType.TEA));
		
		return "home";
	}
	
	@Autowired
	@Qualifier("userSignUpValidator")
	private org.springframework.validation.Validator validator;
	
	@InitBinder
	private void initBinder(WebDataBinder binder) {
		binder.setValidator(validator);
	}
	
	@GetMapping({"/secure"})
	public String securePage() {
		return "secure";
	}
	
	@RequestMapping(value = "/signup", method = RequestMethod.GET)
	public String signUp(Model model) {
		model.addAttribute("user", new User());
		return "signUp";
	}
	

	@RequestMapping(value = "/login", method = RequestMethod.GET)
	public String login(@RequestParam(value = "error", required = false) String error,
			@RequestParam(value = "logout", required = false) String logout, Model model) {

		if (error != null) {
			model.addAttribute("error", "Invalid UserName And PassWord");
		}

		if (logout != null) {
			return "redirect:home";
		}
		return "login";
	}
	
	@RequestMapping(value = "/signup", method = RequestMethod.POST)
	public String register(@ModelAttribute("user") @Validated User user, BindingResult result, Model model) {
		if (!result.hasErrors()) {
			try {
				userService.save(user);
			} catch (Exception e) {
				model.addAttribute("error", e.getMessage());
				return "signUp";
			}
			return "redirect:/home";
		} else {
			model.addAttribute("user", user);
			return "signUp";
		}
	}
	
	private Map<ProductType,List<Product>> classifyProduct(List<Product> products) {
		List<Product> coffeeBF = new ArrayList<>();
		List<Product> coffeeLunch = new ArrayList<>();
		List<Product> coffeeDinner = new ArrayList<>();
		List<Product> tea = new ArrayList<>();
		for (Product product : products) {
			if (product.getProductType() == ProductType.BREAKFAST) {
				coffeeBF.add(product);
			} else if (product.getProductType() == ProductType.LUNCH) {
				coffeeLunch.add(product);
			} else if (product.getProductType() == ProductType.DINNER) {
				coffeeDinner.add(product);
			} else {
				tea.add(product);
			}
		}
		Map<ProductType, List<Product>> result = new HashMap<>();
		result.put(ProductType.BREAKFAST, coffeeBF);
		result.put(ProductType.LUNCH, coffeeLunch);
		result.put(ProductType.DINNER, coffeeDinner);
		result.put(ProductType.TEA, tea);
		return result;
	}
}
