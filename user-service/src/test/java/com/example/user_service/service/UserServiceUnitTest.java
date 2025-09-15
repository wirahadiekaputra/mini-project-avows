package com.example.user_service.service;

import com.example.user_service.model.User;
import com.example.user_service.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceUnitTest {

    @Mock
    UserRepository repo;

    @InjectMocks
    UserService service;

    @Test
    void getActiveUsers_filtersOnlyActive() {
        User a = User.builder().id(1L).name("A").email("a@x.com").active(true).createdAt(new Date()).build();
        when(repo.findByActiveTrue()).thenReturn(List.of(a));

        var res = service.getActiveUsers();

        assertThat(res).hasSize(1);
        assertThat(res.get(0).isActive()).isTrue();
        verify(repo).findByActiveTrue();
    }

    @Test
    void getUsersByEmailDomain_delegatesToRepo() {
        when(repo.findByEmailDomain("%@example.com")).thenReturn(List.of());

        service.getUsersByEmailDomain("example.com");

        verify(repo).findByEmailDomain("%@example.com");
    }

    @Test
    void create_and_getById_and_getAll() {
        User u = User.builder().id(5L).name("X").email("x@x.com").active(true).build();
        when(repo.save(u)).thenReturn(u);
        when(repo.findById(5L)).thenReturn(Optional.of(u));
        when(repo.findAll()).thenReturn(List.of(u));

        var saved = service.create(u);
        assertThat(saved).isEqualTo(u);

        var fetched = service.getUserById(5L);
        assertThat(fetched).isEqualTo(u);

        var all = service.getAllUsers();
        assertThat(all).contains(u);
    }
}
