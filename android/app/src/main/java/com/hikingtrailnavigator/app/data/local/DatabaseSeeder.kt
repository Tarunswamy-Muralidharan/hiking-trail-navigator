package com.hikingtrailnavigator.app.data.local

import com.hikingtrailnavigator.app.data.local.entity.toDomain
import com.hikingtrailnavigator.app.data.repository.EmergencyContactRepository
import com.hikingtrailnavigator.app.data.repository.TrailRepository
import com.hikingtrailnavigator.app.data.repository.UserRepository
import com.hikingtrailnavigator.app.domain.model.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseSeeder @Inject constructor(
    private val trailRepository: TrailRepository,
    private val emergencyContactRepository: EmergencyContactRepository,
    private val userRepository: UserRepository
) {
    suspend fun seedIfEmpty() {
        // Seed trails
        trailRepository.insertTrails(SeedData.trails)

        // Seed danger zones
        trailRepository.insertDangerZones(SeedData.dangerZones)

        // Seed no-coverage zones
        trailRepository.insertNoCoverageZones(SeedData.noCoverageZones)

        // Seed default emergency contacts (only if none exist)
        val contactCount = emergencyContactRepository.getContactCount()
        if (contactCount == 0) {
            SeedData.defaultContacts.forEach { contact ->
                emergencyContactRepository.addContact(contact.toDomain())
            }
        }

        // Seed default users (UML: User hierarchy)
        if (userRepository.getUserByEmail("hiker@demo.com") == null) {
            userRepository.createUser(
                Hiker(
                    userId = "hiker_1", name = "Poornesh P",
                    email = "hiker@demo.com", phoneNumber = "9876543210",
                    experienceLevel = "Intermediate", emergencyContact = "9876543211"
                ),
                passwordHash = "password123"
            )
            userRepository.createUser(
                ForestOfficer(
                    userId = "officer_1", name = "TN Forest Ranger",
                    email = "officer@demo.com", phoneNumber = "04222561525",
                    badgeNumber = "FO-2026-001", assignedRegion = "Western Ghats - Coimbatore"
                ),
                passwordHash = "officer123"
            )
            userRepository.createUser(
                Admin(
                    userId = "admin_1", name = "Admin",
                    email = "admin@demo.com", phoneNumber = "0000000000",
                    assignedRegion = "All"
                ),
                passwordHash = "admin123"
            )
        }
    }
}
