package org.wickedsource.budgeteer.service.user;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.TestPropertySource;
import org.wickedsource.budgeteer.persistence.project.ProjectEntity;
import org.wickedsource.budgeteer.persistence.project.ProjectRepository;
import org.wickedsource.budgeteer.persistence.user.*;
import org.wickedsource.budgeteer.service.UnknownEntityException;

import java.util.*;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestPropertySource(locations = "classpath:application.properties")
@EnableAutoConfiguration
class UserServiceTest {

    @InjectMocks
    private UserService service;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private VerificationTokenRepository verificationTokenRepository;
    @Mock
    private ForgotPasswordTokenRepository forgotPasswordTokenRepository;
    @Mock
    private PasswordHasher passwordHasher;
    @Mock
    private UserMapper mapper;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Test
    void testRegisterUser() throws Exception {
        service.registerUser("User", "", "Password");
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    void testDuplicateUsernameDuringRegistration() {
        when(userRepository.findByName("User")).thenReturn(null, new UserEntity());
        Assertions.assertThatThrownBy(() -> {
            service.registerUser("User", "", "Password");
            service.registerUser("User", "", "Password");
        }).isInstanceOf(UsernameAlreadyInUseException.class);
    }

    @Test
    void testLoginSuccess() throws Exception {
        when(passwordHasher.hash("password")).thenReturn("password");
        when(userRepository.findByNameOrMailAndPassword("user", "password")).thenReturn(createUserEntity());
        when(mapper.map(any(UserEntity.class))).thenCallRealMethod();

        User user = service.login("user", "password");

        Assertions.assertThat(user)
                .isNotNull();
    }

    @Test
    void testLoginFail() {
        Assertions.assertThatThrownBy(() -> service.login("user", "password"))
                .isInstanceOf(InvalidLoginCredentialsException.class);
    }

    @Test
    void testAddUserToProjectSuccess() {
        Optional<UserEntity> userEntityOptional = createUserEntityOptional();
        Optional<ProjectEntity> projectEntityOptional = Optional.of(createProjectEntity());
        when(userRepository.findById(1L)).thenReturn(userEntityOptional);
        when(projectRepository.findById(1L)).thenReturn(projectEntityOptional);

        service.addUserToProject(1L, 1L);

        Assertions.assertThat(userEntityOptional.get().getAuthorizedProjects())
                .isNotEmpty()
                .contains(projectEntityOptional.get());
        Assertions.assertThat(projectEntityOptional.get().getAuthorizedUsers())
                .isNotEmpty()
                .contains(userEntityOptional.get());
    }

    @Test
    void testAddUserToProjectFailProjectNotFound() {
        Assertions.assertThatThrownBy(() -> service.addUserToProject(1L, 1L))
                .isInstanceOf(UnknownEntityException.class)
                .hasMessage("Entity of type class org.wickedsource.budgeteer.persistence.project.ProjectEntity with id 1 does not exist!");
    }

    @Test
    void testAddUserToProjectFailUserNotFound() {
        Assertions.assertThatThrownBy(() -> {
            when(projectRepository.findById(anyLong())).thenReturn(Optional.of(createProjectEntity()));
            service.addUserToProject(1L, 1L);
        }).isInstanceOf(UnknownEntityException.class)
                .hasMessage("Entity of type class org.wickedsource.budgeteer.persistence.user.UserEntity with id 1 does not exist!");
    }

    @Test
    void testRemoveUserFromProjectSuccess() {
        Optional<UserEntity> userEntityOptional = createUserEntityOptional();
        Optional<ProjectEntity> projectEntityOptional = Optional.of(createProjectEntity());
        when(userRepository.findById(1L)).thenReturn(userEntityOptional);
        when(projectRepository.findById(1L)).thenReturn(projectEntityOptional);

        service.removeUserFromProject(1L, 1L);

        Assertions.assertThat(userEntityOptional.get().getAuthorizedProjects())
                .isEmpty();
        Assertions.assertThat(projectEntityOptional.get().getAuthorizedUsers())
                .isEmpty();
    }

    @Test
    void testRemoveUserFromProjectFailProjectNotFound() {
        Assertions.assertThatThrownBy(() -> service.removeUserFromProject(1L, 1L))
                .isInstanceOf(UnknownEntityException.class)
                .hasMessage("Entity of type class org.wickedsource.budgeteer.persistence.project.ProjectEntity with id 1 does not exist!");
    }

    @Test
    void testRemoveUserFromProjectFailUserNotFound() {
        Assertions.assertThatThrownBy(() -> {
            when(projectRepository.findById(anyLong())).thenReturn(Optional.of(createProjectEntity()));
            service.removeUserFromProject(1L, 1L);
        }).isInstanceOf(UnknownEntityException.class)
                .hasMessage("Entity of type class org.wickedsource.budgeteer.persistence.user.UserEntity with id 1 does not exist!");
    }

    @Test
    void testGetUsersNotInProject() {
        when(userRepository.findNotInProject(1L)).thenReturn(Arrays.asList(createUserEntity()));
        when(mapper.map(Arrays.asList(createUserEntity()))).thenCallRealMethod();
        when(mapper.map(any(UserEntity.class))).thenCallRealMethod();

        List<User> users = service.getUsersNotInProject(1L);

        Assertions.assertThat(users)
                .hasSize(1)
                .extracting(User::getName)
                .containsExactly("user");
    }

    @Test
    void testGetUsersInProject() {
        when(userRepository.findInProject(1L)).thenReturn(Arrays.asList(createUserEntity()));
        when(mapper.map(Arrays.asList(createUserEntity()))).thenCallRealMethod();
        when(mapper.map(any(UserEntity.class))).thenCallRealMethod();

        List<User> users = service.getUsersInProject(1L);

        Assertions.assertThat(users)
                .hasSize(1)
                .extracting(User::getName)
                .containsExactly("user");
    }

    @Test
    void testCheckPassword() {
        when(passwordHasher.hash("password")).thenReturn("password");
        when(passwordHasher.hash("PASSWORD")).thenReturn("PASSWORD");
        when(userRepository.findById(1L)).thenReturn(createUserEntityOptional());

        Assertions.assertThat(service.checkPassword(1L, "password"))
                .isTrue();
        Assertions.assertThat(service.checkPassword(1L, "PASSWORD"))
                .isFalse();
    }

    @Test
    void testResetPasswordMailNotFoundException() {
        when(userRepository.findByMail(any())).thenReturn(null);

        Assertions.assertThatThrownBy(() -> service.resetPassword("user@budgeteer.local"))
                .isInstanceOf(MailNotFoundException.class);
    }

    @Test
    void testResetPasswordMailNotVerifiedException() {
        UserEntity user = createUserEntity();
        user.setMailVerified(false);
        when(userRepository.findByMail("user@budgeteer.local")).thenReturn(user);

        Assertions.assertThatThrownBy(() -> service.resetPassword("user@budgeteer.local"))
                .isInstanceOf(MailNotVerifiedException.class);
    }

    @Test
    void testLoadUserToEdit() {
        Optional<UserEntity> userMockOptional = createUserEntityOptional();
        UserEntity userMock = userMockOptional.get();
        when(userRepository.findById(1L)).thenReturn(userMockOptional);

        EditUserData user = service.loadUserToEdit(1L);

        Assertions.assertThat(user.getId()).isEqualTo(userMock.getId());
        Assertions.assertThat(user.getMail()).isEqualTo(userMock.getMail());
        Assertions.assertThat(user.getName()).isEqualTo(userMock.getName());
        Assertions.assertThat(user.getPassword()).isEqualTo(userMock.getPassword());
    }

    @Test
    void testSaveUserUsernameAlreadyInUseException() {
        Optional<UserEntity> user = createUserEntityOptional();
        UserEntity user2 = createUserEntity();
        user2.setId(2L);
        user2.setName("user2");
        when(userRepository.findById(1L)).thenReturn(user);
        EditUserData editUserData = service.loadUserToEdit(1L);
        when(userRepository.findByName("user2")).thenReturn(user2);
        editUserData.setName("user2");

        Assertions.assertThatThrownBy(() -> service.saveUser(editUserData, false))
                .isInstanceOf(UsernameAlreadyInUseException.class);
    }

    @Test
    void testSaveUserMailAlreadyInUseException() {
        Optional<UserEntity> user = createUserEntityOptional();
        UserEntity user2 = createUserEntity();
        user2.setId(2L);
        user2.setMail("user2@budgeteer.local");
        when(userRepository.findById(1L)).thenReturn(user);
        EditUserData editUserData = service.loadUserToEdit(1L);
        when(userRepository.findByMail("user2@budgeteer.local")).thenReturn(user2);
        editUserData.setMail("user2@budgeteer.local");

        Assertions.assertThatThrownBy(() -> service.saveUser(editUserData, false))
                .isInstanceOf(MailAlreadyInUseException.class);
    }

    @Test
    void testSaveUser() throws MailAlreadyInUseException, UsernameAlreadyInUseException {
        Optional<UserEntity> user = createUserEntityOptional();
        when(userRepository.findById(1L)).thenReturn(user);
        EditUserData editUserData = service.loadUserToEdit(1L);
        editUserData.setName("user2");

        service.saveUser(editUserData, false);

        Assertions.assertThat(editUserData.getName())
                .isEqualTo(user.get().getName());
    }

    @Test
    void testCreateVerificationTokenForUser() {
        UserEntity user = createUserEntity();
        String uuid = UUID.randomUUID().toString();

        VerificationToken verificationToken = service.createVerificationTokenForUser(user, uuid);

        verify(verificationTokenRepository, times(1)).save(verificationToken);
    }

    @Test
    void testValidateVerificationTokenInvalid() {
        String uuid = UUID.randomUUID().toString();

        Assertions.assertThat(service.validateVerificationToken(uuid))
                .isEqualTo(-1);
    }

    @Test
    void testValidateVerificationTokenExpired() {
        String uuid = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken(createUserEntity(), uuid);
        Date date = new Date();
        date.setTime(verificationToken.getExpiryDate().getTime() - 90000000); // 25 hours
        verificationToken.setExpiryDate(date);
        when(verificationTokenRepository.findByToken(uuid)).thenReturn(verificationToken);

        Assertions.assertThat(service.validateVerificationToken(uuid))
                .isEqualTo(-2);
    }

    @Test
    void testValidateVerificationTokenValid() {
        String uuid = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken(createUserEntity(), uuid);
        when(verificationTokenRepository.findByToken(uuid)).thenReturn(verificationToken);

        Assertions.assertThat(service.validateVerificationToken(uuid))
                .isZero();
    }

    @Test
    void getUserByMailMailNotFoundException() {
        Assertions.assertThatThrownBy(() -> {
            when(userRepository.findByMail("user@budgeteer.local")).thenReturn(null);
            service.getUserByMail("user@budgeteer.local");
        }).isInstanceOf(MailNotFoundException.class);
    }

    @Test
    void getUserByMail() throws MailNotFoundException {
        when(userRepository.findByMail("user@budgeteer.local")).thenReturn(createUserEntity());

        UserEntity user = service.getUserByMail("user@budgeteer.local");

        Assertions.assertThat(user)
                .isNotNull();
    }

    @Test
    void getUserByIdUserIdNotFoundException() {
        Assertions.assertThatThrownBy(() -> {
            when(userRepository.findById(1L)).thenReturn(Optional.empty());
            service.getUserById(1L);
        }).isInstanceOf(UserIdNotFoundException.class);
    }

    @Test
    void getUserById() throws UserIdNotFoundException {
        when(userRepository.findById(1L)).thenReturn(createUserEntityOptional());
        UserEntity user = service.getUserById(1L);

        Assertions.assertThat(user)
                .isNotNull();
    }

    @Test
    void testCreateForgotPasswordTokenForUserWithOldToken() {
        UserEntity user = createUserEntity();
        String uuid = UUID.randomUUID().toString();
        ForgotPasswordToken oldForgotPasswordToken = new ForgotPasswordToken(user, uuid);
        when(forgotPasswordTokenRepository.findByUser(user)).thenReturn(oldForgotPasswordToken);
        ForgotPasswordToken newForgotPasswordToken = service.createForgotPasswordTokenForUser(user, uuid);

        verify(forgotPasswordTokenRepository, times(1)).delete(oldForgotPasswordToken);
        verify(forgotPasswordTokenRepository, times(1)).save(newForgotPasswordToken);
    }

    @Test
    void testValidateForgotPasswordTokenInvalid() {
        String uuid = UUID.randomUUID().toString();

        Assertions.assertThat(service.validateForgotPasswordToken(uuid))
                .isEqualTo(-1);
    }

    @Test
    void testValidateForgotPasswordTokenExpired() {
        UserEntity user = createUserEntity();
        String uuid = UUID.randomUUID().toString();
        ForgotPasswordToken forgotPasswordToken = new ForgotPasswordToken(user, uuid);
        Date date = new Date();
        date.setTime(forgotPasswordToken.getExpiryDate().getTime() - 90000000); // 25 hours
        forgotPasswordToken.setExpiryDate(date);
        when(forgotPasswordTokenRepository.findByToken(uuid)).thenReturn(forgotPasswordToken);

        Assertions.assertThat(service.validateForgotPasswordToken(uuid))
                .isEqualTo(-2);
    }

    @Test
    void testValidateForgotPasswordTokenValid() {
        UserEntity user = createUserEntity();
        String uuid = UUID.randomUUID().toString();
        ForgotPasswordToken forgotPasswordToken = new ForgotPasswordToken(user, uuid);
        when(forgotPasswordTokenRepository.findByToken(uuid)).thenReturn(forgotPasswordToken);

        Assertions.assertThat(service.validateForgotPasswordToken(uuid))
                .isZero();
    }

    @Test
    void testGetUserByForgotPasswordTokenNotNull() {
        String uuid = UUID.randomUUID().toString();
        ForgotPasswordToken forgotPasswordToken = new ForgotPasswordToken(createUserEntity(), uuid);
        when(forgotPasswordTokenRepository.findByToken(uuid)).thenReturn(forgotPasswordToken);

        UserEntity userResult = service.getUserByForgotPasswordToken(uuid);

        Assertions.assertThat(userResult)
                .isNotNull();
    }

    @Test
    void testGetUserByForgotPasswordTokenNull() {
        String uuid = UUID.randomUUID().toString();
        when(forgotPasswordTokenRepository.findByToken(uuid)).thenReturn(null);

        UserEntity userResult = service.getUserByForgotPasswordToken(uuid);

        Assertions.assertThat(userResult)
                .isNull();
    }

    @Test
    void testDeleteForgotPasswordToken() {
        String uuid = UUID.randomUUID().toString();
        ForgotPasswordToken forgotPasswordToken = new ForgotPasswordToken(createUserEntity(), uuid);
        when(forgotPasswordTokenRepository.findByToken(uuid)).thenReturn(forgotPasswordToken);

        service.deleteForgotPasswordToken(uuid);

        verify(forgotPasswordTokenRepository, times(1)).delete(forgotPasswordToken);
    }

    private UserEntity createUserEntity() {
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setName("user");
        user.setMail("user@budgeteer.local");
        user.setMailVerified(true);
        user.setPassword("password");
        user.setAuthorizedProjects(new ArrayList<>());
        return user;
    }

    private Optional<UserEntity> createUserEntityOptional() {
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setName("user");
        user.setMail("user@budgeteer.local");
        user.setMailVerified(true);
        user.setPassword("password");
        user.setAuthorizedProjects(new ArrayList<>());
        return Optional.of(user);
    }

    private ProjectEntity createProjectEntity() {
        ProjectEntity project = new ProjectEntity();
        project.setId(1L);
        project.setName("name");
        project.setAuthorizedUsers(new ArrayList<>());
        return project;
    }
}
