package com.walkmanx21.junit.service;

import com.walkmanx21.junit.dao.UserDao;
import com.walkmanx21.junit.dto.User;
import com.walkmanx21.junit.extension.ConditionalExtension;
import com.walkmanx21.junit.extension.GlobalExtension;
import com.walkmanx21.junit.extension.PostProcessingExtension;
import com.walkmanx21.junit.extension.UserServiceParamResolver;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Tag("fast")
@Tag("user")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith({
        UserServiceParamResolver.class,
        GlobalExtension.class,
        PostProcessingExtension.class,
        ConditionalExtension.class,
        MockitoExtension.class
})
public class UserServiceTest {

    private static final User IVAN = User.of(1, "Ivan", "123");
    private static final User PETR = User.of(2, "Petr", "111");

    @InjectMocks
    private UserService userService;

    @Mock
    private UserDao userDao;

    @BeforeAll
    static void init() {
        System.out.println("Before all ");
    }

    @BeforeEach
    void prepare() {
        System.out.println("Before each " + this);
//        this.userDao = Mockito.spy(new UserDao());
//        this.userService = new UserService(userDao);
    }


    @Test
    void shouldDeleteExistedUser() {
        userService.add(IVAN);
        Mockito.doReturn(true).when(userDao).delete(IVAN.getId());
//        Mockito.when(userDao.delete(IVAN.getId()))
//                .thenReturn(true);
//        Mockito.doReturn(true).when(userDao).delete(Mockito.any());
        boolean deleteResult = userService.delete(1);
        assertThat(deleteResult).isTrue();
    }


    @Test
    void usersEmptyIfNoUserAdded() {
        System.out.println("Test 1 " + this);
        var users = userService.getAll();
        assertTrue(users.isEmpty(), () -> "Users list should be empty");
    }

    @Test
    void usersSizeIfUserAdded() {
        System.out.println("Test 2 " + this);
        userService.add(IVAN, PETR);
        var users = userService.getAll();
        assertThat(users).hasSize(2);

    }


    @Test
    void usersConvertedToMapById() {
        userService.add(IVAN, PETR);
        Map<Integer, User> users = userService.getAllConvertedById();
        assertAll(
                () -> assertThat(users).containsKeys(IVAN.getId(), PETR.getId()),
                () -> assertThat(users).containsValues(IVAN, PETR)
        );
    }

    @AfterEach
    void clearUsers() {
        System.out.println("After each " + this);
    }

    @AfterAll
    static void closeConnectionPool() {
        System.out.println("After all ");
    }

    @Nested
    @DisplayName("Test user login functionality")
    @Tag("login")
    class LoginTests {
        @Test
        void loginSuccessIfUserExist() {
            userService.add(IVAN);
            Optional<User> maybeUser = userService.login(IVAN.getUsername(), IVAN.getPassword());
            assertThat(maybeUser).isPresent();
            maybeUser.ifPresent(user -> assertThat(user).isEqualTo(IVAN));
            maybeUser.ifPresent(user -> assertEquals(IVAN, user));
        }

        @Test
        void loginFailIfPasswordIsNotCorrect() {
            userService.add(IVAN);
            Optional<User> maybeUser = userService.login(IVAN.getUsername(), "1234");
            assertTrue(maybeUser.isEmpty());
        }

        @Test
        void loginFailIfUserIsNotExist() {
            userService.add(IVAN);
            Optional<User> maybeUser = userService.login("dummy", IVAN.getPassword());
            assertTrue(maybeUser.isEmpty());
        }

        @Test
        void throwExceptionIfUsernameOrPasswordIsNull() {
            assertAll(
                    () -> {
                        var exception = assertThrows(IllegalArgumentException.class, () -> userService.login("dummy", null));
                        assertThat(exception.getMessage()).isEqualTo("username or password is null");
                    },
                    () -> assertThrows(IllegalArgumentException.class, () -> userService.login(null, "dummy"))
            );
        }

        @ParameterizedTest
//        @ArgumentsSource()
//        @NullSource
////        @EmptySource
//        @ValueSource(strings = {
//                "Ivan", "Petr"
//        })
        @MethodSource("com.walkmanx21.junit.service.UserServiceTest#getArgumentsForLoginTests")
        void loginParameterizedTest(String username, String password, Optional<User> user) {
            userService.add(IVAN, PETR);
            Optional<User> maybeUser = userService.login(username, password);
            assertThat(maybeUser).isEqualTo(user);
        }
    }

    static Stream<Arguments> getArgumentsForLoginTests() {
        return Stream.of(
                Arguments.of("Ivan", "123", Optional.of(IVAN)),
                Arguments.of("Petr", "111", Optional.of(PETR)),
                Arguments.of("Ivan", "dummy", Optional.empty()),
                Arguments.of("dummy", "123", Optional.empty())
        );
    }
}
