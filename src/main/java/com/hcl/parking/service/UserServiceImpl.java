package com.hcl.parking.service;

import java.time.LocalDate;
import java.time.Period;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hcl.parking.dto.LoginDetailsDto;
import com.hcl.parking.dto.LoginDto;
import com.hcl.parking.dto.UserDetailsDto;
import com.hcl.parking.dto.UserDto;
import com.hcl.parking.entity.Parking;
import com.hcl.parking.entity.Role;
import com.hcl.parking.entity.User;
import com.hcl.parking.exception.ParkingSlotException;
import com.hcl.parking.repository.ParkingRepository;
import com.hcl.parking.repository.RoleRepository;
import com.hcl.parking.repository.UserRepository;
import com.hcl.parking.util.ParkingConstants;

@Service
public class UserServiceImpl implements UserService {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

	@Autowired
	UserRepository userRepository;

	@Autowired
	ParkingRepository parkingRepository;

	@Autowired
	RoleRepository roleRepository;

	public UserDetailsDto register(UserDto userDto) {
		LOGGER.debug("UserServiceImpl register()");
		User user = userRepository.findByEmail(userDto.getEmail());
		List<Parking> paList = parkingRepository.findByUserId(null);
		UserDetailsDto userDetailsDto = new UserDetailsDto();
		if (user != null) {
			throw new ParkingSlotException(ParkingConstants.ALREADY_REGISTERED);
		} else {
			User u = new User();
			if (calculateAge(userDto.getCareerStartDate()) >= 15 && calculateAge(userDto.getJoiningDate()) >= 5) {
				BeanUtils.copyProperties(userDto, u);
				u.setRoleId(1);
				Base64.Encoder encoder = Base64.getEncoder();
				String password = encoder.encodeToString(userDto.getPassword().getBytes());
				u.setPassword(password);
				User registeredUser = userRepository.save(u);
				if (!paList.isEmpty()) {
					Parking p = paList.get(0);
					p.setUserId(registeredUser.getUserId());
					parkingRepository.save(p);
					userDetailsDto.setStatusCode(201);
					userDetailsDto.setMessage(ParkingConstants.SUCCESS_VIP);
				} else {
					throw new ParkingSlotException(ParkingConstants.PARKING_SLOT_NOT_AVAIABLE);
				}
			} else {
				BeanUtils.copyProperties(userDto, u);
				u.setRoleId(2);
				Base64.Encoder encoder = Base64.getEncoder();
				String password = encoder.encodeToString(userDto.getPassword().getBytes());
				u.setPassword(password);
				userRepository.save(u);
				userDetailsDto.setStatusCode(201);
				userDetailsDto.setMessage(ParkingConstants.SUCCESS_EMPLOYEE);
			}

		}
		return userDetailsDto;
	}

	public LoginDetailsDto login(LoginDto loginDto) {
		LOGGER.debug("UserServiceImpl login()");
		LoginDetailsDto loginResponseDto = null;
		Base64.Encoder encoder = Base64.getEncoder();
		String password = encoder.encodeToString(loginDto.getPassword().getBytes());
        System.out.println("password"+password);
		List<User> users = userRepository.findByemailAndPassword(loginDto.getEmail(), password);
		if (users.isEmpty()) {
			loginResponseDto = new LoginDetailsDto();
			loginResponseDto.setStatusCode(401);
			loginResponseDto.setMessage(ParkingConstants.LOGIN_FAILURE);
		} else {
			User user = users.get(0);
			Optional<Role> role = roleRepository.findById(user.getRoleId());
			loginResponseDto = new LoginDetailsDto();
			loginResponseDto.setUserId(user.getUserId());
			loginResponseDto.setRoleType(role.get().getRoleType());
			loginResponseDto.setStatusCode(200);
			loginResponseDto.setMessage(ParkingConstants.LOGIN_SUCCESS);
			
		}
		return loginResponseDto;
	}

	public int calculateAge(LocalDate birthDate) {
		LocalDate todayDate = LocalDate.now();
		Period p = Period.between(birthDate, todayDate);
		return p.getYears();

	}
}