package dev.synapse.di

import dev.synapse.database.SynapseDatabase
import dev.synapse.domain.repository.ResonanceRepository

expect fun createResonanceRepository(database: SynapseDatabase): ResonanceRepository
