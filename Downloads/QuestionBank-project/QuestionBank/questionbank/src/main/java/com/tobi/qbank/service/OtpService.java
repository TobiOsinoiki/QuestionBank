package com.tobi.qbank.service;

import com.tobi.qbank.entity.Otp;
import com.tobi.qbank.repository.OtpRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Random;

	@Service
	public class OtpService {

	    @Autowired
	    private OtpRepository otpRepository;

	    private final Random random = new Random();

	    public String generateOtp() {
	        return String.format("%06d", random.nextInt(1000000));
	    }

	    @Transactional
	    public void saveOtp(String email, String code) {
	        otpRepository.deleteByEmail(email);
	        otpRepository.save(new Otp(email, code));
	    }

	    @Transactional
	    public boolean verifyOtp(String email, String code) {
	        Optional<Otp> optionalOtp = otpRepository.findByEmailAndCode(email, code);

	        if (optionalOtp.isPresent()) {
	            Otp otp = optionalOtp.get();

	            if (!otp.isExpired()) {
	                otpRepository.deleteByEmail(email);
	                return true;
	            }
	        }

	        return false;
	    }
	}