package com.hikingtrailnavigator.app.data.local

import com.hikingtrailnavigator.app.data.local.entity.toDomain
import com.hikingtrailnavigator.app.data.repository.EmergencyContactRepository
import com.hikingtrailnavigator.app.data.repository.TrailRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseSeeder @Inject constructor(
    private val trailRepository: TrailRepository,
    private val emergencyContactRepository: EmergencyContactRepository
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
    }
}
