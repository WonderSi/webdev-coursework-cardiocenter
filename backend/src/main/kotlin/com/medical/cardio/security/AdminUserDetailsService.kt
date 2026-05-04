package com.medical.cardio.security

import com.medical.cardio.repository.AdminUserRepository
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class AdminUserDetailsService(
    private val adminUserRepository: AdminUserRepository
) : UserDetailsService {

    /*
        Поиск пользователя по логину
        Достаем роль и добавляем префикс
        Создание объекта UserDetails для Spring Security

        UsernameNotFoundException
    */
    override fun loadUserByUsername(email: String): UserDetails =
        adminUserRepository.findByEmail(email)
            ?.let { entity ->
                val authority = SimpleGrantedAuthority("ROLE_${entity.role}")
                User(
                    entity.email,
                    entity.passwordHash,
                    listOf(authority)
                )
            }
            ?: throw UsernameNotFoundException("Admin user '$email' not found")


    fun authentication(userDetails: UserDetails) =
        UsernamePasswordAuthenticationToken(
            userDetails,
            null,
            userDetails.authorities
        )
}
