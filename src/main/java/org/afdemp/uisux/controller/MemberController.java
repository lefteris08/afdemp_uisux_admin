package org.afdemp.uisux.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import org.afdemp.uisux.domain.security.Role;
import org.afdemp.uisux.domain.security.UserRole;
import org.afdemp.uisux.utility.SecurityUtility;

import org.afdemp.uisux.domain.User;
import org.afdemp.uisux.service.UserRoleService;
import org.afdemp.uisux.service.UserService;
import org.afdemp.uisux.utility.ΜailConstructor;

@Controller
@RequestMapping("/member")
public class MemberController {
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private UserRoleService userRoleService;
	
	@Autowired
	private ΜailConstructor mailConstructor;
	
	@Autowired
	private JavaMailSender mailSender;
	
	@RequestMapping(value = "/add", method = RequestMethod.GET)
	public String addMember(Model model) {
		User user = new User();
		model.addAttribute(user);
		return "addMember";
	}
	
	@RequestMapping(value="/add", method = RequestMethod.POST)
	public String newMemberPost(
			HttpServletRequest request,
			@ModelAttribute("member") User member,
			BindingResult memberResult, 
			Model model
			) throws Exception{
		
		if (memberResult.hasErrors()) {
			model.addAttribute("insertFailure",true);
			return "addMember";
		}
		
		model.addAttribute("classActiveNewAccount", true);
		model.addAttribute("member", member);
		
		User user = userService.findByUsername(member.getUsername());
		
		if (user != null) {
			member = user;
			Role role = new Role();
			role.setRoleId(2);
			role.setName("ROLE_MEMBER");
			UserRole memberRole = new UserRole(member, role);
			member.getUserRoles().add(memberRole);
			userService.save(member);
			return "addMember";
		}else if (userService.findByEmail(member.getEmail()) != null) {
			model.addAttribute("emailExists", true);
			return "addMember";
		}
		
		String password = SecurityUtility.randomPassword();
		
		String encryptedPassword = SecurityUtility.passwordEncoder().encode(password);
		member.setPassword(encryptedPassword);
		
		Role role = new Role();
		role.setRoleId(2);
		role.setName("ROLE_MEMBER");
		Set<UserRole> userRoles = new HashSet<>();
		userRoles.add(new UserRole(member, role));
		userService.createUser(member, userRoles);
		
		String token = UUID.randomUUID().toString();
		userService.createPasswordResetTokenForUser(member, token);
		
		String appUrl = "http://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath();
		
		SimpleMailMessage email = mailConstructor.constructResetTokenEmail(appUrl, request.getLocale(), token, member, password);
		
		mailSender.send(email);
		
		model.addAttribute("emailSent", "true");
		
		return "addMember";
	}
	
	@RequestMapping("/memberList")
	public String memberList(Model model) {
		List<User> memberList = userRoleService.findAllMembers();
		
		model.addAttribute("memberList", memberList);

		return "memberList";
	}
	
	

//	
//	@RequestMapping("/productList")
//	public String productList(Model model) {
//		List<Product> productList = productService.findAll();
//		model.addAttribute("productList", productList);		
//		return "productList";
//	}
//	
//	@RequestMapping(value="/remove", method=RequestMethod.POST)
//	public String remove(
//			@ModelAttribute("id") String id, Model model
//			) {
//		productService.removeOne(Long.parseLong(id.substring(8)));
//		List<Product> productList = productService.findAll();
//		model.addAttribute("productList", productList);
//		return "redirect:/product/productList";
//	}
//	
//	
//	@RequestMapping("/productInfo")
//	public String productInfo(@RequestParam("id") Long id, Model model) {
//		Product product = productService.findOne(id);
//		model.addAttribute("product", product);
//		
//		return "productInfo";
//	}
//	

//	
//	@RequestMapping(value="/updateProduct", method=RequestMethod.POST)
//	public String updateProductPost(@ModelAttribute("product") Product product, BindingResult productResult,
//			@ModelAttribute("type") String type, BindingResult typeResult, Model model) {
//		
//		if (productResult.hasErrors() || typeResult.hasErrors()) {
//			model.addAttribute("updateSuccess",false);
//			return "redirect:/product/productInfo?id="+product.getId();
//		}
//
//		boolean success;
//		if (productService.createProduct(product, type) & ImageUtility.trySaveImage(product)) {
//				success = true;
//		}else{
//			success = false;
//		}
//		model.addAttribute("updateSuccess",success);
//		
//		return "redirect:/product/productInfo?id="+product.getId();
//	}
	


}