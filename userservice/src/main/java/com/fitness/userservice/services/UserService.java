package com.fitness.userservice.services;


import com.fitness.userservice.dto.RequestDTO;
import com.fitness.userservice.dto.ResponseDto;
import com.fitness.userservice.models.User;
import com.fitness.userservice.repository.UserRepo;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {
    private UserRepo userRepo;

    public ResponseDto register(RequestDTO requestDTO) {
        if (userRepo.existsByEmail(requestDTO.getEmail())){
            throw new RuntimeException("user already exist");
        }
        User user=new User();
        user.setEmail(requestDTO.getEmail());
        user.setPassword(requestDTO.getPassword());
        user.setFirstName(requestDTO.getFirstName());
        user.setLastName(requestDTO.getLastName());
        User savedUser=userRepo.save(user);
        ResponseDto responseDto=new ResponseDto();
        responseDto.setId(savedUser.getId());
        responseDto.setEmail(savedUser.getEmail());
        responseDto.setPassword(savedUser.getPassword());
        responseDto.setFirstName(savedUser.getFirstName());
        responseDto.setLastName(savedUser.getLastName());
        responseDto.setCreatedAt(savedUser.getCreatedAt());
        responseDto.setUpdatedAt(savedUser.getUpdatedAt());
        return responseDto;
    }

    public ResponseDto getUserById(String userId) {
        User user=userRepo.findById(userId).orElseThrow(() -> new RuntimeException("user not found exeption"));
        ResponseDto responseDto=new ResponseDto();
        responseDto.setId(user.getId());
        responseDto.setEmail(user.getEmail());
        responseDto.setPassword(user.getPassword());
        responseDto.setFirstName(user.getFirstName());
        responseDto.setLastName(user.getLastName());
        responseDto.setCreatedAt(user.getCreatedAt());
        responseDto.setUpdatedAt(user.getUpdatedAt());
        return responseDto;
    }


}
