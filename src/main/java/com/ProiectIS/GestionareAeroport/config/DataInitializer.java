package com.ProiectIS.GestionareAeroport.config;

import com.ProiectIS.GestionareAeroport.model.AirlineCompany;
import com.ProiectIS.GestionareAeroport.model.AirportStaff;
import com.ProiectIS.GestionareAeroport.model.DiscountPolicy;
import com.ProiectIS.GestionareAeroport.repository.AirlineCompanyRepository;
import com.ProiectIS.GestionareAeroport.repository.AirportStaffRepository;
import com.ProiectIS.GestionareAeroport.repository.DiscountPolicyRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(AirlineCompanyRepository repository,
                                      AirportStaffRepository staffRepository,
                                      DiscountPolicyRepository discountPolicyRepository,
                                      PasswordEncoder passwordEncoder) {
        return args -> {
            if (repository.findByEmail("admin@companie.com").isEmpty()) {
                AirlineCompany admin = new AirlineCompany();
                admin.setCompanyId("COMP001");
                admin.setName("Compania Test");
                admin.setEmail("admin@companie.com");
                admin.setPasswordHash(passwordEncoder.encode("parola123"));
                repository.save(admin);
                System.out.println("Cont de test creat: admin@companie.com / parola123");
            }

            if (staffRepository.findByPersonalCode("STAFF123").isEmpty()) {
                AirportStaff staff = new AirportStaff();
                staff.setStaffId("STF001");
                staff.setName("Personal Aeroport Test");
                staff.setPersonalCode("STAFF123");
                staffRepository.save(staff);
                System.out.println("Cont personal aeroport creat: STAFF123");
            }

            if (discountPolicyRepository.count() == 0) {
                discountPolicyRepository.save(new DiscountPolicy());
                System.out.println("Politica de discount initializata: 5% tur-retur, 40% last-minute.");
            }
        };
    }
}
