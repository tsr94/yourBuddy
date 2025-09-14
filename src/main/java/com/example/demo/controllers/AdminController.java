package com.example.demo.controllers;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.count.*;
import com.example.demo.entities.*;
import com.example.demo.loginCredentials.*;
import com.example.demo.services.*;
import jakarta.validation.Valid;

@Controller
public class AdminController {
	@Autowired
	private UserServices services;
	@Autowired
	private AdminServices adminServices;
	@Autowired
	private ProductServices productServices;	
	@Autowired
	private OrderServices orderServices;

	private String email;
	private User user;
	@PostMapping("/adminLogin")
	public String  getAllData(  @ModelAttribute("adminLogin") AdminLogin login, Model model)
	{
		String email=login.getEmail();
		String password=login.getPassword();
		if(adminServices.validateAdminCredentials(email, password))
		{
			return "redirect:/admin/services";
		}
		else {
			model.addAttribute("error", "Invalid email or password");
			return "Login";
		}

	}

	@PostMapping("/userLogin")
	public String userLogin(@ModelAttribute("userLogin") UserLogin login,
							Model model,
							HttpSession session) {

		email = login.getUserEmail();
		String password = login.getUserPassword();

		if (services.validateLoginCredentials(email, password)) {
			User user = this.services.getUserByEmail(email);

			// ✅ store user in session
			session.setAttribute("user", user);

			List<Orders> orders = this.orderServices.getOrdersForUser(user);
			model.addAttribute("orders", orders);
			model.addAttribute("name", user.getUname());

			return "BuyProduct";
		} else {
			model.addAttribute("error2", "Invalid email or password");
			return "Login";
		}
	}
	
	// Show the admin registration form
	@GetMapping("/adminRegister")
	public String showAdminRegisterForm(Model model) {
	    model.addAttribute("admin", new Admin());
	    return "Register_Admin";
	}

	// Handle admin registration form submission
	@PostMapping("/adminRegister")
	public String handleAdminRegister(@ModelAttribute("admin") Admin admin, Model model) {
	    // Check if admin with same email already exists
	    Admin existingAdmin = adminServices.getAdminByEmail(admin.getAdminEmail());
	    if (existingAdmin != null) {
	        model.addAttribute("error", "An admin with this email already exists.");
	        return "Register_Admin";
	    }

	    adminServices.addAdmin(admin);
	    model.addAttribute("success", "Admin registered successfully.");
	    model.addAttribute("admin", new Admin()); // reset form
	    return "Register_Admin";
	}
	@PostMapping("/product/search")
	public String seachHandler(@RequestParam("productName") String name,Model model)
	{

		Product product=this.productServices.getProductByName(name);
		if(product==null)
		{
			model.addAttribute("message", "SORRY...!  Product Unavailable");
			model.addAttribute("product", product);
			List<Orders> orders = this.orderServices.getOrdersForUser(user);
			model.addAttribute("orders", orders);
			return "BuyProduct";
		}
		List<Orders> orders = this.orderServices.getOrdersForUser(user);
		model.addAttribute("orders", orders);
		model.addAttribute("product", product);
		return "BuyProduct";

	} 
	
	@GetMapping("/register")
	public String showUserRegisterForm(Model model) {
	    model.addAttribute("user", new User());
	    return "Register_User"; // Your Thymeleaf page
	}

	@PostMapping("/register")
	public String handleUserRegister(@ModelAttribute("user") User user, Model model) {
	    User existing = services.getUserByEmail(user.getUemail());
	    if (existing != null) {
	        model.addAttribute("error", "User already exists with this email.");
	        return "Register_User";
	    }

	    services.addUser(user);
	    model.addAttribute("success", "User registered successfully.");
	    model.addAttribute("user", new User()); // Clear form
	    return "Register_User";
	}

	@GetMapping("/admin/services")
	public String returnBack(Model model)
	{
		List<User> users= this.services.getAllUser();
		List<Admin>admins=this.adminServices.getAll(); 
		List<Product>products=this.productServices.getAllProducts();
		List<Orders> orders = this.orderServices.getOrders();
		model.addAttribute("users",users);
		model.addAttribute("admins", admins);
		model.addAttribute("products", products);
		model.addAttribute("orders", orders);

		return "Admin_Page";
	}
	@GetMapping("/addAdmin")
	public String addAdminPage()
	{
		return "Add_Admin";
	}
	@PostMapping("addingAdmin")
	public String addAdmin( @ModelAttribute Admin admin)
	{

		this.adminServices.addAdmin(admin);
		return "redirect:/admin/services";

	}
	@GetMapping("/updateAdmin/{adminId}")
	public String update(@PathVariable("adminId") int id,Model model)
	{
		Admin admin = this.adminServices.getAdmin(id);
		model.addAttribute("admin", admin);
		return "Update_Admin";
	}
	@GetMapping("/updatingAdmin/{id}")
	public String updateAdmin(@ModelAttribute Admin admin,@PathVariable("id") int id)
	{
		this.adminServices.update(admin, id);
		return "redirect:/admin/services";
	}
	@GetMapping("/deleteAdmin/{id}")
	public String deleteAdmin(@PathVariable("id") int id)
	{
		this.adminServices.delete(id);
		return "redirect:/admin/services";
	}
	@GetMapping("/addProduct")
	public String addProduct()
	{
		return "Add_Product";
	}
	
	@GetMapping("/updateProduct/{productId}")
	public String updateProduct(@PathVariable("productId") int id,Model model)
	{
		Product product=this.productServices.getProduct(id);
		System.out.println(product);
		model.addAttribute("product", product);
		return "Update_Product";
	}

	@GetMapping("/addUser")
	public String addUser()
	{
		return "Add_User";
	}

	@GetMapping("/updateUser/{userId}")
	public String updateUserPage(@PathVariable("userId") int id,Model model)
	{
		User user = this.services.getUser(id);
		model.addAttribute("user", user);
		return "Update_User";
	}

	@PostMapping("/product/order")
	public String orderHandler(@ModelAttribute() Orders order,Model model)
	{
		double  totalAmount = Logic.countTotal(order.getoPrice(),order.getoQuantity());
		order.setTotalAmmout(totalAmount);
		order.setUser(user);
		Date d=new Date();
		order.setOrderDate(d);
		this.orderServices.saveOrder(order);
		model.addAttribute("amount",totalAmount);
		return "Order_success";
	}

	@GetMapping("/product/back")
	public String back(Model model)
	{
		List<Orders> orders = this.orderServices.getOrdersForUser(user);
		model.addAttribute("orders", orders);
		return "BuyProduct";
	}

}