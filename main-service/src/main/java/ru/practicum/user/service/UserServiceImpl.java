package ru.practicum.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.User;
import ru.practicum.user.repo.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public List<UserDto> getAll(List<Long> ids, Integer from, Integer size) {
        List<User> users;
        validateSearchParameters(from, size);
        PageRequest pageRequest = PageRequest.of(from, size, Sort.by("id"));
        if (ids != null) {
            users = userRepository.findAllByIdIn(ids, pageRequest);
        } else {
            users = userRepository.findAll(pageRequest).toList();
        }
        return users.stream()
                .map(userMapper::toUserDto)
                .collect(Collectors.toList());
    }

    private void validateSearchParameters(int from, int size) {
        if (from < 0) {
            log.info("Параметр запроса 'from' должен быть больше или равен 0, указано значение {}", from);
            throw new ValidationException();
        } else if (size <= 0) {
            log.info("Параметр запроса 'size' должен быть больше 0, указано значение {}", size);
            throw new ValidationException();
        }
    }

    public UserDto create(NewUserRequest newUserRequest) {
        User user = userMapper.toUser(newUserRequest);
        validateToCreate(user);
        findUserByEmail(newUserRequest.getEmail());
        User savedUser = userRepository.save(user);
        log.info("Добавлен новый пользователь: {}", savedUser);
        return userMapper.toUserDto(user);
    }

    private void validateToCreate(User user) {
        String name = user.getName();
        String email = user.getEmail();
        if (name == null) {
            log.info("У пользователя {} не задано имя", user);
            throw new ValidationException();
        }
        if (name.isBlank()) {
            log.info("У пользователя {} имя состоит только из пробелов", user);
            throw new ValidationException();
        }
        if (name.length() < 2) {
            log.info("У пользователя {} длина имени меньше 2 символов", user);
            throw new ValidationException();
        }
        if (name.length() > 250) {
            log.info("У пользователя {} длина имени больше 250 символов", user);
            throw new ValidationException();
        }
        if (email == null) {
            log.info("У пользователя {} не задана электронная почта", user);
            throw new ValidationException();
        }
        if (!email.contains("@")) {
            log.info("У пользователя {} указана неверная электронная почта", user);
            throw new ValidationException();
        }
        if (email.length() < 6) {
            log.info("У пользователя {} длина электронной почты меньше 6 символов", user);
            throw new ValidationException();
        }
        if (email.length() > 254) {
            log.info("У пользователя {} длина электронной почты больше 254 символов", user);
            throw new ValidationException();
        }
        String[] emailParts = email.split("@");
        if (emailParts.length == 2) {
            if (emailParts[0].length() > 64) {
                log.info("У пользователя {} длина имени электронной почты больше 64 символов", user);
                throw new ValidationException();
            } else if (emailParts[1].length() > 63 && email.length() != 254) {
                log.info("У пользователя {} длина домена электронной почты больше 63 символов", user);
                throw new ValidationException();
            }
        }
    }

    private void findUserByEmail(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent()) {
            log.info("В базе уже есть пользователь с email {}", email);
            throw new ConflictException();
        }
    }

    public void deleteUser(long userId) {
        User user = findUser(userId);
        userRepository.delete(user);
        log.info("Удален пользователь {}", user);
    }

    private User findUser(long userId) {
        if (userId == 0) {
            throw new ValidationException();
        }
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            throw new NotFoundException();
        }
        return user.get();
    }

}