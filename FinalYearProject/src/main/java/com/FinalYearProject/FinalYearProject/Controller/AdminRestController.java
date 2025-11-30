package com.FinalYearProject.FinalYearProject.Controller;

import com.FinalYearProject.FinalYearProject.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequestMapping("${app.version}/admin")
@RestController
public class AdminRestController {
    @Autowired
    private UserService userService;


    @PostMapping("/findUserById")
    public ResponseEntity<?> findUserById(@RequestBody Map<String ,Long> request){
        Long Id= request.get("id");
        try{
            return ResponseEntity.ok(
                    userService.getUserById(Id)
            );
        }
        catch (Exception e){
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                            Map.of(
                                    "message","invade Id try again ",
                                    "error",e.getMessage()
                            )
                    );
        }
    }

    @PostMapping("/findByEmail")
    public ResponseEntity<?> findByEmail(@RequestBody Map<String , String> requst){
       String Email= requst.get("email");
       try{
           return ResponseEntity.ok(
                   userService.findByEmail( Email)
           );
       }
       catch (Exception e){
           return ResponseEntity
                   .status(HttpStatus.BAD_REQUEST)
                   .body(
                           Map.of(
                               "message", "User email not found",
                                   "error", e.getMessage()
                           )
                   );
       }
    }

    @PostMapping("/listOfUserByRole")
    public ResponseEntity<?> listOfUserByRole(@RequestBody Map<String ,String > request){
        String Role=request.get("role");
        try{
            return ResponseEntity.ok(
                    userService.listOfUserByRole(Role)
            );
        }
        catch (Exception e){
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                            Map.of(
                                    "message","check role spelling ",
                                    "error", e.getMessage()
                            )
                    );
        }
    }
    @PostMapping("/getAllUsers")
    public ResponseEntity<?> getAllUsers(){
        try{
            return ResponseEntity.ok(
                    userService.getAllUsers()
            );
        }
        catch (Exception e){
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                            Map.of(
                                    "message","no users are saves in Db",
                                    "error",e.getMessage()
                            )
                    );
        }
    }
    @DeleteMapping("/deleteById")
    public ResponseEntity<?> deleteById( @RequestBody Map<String,Long> request){
        Long Id= request.get("id");
        try{
            String message=userService.deleteUserById(Id);
            return ResponseEntity.ok(
                    Map.of(
                            "states","successful",
                            "message",message
                    )
            );
        }
        catch (Exception e){
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                            Map.of(
                                    "message","Something went wrong",
                                    "error",e.getMessage()
                            )
                    );
        }
    }

    @PostMapping("/suspendUserById")
    public ResponseEntity<?> suspendUserById(@RequestBody Map<String,Long> request){
        Long Id= request.get("id");
        try {
            String Message= userService.suspendUserById(Id);
            return ResponseEntity.ok(
                    Map.of(
                            "states","successful",
                            "message", Message
                    )
            );
        }
        catch (UsernameNotFoundException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(
                            Map.of(
                                    "states","unsuccessful",
                                    "error", e.getMessage()
                            )
                    );
        }
        catch (Exception e){
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            Map.of(
                                    "states","unsuccessful",
                                    "message","something went wrong try again",
                                    "error", e.getMessage()
                            )
                    );
        }
    }
    @GetMapping("/test")
    public String test(){
        return "hii";
    }
}
