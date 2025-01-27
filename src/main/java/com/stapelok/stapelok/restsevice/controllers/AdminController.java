package com.stapelok.stapelok.restsevice.controllers;

import com.stapelok.stapelok.models.Order;
import com.stapelok.stapelok.models.PreOrder;
import com.stapelok.stapelok.models.Products;
import com.stapelok.stapelok.repositories.*;
import org.aspectj.weaver.ast.Or;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {



    //connect models repo and we can use db methods
    @Autowired
    private  ProductsRepository productsRepository;

    @Autowired
    private PreOrderRepository preOrderRepository;
    @Autowired
    private OrderRepository orderRepository;

    @GetMapping("/admin_main_page")
    public String getAdminMainPage(Model model){
        Iterable<Products> products=productsRepository.findAll();
        model.addAttribute("products",products);
        return "/admin_main_page";
    }

    @GetMapping("/post-edit/{id}")
    private String getEditPostPage(Model model,@PathVariable(value="id") long id){
        Optional<Products> products=productsRepository.findById(id);
        ArrayList<Products> arrProd=new ArrayList<>();
        products.ifPresent(arrProd::add);
        model.addAttribute("product",arrProd);
        return "admin_edit_prod";
    }

    @GetMapping("/add_prod")
    public  String getAddProductPage(Model model){
        return  "admin_add_prod";
    }

    @GetMapping("/fabric")
    private String getFabricPage(Model model){
        ArrayList<Products> products= (ArrayList<Products>) productsRepository.findAll();
        model.addAttribute("products",products);
        return "fabric_storage";
    }

    //form func to add new
    @PostMapping("/addprod")
    public String addProduct(@RequestParam("p_type") String p_type, @RequestParam String title, @RequestParam String description , @RequestParam String price,
                             @RequestParam String stocks, @RequestParam String country,
                             @RequestParam String composition, @RequestParam String width, @RequestParam String density,
                             @RequestParam("file1") MultipartFile file1, @RequestParam("file2") MultipartFile file2, @RequestParam("file3") MultipartFile file3) throws IOException {

        byte[] photo1=null;
        byte[] photo2=null;
        byte[] photo3=null;
        if(!file1.isEmpty()){
            photo1= file1.getBytes();
        }
        if(!file2.isEmpty()){
            photo2= file2.getBytes();
        }
        if(!file3.isEmpty()){
            photo3= file3.getBytes();
        }

        Date date=new Date();
        Products products=new Products(p_type,title,description,price,"0",stocks,"В наявності",date,country,composition,width,density,photo1,photo2,photo3);
        productsRepository.save(products);

        return  "redirect:/admin/add_prod";

    }
    @PostMapping("/post-edit/{id}")
    private String editProduct(@PathVariable(value="id") long id, @RequestParam("p_type") String p_type, @RequestParam String title, @RequestParam String description ,
                               @RequestParam String price, @RequestParam String stocks, @RequestParam String country,@RequestParam String p_w_sale,
                               @RequestParam String composition, @RequestParam String width, @RequestParam String density,
                               @RequestParam("file1") MultipartFile file1, @RequestParam("file2") MultipartFile file2, @RequestParam("file3") MultipartFile file3) throws IOException {
        Products product=productsRepository.findById(id).orElseThrow();
        byte[] photo1=null;
        byte[] photo2=null;
        byte[] photo3=null;
        if(!file1.isEmpty()){
            photo1= file1.getBytes();
            product.setImage1(photo1);
            System.out.println("file1 empty check"+file3.isEmpty());
        }
        if(!file2.isEmpty()){
            photo2= file2.getBytes();
            product.setImage2(photo2);
            System.out.println("file2 empty check"+file3.isEmpty());
        }
        if(!file3.isEmpty()){
            photo3= file3.getBytes();
            product.setImage3(photo3);
            System.out.println("file3 empty check"+file3.isEmpty());
        }
        product.setDescription(description);
        product.setWidth(width);
        product.setStocks(stocks);
        product.setComposition(composition);
        product.setCountry(country);
        product.setDensity(density);
        product.setPrice(price);
        product.setP_w_sale(p_w_sale);
        product.setTitle(title);
        product.setType(p_type);
        productsRepository.save(product);

        return "redirect:/admin/admin_main_page";
    }
    @GetMapping("/delete-photo/{id}/{numphoto}")
    private String deleteImage(@PathVariable(value = "id") long id, @PathVariable(value = "numphoto") int num_photo){
        Products products=productsRepository.findById(id).orElseThrow();
        switch (num_photo) {
            case 1 -> {
                products.setImage1(null);
            }
            case 2 -> {
                products.setImage2(null);
            }
            case 3 -> {
                products.setImage3(null);
            }
        }
        productsRepository.save(products);

        return "redirect:/admin/post-edit/"+id;
    }

    @GetMapping("/orders")
    public  String getOrders(Model model){
        ArrayList<Order> arrayListOrders= (ArrayList<Order>) orderRepository.findAll();
        ArrayList<Order> waitCallList=new ArrayList<>();
        ArrayList<Order> awaitShipmentList=new ArrayList<>();
        ArrayList<Order> deliverList=new ArrayList<>();
        ArrayList<Order> abolition=new ArrayList<>();
        for(int i=0;i<arrayListOrders.size();i++){
            if(arrayListOrders.get(i).getStatus().equals("Очікуйте дзвінка")){
                waitCallList.add(arrayListOrders.get(i));
            }else if(arrayListOrders.get(i).getStatus().equals("Підтвердженно")){
                awaitShipmentList.add(arrayListOrders.get(i));
            }else if(arrayListOrders.get(i).getStatus().equals("Відправленно")){
                deliverList.add(arrayListOrders.get(i));
            }else if(arrayListOrders.get(i).getStatus().equals("Відміна")){
                abolition.add(arrayListOrders.get(i));
            }
        }
        arrayListOrders.clear();
        arrayListOrders.addAll(waitCallList);
        arrayListOrders.addAll(awaitShipmentList);
        arrayListOrders.addAll(deliverList);
        arrayListOrders.addAll(abolition);
        model.addAttribute("orders",arrayListOrders);
        ArrayList<PreOrder> preOrderArrayList=(ArrayList<PreOrder>) preOrderRepository.findAll();
        model.addAttribute("products",preOrderArrayList);
        return "/orders";
    }
    @GetMapping("/order_details/{id}")
    public String getOrder_details(Model model,@PathVariable long id){
        Optional<Order> order=orderRepository.findById(id);
        ArrayList<Order> arrayList=new ArrayList<>();
        order.ifPresent(arrayList::add);
        System.out.println( order);
        model.addAttribute("order",arrayList);
        ArrayList<PreOrder> preOrderArrayList=preOrderRepository.getPreOrderByOrder_id(id);
        model.addAttribute("preOrderProducts",preOrderArrayList);
        ArrayList<Products> productsArrayList=new ArrayList<>();
        for(int i=0;i<preOrderArrayList.size();i++){
            Optional<Products> products=  productsRepository.findById(preOrderArrayList.get(i).getId_prod());
            productsArrayList.add(products.get());
        }
        model.addAttribute("products",productsArrayList);
        return "/order_details";
    }
    @PostMapping("/order_details/{id}")
    private String setOrderNewData(Model model,@PathVariable long id,@RequestParam(name = "status") String status,
                                   @RequestParam String first_name, @RequestParam String last_name,@RequestParam String middle_name,
                                   @RequestParam String address, @RequestParam String phone_num, @RequestParam(name="delivery") String delivery){
        Optional<Order> opOrder=orderRepository.findById(id);
        ArrayList<Order> arrayList=new ArrayList<>();
        opOrder.ifPresent(arrayList::add);
        Order order=arrayList.get(0);
        order.setStatus(status);
        order.setName(first_name);
        order.setLast_name(last_name);
        order.setMiddle_name(middle_name);
        order.setAddress(address);
        order.setPhone_num(phone_num);
        order.setDelivery(delivery);
        orderRepository.save(order);
        return getOrders(model);
    }



}
