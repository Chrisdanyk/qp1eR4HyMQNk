package org.priere.service.security;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import org.priere.domain.security.UserEntity;
import org.priere.domain.security.UserRole;
import org.priere.domain.security.UserStatus;

/**
 * Creates some test users in fresh database.
 * 
 * TODO This class is temporary for test, only. Just delete this class
 * if you do not need the test users to be created automatically.
 *
 */
@Singleton
@Startup
public class TestUsersCreator {

    private static final Logger logger = Logger.getLogger(TestUsersCreator.class.getName());
    
    @Inject
    private UserService userService;
    
    @PostConstruct
    public void postConstruct() {
        
       if(userService.countAllEntries() == 0) {
           
            logger.log(Level.WARNING, "Creating test user 'admin' with password 'admin'.");
            UserEntity admin = new UserEntity();
            admin.setUsername("admin");
            admin.setPassword("admin");
            admin.setRoles(Arrays.asList(new UserRole[]{UserRole.Administrator}));
            admin.setStatus(UserStatus.Active);
            admin.setEmail("admin@domain.test");
            
            userService.save(admin);
            
            logger.log(Level.WARNING, "Creating test user 'registered' with password 'registered'.");
            UserEntity registeredUser = new UserEntity();
            registeredUser.setUsername("registered");
            registeredUser.setPassword("registered");
            registeredUser.setRoles(Arrays.asList(new UserRole[]{UserRole.Registered}));
            registeredUser.setStatus(UserStatus.Active);
            registeredUser.setEmail("registered@domain.test");
            
            userService.save(registeredUser);
            
        }
    }
}
