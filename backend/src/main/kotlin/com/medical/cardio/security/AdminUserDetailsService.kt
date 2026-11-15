package com.medical.cardio.security

import com.medical.cardio.repository.AdminUserRepository
import com.medical.cardio.entity.AdminSection
import com.medical.cardio.entity.Role
import org.springframework.transaction.annotation.Transactional
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
    @Transactional(readOnly = true)
    override fun loadUserByUsername(email: String): UserDetails =
        adminUserRepository.findByEmail(email)
            ?.let { entity ->
                val authority = SimpleGrantedAuthority("ROLE_${entity.role}")
                val sections = if (entity.role == Role.SUPER_ADMIN) AdminSection.entries.toSet()
                    else entity.allowedSections.toSet()
                User.withUsername(entity.email)
                    .password(entity.passwordHash)
                    .authorities(listOf(authority) + sections.map { SimpleGrantedAuthority("SECTION_$it") })
                    .disabled(entity.isBlocked)
                    .build()
            }
            ?: throw UsernameNotFoundException("Admin user '$email' not found")


    fun authentication(userDetails: UserDetails) =
        UsernamePasswordAuthenticationToken(
            userDetails,
            null,
            userDetails.authorities
        )
}
