package com.example.demo;


import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

@Controller
public class HomeController {

    @Autowired
    MessageRepository messageRepository;

    @Autowired
    CloudinaryConfig cloudc;

    @Autowired
    private UserService userService;

    @Autowired
    UserRepository userRepository;

    @RequestMapping("/")
    public String listMessages(Model model){

        model.addAttribute("messages", messageRepository.findAll());
        return "list";
    }

    public Author getCurrentUser(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByUsername(auth.getName());
    }

    @GetMapping("/add")
    public String messageForm(Model model){
        model.addAttribute("message", new Message());
        return "messageform";
    }

    @PostMapping("/process")
    public String processForm(@Valid Message message, @RequestParam("file") MultipartFile file, BindingResult result){
        if (result.hasErrors() || file.isEmpty()){
            return "messageform";
        }

        try{
            Map uploadResult = cloudc.upload(file.getBytes(),
                    ObjectUtils.asMap("resourcetype", "auto"));
            message.setImage(uploadResult.get("url").toString());
            Author currentUser = getCurrentUser();
            message.setAuthor(currentUser); //get logged in username
            message.setPosteddate(new Date());
            currentUser.addMessage(message);
            // Add message to user
            messageRepository.save(message);
        } catch (IOException e) {
            e.printStackTrace();
            return "messageform";
        }
        return "redirect:/";
    }

    @RequestMapping("/messages/{id}")
    public String showCourse(@PathVariable("id") long id, Model model){
        model.addAttribute("message", messageRepository.findOne(id));
        return "show";
    }

    @RequestMapping("/edit/{id}")
    public String editMessage(@PathVariable("id") long id, Model model){
        model.addAttribute("message", messageRepository.findOne(id));
        return "messageform";
    }

    @RequestMapping("/delete/{id}")
    public String delMessage(@PathVariable("id") long id){
        messageRepository.delete(id);
        return "redirect:/";
    }


    @RequestMapping("/login")
    public String login(){
        return "login";
    }


    @RequestMapping(value = "/register", method = RequestMethod.GET)
    public String showRegistrationPage(Model model){
        model.addAttribute("user", new Author());
        return "registration";
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public String processRegistrationPage(@Valid @ModelAttribute("user") Author user,
                                          BindingResult result,
                                          Model model) {
        model.addAttribute("user", user);
        if (result.hasErrors()){
            return "registration";
        } else {
            userService.saveUser(user);
            model.addAttribute("message", "user Account Successfully Created");
        }
        return "redirect:/";
    }


}
