package com.kunal.image.data.generator;

import com.kunal.image.data.Role;
import com.kunal.image.data.entity.ImageEntity;
import com.kunal.image.data.entity.User;
import com.kunal.image.data.service.ImageEntityRepository;
import com.kunal.image.data.service.UserRepository;
import com.vaadin.exampledata.DataType;
import com.vaadin.exampledata.DataTypeWithRandomOptions;
import com.vaadin.exampledata.ExampleDataGenerator;
import com.vaadin.flow.spring.annotation.SpringComponent;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringComponent
public class DataGenerator {

    @Bean
    public CommandLineRunner loadData(PasswordEncoder passwordEncoder, UserRepository userRepository,
            ImageEntityRepository imageEntityRepository) {
        return args -> {
            Logger logger = LoggerFactory.getLogger(getClass());
            if (userRepository.count() != 0L) {
                logger.info("Using existing database");
                return;
            }
            int seed = 123;

            logger.info("Generating demo data");

            logger.info("... generating 2 User entities...");
            User user = new User();
            user.setName("John Normal");
            user.setUsername("john");
            user.setHashedPassword(passwordEncoder.encode("john"));
            user.setProfilePictureUrl(
                    "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&ixlib=rb-1.2.1&auto=format&fit=crop&w=128&h=128&q=80");
            user.setRoles(Collections.singleton(Role.USER));
            userRepository.save(user);
            User admin = new User();
            admin.setName("Emma Powerful");
            admin.setUsername("emma");
            admin.setHashedPassword(passwordEncoder.encode("emma"));
            admin.setProfilePictureUrl(
                    "https://images.unsplash.com/photo-1607746882042-944635dfe10e?ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&ixlib=rb-1.2.1&auto=format&fit=crop&w=128&h=128&q=80");
            admin.setRoles(Stream.of(Role.USER, Role.ADMIN).collect(Collectors.toSet()));
            userRepository.save(admin);
            logger.info("... generating 100 Image Entity entities...");
            ExampleDataGenerator<ImageEntity> imageEntityRepositoryGenerator = new ExampleDataGenerator<>(
                    ImageEntity.class, LocalDateTime.of(2022, 1, 20, 0, 0, 0));
            imageEntityRepositoryGenerator.setData(ImageEntity::setId, DataType.ID);
            imageEntityRepositoryGenerator.setData(ImageEntity::setTitle, DataType.BOOK_TITLE);
            imageEntityRepositoryGenerator.setData(ImageEntity::setImage, DataType.BOOK_IMAGE_URL);
            imageEntityRepositoryGenerator.setData(ImageEntity::setIsPublic, DataType.BOOLEAN_90_10);
            imageEntityRepositoryGenerator.setData(ImageEntity::setUsername, new DataTypeWithRandomOptions("/Users.txt"));

            imageEntityRepository.saveAll(imageEntityRepositoryGenerator.create(100, seed));

            logger.info("Generated demo data");
        };
    }

}