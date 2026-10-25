package com.collectto.api_collectto.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.collectto.api_collectto.application.usecases.user.ChangePasswordUseCase;
import com.collectto.api_collectto.application.usecases.user.CreateUserUseCase;
import com.collectto.api_collectto.application.usecases.user.DeleteUserUseCase;
import com.collectto.api_collectto.application.usecases.user.FetchCurrentUserInfoUseCase;
import com.collectto.api_collectto.application.usecases.user.FetchUserUseCase;
import com.collectto.api_collectto.application.usecases.user.UpdateUserUseCase;
import com.collectto.api_collectto.domain.ports.PasswordHasher;
import com.collectto.api_collectto.domain.ports.StorageProvider;
import com.collectto.api_collectto.domain.ports.UserFollowRepository;
import com.collectto.api_collectto.domain.ports.UserRepository;
import com.collectto.api_collectto.domain.shared.StorageUrlPaths;

@Configuration
public class UserConfig {

    @Bean
    public ChangePasswordUseCase changePasswordUseCase(UserRepository userRepository, PasswordHasher passwordHasher) {
        return new ChangePasswordUseCase(userRepository, passwordHasher);
    }

    @Bean
    public CreateUserUseCase createUserUseCase(UserRepository userRepository, PasswordHasher passwordHasher) {
        return new CreateUserUseCase(userRepository, passwordHasher);
    }

    @Bean
    public DeleteUserUseCase deleteUserUseCase(UserRepository userRepository) {
        return new DeleteUserUseCase(userRepository);
    }

    @Bean
    public FetchCurrentUserInfoUseCase fetchCurrentUserInfoUseCase(UserRepository userRepository) {
        return new FetchCurrentUserInfoUseCase(userRepository);
    }

    @Bean
    public FetchUserUseCase fetchUserUseCase(UserRepository userRepository, UserFollowRepository userFollowRepository) {
        return new FetchUserUseCase(userRepository, userFollowRepository);
    }

    @Bean
    public UpdateUserUseCase updateUserUseCase(UserRepository userRepository, StorageProvider storageProvider, StorageUrlPaths storageUrlPaths) {
        return new UpdateUserUseCase(userRepository, storageProvider, storageUrlPaths);
    }
}
