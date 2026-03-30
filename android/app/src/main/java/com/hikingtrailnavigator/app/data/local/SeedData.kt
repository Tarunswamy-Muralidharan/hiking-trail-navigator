package com.hikingtrailnavigator.app.data.local

import com.hikingtrailnavigator.app.data.local.entity.DangerZoneEntity
import com.hikingtrailnavigator.app.data.local.entity.EmergencyContactEntity
import com.hikingtrailnavigator.app.data.local.entity.HazardReportEntity
import com.hikingtrailnavigator.app.data.local.entity.LowActivityZoneEntity
import com.hikingtrailnavigator.app.data.local.entity.NoCoverageZoneEntity
import com.hikingtrailnavigator.app.domain.model.*
import com.google.gson.Gson

object SeedData {

    private val gson = Gson()

    // Trails around Coimbatore / Western Ghats near PSG iTech Neelambur
    val trails = listOf(
        Trail(
            id = "trail_1",
            name = "Vellingiri Hills Trek",
            description = "Sacred seven-hill trek from Poondi to the Vellingiri Andavar temple at 1840m. One of the most popular and challenging treks near Coimbatore through dense Western Ghats forests.",
            difficulty = Difficulty.Hard,
            distance = 22.0,
            estimatedDuration = "10-12 hours",
            elevationGain = 1500,
            rating = 4.7,
            coordinates = listOf(
                LatLng(11.0100, 76.7950), // Poondi base (start)
                LatLng(11.0078, 76.7928), // Trail entrance past temple
                LatLng(11.0055, 76.7903), // Forest path - 1st hill approach
                LatLng(11.0038, 76.7870), // 1st hill - Vellai Vinayakar temple
                LatLng(11.0015, 76.7845), // Ridge path between 1st and 2nd hill
                LatLng(10.9990, 76.7822), // 2nd hill summit
                LatLng(10.9968, 76.7795), // Descent to valley
                LatLng(10.9950, 76.7760), // Valley stream crossing
                LatLng(10.9935, 76.7738), // 3rd hill - Vazhukku Paarai (slippery rock)
                LatLng(10.9912, 76.7710), // Ridge trail through dense forest
                LatLng(10.9890, 76.7685), // 4th hill - Kai Thatti Sonai
                LatLng(10.9865, 76.7668), // Trail curves west through shola forest
                LatLng(10.9840, 76.7640), // 5th hill approach - steep section
                LatLng(10.9815, 76.7615), // 5th hill - Aandi Sonai spring
                LatLng(10.9790, 76.7590), // Grassland traverse to 6th hill
                LatLng(10.9762, 76.7568), // 6th hill - panoramic viewpoint
                LatLng(10.9738, 76.7545), // Final steep ascent begins
                LatLng(10.9715, 76.7525), // Rocky scramble section
                LatLng(10.9690, 76.7510)  // 7th hill - Vellingiri Andavar temple (summit)
            ),
            startPoint = LatLng(11.0100, 76.7950),
            endPoint = LatLng(10.9690, 76.7510),
            hazards = listOf("Leeches", "Steep terrain", "Wild elephants", "Limited water sources", "Slippery rocks"),
            region = "Coimbatore",
            popularity = 95,
            coverageStatus = CoverageStatus.None,
            elevationProfile = listOf(
                ElevationPoint(0.0, 340), ElevationPoint(3.0, 550), ElevationPoint(6.0, 800),
                ElevationPoint(9.0, 1050), ElevationPoint(12.0, 1250), ElevationPoint(15.0, 1450),
                ElevationPoint(18.0, 1680), ElevationPoint(22.0, 1840)
            ),
            schedule = "Open: Mon-Sat (6 AM - 4 PM) | Closed: Sunday, Full Moon days"
        ),
        Trail(
            id = "trail_2",
            name = "Siruvani Dam Trek",
            description = "Scenic trek through the Siruvani forest reserve to the famous Siruvani Dam, known for having some of the tastiest water in Asia. Moderate trail with river crossings.",
            difficulty = Difficulty.Moderate,
            distance = 10.0,
            estimatedDuration = "4-5 hours",
            elevationGain = 450,
            rating = 4.5,
            coordinates = listOf(
                LatLng(10.9450, 76.6350), // Siruvani foothills trailhead
                LatLng(10.9438, 76.6332), // Forest entry checkpoint
                LatLng(10.9425, 76.6310), // Winding forest path
                LatLng(10.9415, 76.6285), // First river crossing
                LatLng(10.9398, 76.6268), // Trail follows river upstream
                LatLng(10.9380, 76.6248), // Dense bamboo grove section
                LatLng(10.9365, 76.6225), // Second river crossing
                LatLng(10.9350, 76.6200), // Uphill through teak plantation
                LatLng(10.9332, 76.6175), // Ridge with valley views
                LatLng(10.9318, 76.6148), // Descent to dam approach
                LatLng(10.9305, 76.6128), // Dam viewpoint trail
                LatLng(10.9290, 76.6110)  // Siruvani Dam
            ),
            startPoint = LatLng(10.9450, 76.6350),
            endPoint = LatLng(10.9290, 76.6110),
            hazards = listOf("River crossing", "Slippery in monsoon", "Leeches"),
            region = "Coimbatore",
            popularity = 88,
            coverageStatus = CoverageStatus.Partial,
            elevationProfile = listOf(
                ElevationPoint(0.0, 420), ElevationPoint(2.5, 550), ElevationPoint(5.0, 680),
                ElevationPoint(7.5, 780), ElevationPoint(10.0, 870)
            ),
            schedule = "Open: Tue-Sun (7 AM - 5 PM) | Closed: Monday"
        ),
        Trail(
            id = "trail_3",
            name = "Topslip - Parambikulam Trek",
            description = "Biodiversity-rich trek through Anamalai Tiger Reserve connecting Topslip to Parambikulam. Rich wildlife including elephants, gaur, and hornbills.",
            difficulty = Difficulty.Moderate,
            distance = 14.0,
            estimatedDuration = "6-7 hours",
            elevationGain = 700,
            rating = 4.6,
            coordinates = listOf(
                LatLng(10.4840, 76.8380), // Topslip forest gate
                LatLng(10.4828, 76.8365), // Teak plantation entrance
                LatLng(10.4812, 76.8348), // Trail follows forest road
                LatLng(10.4795, 76.8330), // Elephant observation tower
                LatLng(10.4778, 76.8308), // Stream crossing - gaur territory
                LatLng(10.4760, 76.8285), // Dense evergreen forest path
                LatLng(10.4745, 76.8265), // Ridge climb begins
                LatLng(10.4728, 76.8242), // Hornbill nesting area
                LatLng(10.4710, 76.8222), // Switchback trail section
                LatLng(10.4695, 76.8198), // Valley overlook viewpoint
                LatLng(10.4678, 76.8175), // Final descent to Parambikulam
                LatLng(10.4650, 76.8150)  // Parambikulam dam area
            ),
            startPoint = LatLng(10.4840, 76.8380),
            endPoint = LatLng(10.4650, 76.8150),
            hazards = listOf("Wild elephants", "Tiger territory", "Dense forest", "River crossings"),
            region = "Anamalai Hills",
            popularity = 82,
            coverageStatus = CoverageStatus.None,
            elevationProfile = listOf(
                ElevationPoint(0.0, 780), ElevationPoint(3.5, 950), ElevationPoint(7.0, 1180),
                ElevationPoint(10.5, 1350), ElevationPoint(14.0, 1480)
            ),
            schedule = "Open: Wed-Mon (6 AM - 3 PM) | Closed: Tuesday (Forest dept maintenance)"
        ),
        Trail(
            id = "trail_4",
            name = "Kolli Hills - Agaya Gangai Falls",
            description = "Trek to the stunning Agaya Gangai waterfalls in Kolli Hills via 70 hairpin bends. The trail descends through medicinal plant forests.",
            difficulty = Difficulty.Moderate,
            distance = 8.0,
            estimatedDuration = "3-4 hours",
            elevationGain = 500,
            rating = 4.3,
            coordinates = listOf(
                LatLng(11.2540, 78.3580), // Kolli Hills viewpoint (top)
                LatLng(11.2532, 78.3572), // Start of 1000-step descent
                LatLng(11.2522, 78.3565), // First set of stone steps
                LatLng(11.2515, 78.3555), // Trail curves through forest canopy
                LatLng(11.2505, 78.3548), // Medicinal plant forest section
                LatLng(11.2495, 78.3538), // Rocky switchback section
                LatLng(11.2485, 78.3525), // Steep descent - rope section
                LatLng(11.2475, 78.3512), // Stream crossing near falls
                LatLng(11.2465, 78.3498), // Mist zone - falls spray
                LatLng(11.2450, 78.3480), // Agaya Gangai Falls viewpoint
                LatLng(11.2440, 78.3470), // Falls base pool area
                LatLng(11.2420, 78.3460)  // Falls rest area
            ),
            startPoint = LatLng(11.2540, 78.3580),
            endPoint = LatLng(11.2420, 78.3460),
            hazards = listOf("Steep steps", "Slippery near falls", "Monkeys"),
            region = "Namakkal",
            popularity = 85,
            coverageStatus = CoverageStatus.Partial,
            elevationProfile = listOf(
                ElevationPoint(0.0, 1300), ElevationPoint(2.0, 1150), ElevationPoint(4.0, 980),
                ElevationPoint(6.0, 850), ElevationPoint(8.0, 800)
            ),
            schedule = "Open: All days (8 AM - 6 PM) | Closed: During heavy rain alerts"
        ),
        Trail(
            id = "trail_5",
            name = "Doddabetta Peak Trail",
            description = "Short trek to the highest peak in the Nilgiri Mountains at 2637m. Easy access from Ooty with well-maintained path and telescope house at summit.",
            difficulty = Difficulty.Easy,
            distance = 3.0,
            estimatedDuration = "1-2 hours",
            elevationGain = 200,
            rating = 4.2,
            coordinates = listOf(
                LatLng(11.4010, 76.7350), // Doddabetta parking area
                LatLng(11.4015, 76.7355), // Paved walkway starts
                LatLng(11.4020, 76.7362), // Gentle incline through eucalyptus
                LatLng(11.4025, 76.7368), // First viewpoint - valley view
                LatLng(11.4030, 76.7375), // Switchback on paved path
                LatLng(11.4035, 76.7382), // Shola forest section
                LatLng(11.4040, 76.7390), // Steep section with railing
                LatLng(11.4045, 76.7396), // Near summit - fog zone
                LatLng(11.4050, 76.7403), // Final stairs to telescope house
                LatLng(11.4055, 76.7410)  // Doddabetta Peak summit (2637m)
            ),
            startPoint = LatLng(11.4010, 76.7350),
            endPoint = LatLng(11.4055, 76.7410),
            hazards = listOf("Fog", "Cold winds at summit"),
            region = "Nilgiris",
            popularity = 94,
            coverageStatus = CoverageStatus.Full,
            elevationProfile = listOf(
                ElevationPoint(0.0, 2437), ElevationPoint(1.0, 2520), ElevationPoint(2.0, 2590),
                ElevationPoint(3.0, 2637)
            ),
            schedule = "Open: All days (7 AM - 5:30 PM)"
        ),
        Trail(
            id = "trail_6",
            name = "Anamalai Hills - Grass Hills Trek",
            description = "Stunning grassland trek in the Anamalai Hills with panoramic views of the Western Ghats. Home to the endangered Nilgiri Tahr.",
            difficulty = Difficulty.Hard,
            distance = 16.0,
            estimatedDuration = "7-8 hours",
            elevationGain = 1100,
            rating = 4.8,
            coordinates = listOf(
                LatLng(10.3500, 76.8800), // Grass Hills base camp
                LatLng(10.3488, 76.8785), // Forest road entrance
                LatLng(10.3475, 76.8768), // Trail through cardamom plantation
                LatLng(10.3460, 76.8750), // Stream ford - elephant territory
                LatLng(10.3445, 76.8730), // Steep climb through shola forest
                LatLng(10.3430, 76.8710), // Emerging onto grassland plateau
                LatLng(10.3412, 76.8690), // Open grassland - Nilgiri Tahr zone
                LatLng(10.3395, 76.8668), // Ridgeline traverse
                LatLng(10.3378, 76.8645), // Wind-exposed saddle
                LatLng(10.3360, 76.8622), // Panoramic viewpoint west
                LatLng(10.3340, 76.8598), // Rocky ascent to summit ridge
                LatLng(10.3320, 76.8575), // Summit approach - cliff edges
                LatLng(10.3300, 76.8552), // False summit
                LatLng(10.3280, 76.8530), // Final ridge walk
                LatLng(10.3250, 76.8510)  // Grass Hills summit viewpoint
            ),
            startPoint = LatLng(10.3500, 76.8800),
            endPoint = LatLng(10.3250, 76.8510),
            hazards = listOf("Wild elephants", "No water sources", "Steep grassland", "Fog", "Leeches in monsoon"),
            region = "Anamalai Hills",
            popularity = 72,
            coverageStatus = CoverageStatus.None,
            elevationProfile = listOf(
                ElevationPoint(0.0, 1200), ElevationPoint(4.0, 1550), ElevationPoint(8.0, 1900),
                ElevationPoint(12.0, 2100), ElevationPoint(16.0, 2300)
            ),
            schedule = "Open: Mon-Fri (6 AM - 2 PM) | Closed: Sat, Sun (Forest permit needed)"
        ),
        Trail(
            id = "trail_7",
            name = "Perur - Marudhamalai Temple Trek",
            description = "Easy spiritual trek from Perur to Marudhamalai hilltop temple. Close to Coimbatore city with good trail marking. Great for beginners.",
            difficulty = Difficulty.Easy,
            distance = 5.0,
            estimatedDuration = "2-3 hours",
            elevationGain = 300,
            rating = 4.1,
            coordinates = listOf(
                LatLng(10.9950, 76.9200), // Perur temple entrance
                LatLng(10.9955, 76.9190), // Cross village road
                LatLng(10.9960, 76.9178), // Begin rocky footpath
                LatLng(10.9966, 76.9165), // Through dry scrubland
                LatLng(10.9972, 76.9152), // First stone steps
                LatLng(10.9978, 76.9140), // Shaded banyan tree rest stop
                LatLng(10.9985, 76.9128), // Rocky ascent - marked trail
                LatLng(10.9990, 76.9115), // Halfway viewpoint - city view
                LatLng(10.9996, 76.9102), // Steep section with handrails
                LatLng(11.0003, 76.9090), // Temple approach road
                LatLng(11.0010, 76.9080)  // Marudhamalai hilltop temple
            ),
            startPoint = LatLng(10.9950, 76.9200),
            endPoint = LatLng(11.0010, 76.9080),
            hazards = listOf("Rocky terrain", "Monkeys at temple area"),
            region = "Coimbatore",
            popularity = 90,
            coverageStatus = CoverageStatus.Full,
            elevationProfile = listOf(
                ElevationPoint(0.0, 380), ElevationPoint(1.25, 450), ElevationPoint(2.5, 530),
                ElevationPoint(3.75, 600), ElevationPoint(5.0, 680)
            ),
            schedule = "Open: All days (5 AM - 8 PM) | Temple darshan: 6 AM - 12 PM, 4 PM - 8 PM"
        ),
        Trail(
            id = "trail_9",
            name = "PSG iTech Neelambur Campus Trek",
            description = "A fun campus trek around PSG Institute of Technology and Applied Research, Neelambur. Walk from the main gate past the admin block, through the central courtyard, along the lab buildings, loop around the ground, and back via the canteen. Perfect break between OOSE lab sessions!",
            difficulty = Difficulty.Easy,
            distance = 1.5,
            estimatedDuration = "30-45 mins",
            elevationGain = 15,
            rating = 4.9,
            coordinates = listOf(
                LatLng(11.0640, 77.0929), // Main gate area - campus entrance driveway
                LatLng(11.0643, 77.0928), // Walk north on driveway past parking
                LatLng(11.0645, 77.0928), // Enter campus building area - south corridor
                LatLng(11.0646, 77.0929), // Turn east on walkway between blocks
                LatLng(11.0646, 77.0931), // Continue east between building rows
                LatLng(11.0646, 77.0933), // Reach eastern walkway of building cluster
                LatLng(11.0648, 77.0933), // Turn north on eastern corridor past labs
                LatLng(11.0650, 77.0933), // Continue north between east buildings
                LatLng(11.0652, 77.0933), // Pass CSE / IT department corridor
                LatLng(11.0654, 77.0933), // Continue north past department blocks
                LatLng(11.0656, 77.0932), // North end of building cluster - turn west
                LatLng(11.0656, 77.0930), // Walk west along north building corridor
                LatLng(11.0656, 77.0928), // Continue west
                LatLng(11.0656, 77.0926), // Northwest corner of building cluster
                LatLng(11.0654, 77.0925), // Turn south on western corridor
                LatLng(11.0652, 77.0925), // Continue south past west buildings
                LatLng(11.0650, 77.0925), // Continue south on west corridor
                LatLng(11.0648, 77.0925), // Pass western department blocks
                LatLng(11.0646, 77.0925), // Southwest corner of building cluster
                LatLng(11.0646, 77.0927), // Turn east back toward main driveway
                LatLng(11.0645, 77.0928), // Rejoin main driveway
                LatLng(11.0643, 77.0928), // Head south toward gate
                LatLng(11.0640, 77.0929)  // Back to main gate
            ),
            startPoint = LatLng(11.0640, 77.0929),
            endPoint = LatLng(11.0640, 77.0929),
            hazards = listOf("Faculty supervision required", "Attendance shortage risk", "Canteen temptation", "HOD sighting zone"),
            region = "Coimbatore",
            popularity = 100,
            coverageStatus = CoverageStatus.Full,
            elevationProfile = listOf(
                ElevationPoint(0.0, 390), ElevationPoint(0.3, 392), ElevationPoint(0.6, 395),
                ElevationPoint(0.9, 393), ElevationPoint(1.2, 391), ElevationPoint(1.5, 390)
            ),
            schedule = "Open: Mon-Fri (8:30 AM - 4:30 PM) | Closed: Sat, Sun, College holidays | Warning: HOD patrol hours 9-10 AM"
        ),
        Trail(
            id = "trail_8",
            name = "Black Thunder - Mettupalayam Forest Trek",
            description = "Forest trek through the foothills near Mettupalayam. Dense deciduous forests with diverse birdlife. Trail follows the mountain railway route.",
            difficulty = Difficulty.Moderate,
            distance = 12.0,
            estimatedDuration = "5-6 hours",
            elevationGain = 650,
            rating = 4.4,
            coordinates = listOf(
                LatLng(11.2950, 76.9400), // Mettupalayam town edge
                LatLng(11.2958, 76.9388), // Cross railway tracks
                LatLng(11.2968, 76.9372), // Forest road entrance
                LatLng(11.2978, 76.9358), // Along Nilgiri Mountain Railway
                LatLng(11.2990, 76.9340), // First bridge crossing
                LatLng(11.3002, 76.9322), // Trail diverges from railway
                LatLng(11.3015, 76.9305), // Dense deciduous forest path
                LatLng(11.3028, 76.9288), // Stream crossing - birdwatching spot
                LatLng(11.3040, 76.9268), // Uphill through teak forest
                LatLng(11.3052, 76.9248), // Old British-era milestone
                LatLng(11.3065, 76.9228), // Steep climb with valley view
                LatLng(11.3078, 76.9208), // Ridge path through mixed forest
                LatLng(11.3090, 76.9180), // Near watchtower
                LatLng(11.3100, 76.9150)  // Forest trail end - hilltop
            ),
            startPoint = LatLng(11.2950, 76.9400),
            endPoint = LatLng(11.3100, 76.9150),
            hazards = listOf("Monkeys", "Slippery in rain", "Train tracks nearby"),
            region = "Mettupalayam",
            popularity = 78,
            coverageStatus = CoverageStatus.Partial,
            elevationProfile = listOf(
                ElevationPoint(0.0, 350), ElevationPoint(2.0, 480), ElevationPoint(4.0, 620),
                ElevationPoint(6.0, 750), ElevationPoint(8.0, 870), ElevationPoint(12.0, 1000)
            ),
            schedule = "Open: All days (6 AM - 5 PM) | Closed: Monsoon season (Jun-Aug)"
        )
    )

    val dangerZones = listOf(
        DangerZoneEntity(
            id = "dz_1", name = "Vellingiri - Elephant Corridor",
            centerLat = 10.9930, centerLng = 76.7750, radius = 800.0,
            type = "Wildlife", severity = "Critical",
            description = "Active elephant corridor. Trek only in groups. Avoid after 4 PM.", verified = true
        ),
        DangerZoneEntity(
            id = "dz_2", name = "Siruvani - Flash Flood Zone",
            centerLat = 10.9370, centerLng = 76.6230, radius = 400.0,
            type = "Flood", severity = "High",
            description = "River crossing prone to flash floods during monsoon (June-Sept).", verified = true
        ),
        DangerZoneEntity(
            id = "dz_3", name = "Topslip - Tiger Territory",
            centerLat = 10.4750, centerLng = 76.8270, radius = 1000.0,
            type = "Wildlife", severity = "Critical",
            description = "Anamalai Tiger Reserve core zone. Strictly follow forest guard instructions.", verified = true
        ),
        DangerZoneEntity(
            id = "dz_4", name = "Anamalai - Steep Grassland Drop",
            centerLat = 10.3400, centerLng = 76.8690, radius = 300.0,
            type = "Terrain", severity = "High",
            description = "Steep cliff edges hidden by tall grass. Stay on marked trail.", verified = true
        ),
        DangerZoneEntity(
            id = "dz_5", name = "Vellingiri - Landslide Zone",
            centerLat = 10.9870, centerLng = 76.7690, radius = 350.0,
            type = "Landslide", severity = "High",
            description = "Loose soil section prone to landslides after heavy rain.", verified = true
        ),
        DangerZoneEntity(
            id = "dz_7", name = "Canteen - Food Trap",
            centerLat = 11.0646, centerLng = 77.0931, radius = 15.0,
            type = "Terrain", severity = "Critical",
            description = "High food temptation zone. Biriyani aroma radius: 50m. Budget may not survive. Students often get stuck here for hours.",
            verified = true
        ),
        DangerZoneEntity(
            id = "dz_8", name = "HOD Office - No Entry Zone",
            centerLat = 11.0654, centerLng = 77.0929, radius = 10.0,
            type = "Restricted", severity = "Critical",
            description = "Attendance verification zone. Avoid if bunking. High risk of getting caught. Survival rate: 20%.",
            verified = true
        ),
        DangerZoneEntity(
            id = "dz_9", name = "Library - Sleep Trap",
            centerLat = 11.0652, centerLng = 77.0931, radius = 12.0,
            type = "Wildlife", severity = "High",
            description = "AC + silence = guaranteed nap. Many students entered, few returned awake. Bring coffee or perish.",
            verified = true
        ),
        DangerZoneEntity(
            id = "dz_10", name = "CS Lab - Code Zone",
            centerLat = 11.0650, centerLng = 77.0933, radius = 12.0,
            type = "Terrain", severity = "High",
            description = "OOSE Lab. Deadlines strike without warning. Segfaults and NullPointers lurk everywhere. Code or perish.",
            verified = true
        ),
        DangerZoneEntity(
            id = "dz_11", name = "Parking Lot - Bike Jungle",
            centerLat = 11.0642, centerLng = 77.0929, radius = 15.0,
            type = "Terrain", severity = "Medium",
            description = "Peak hour chaos. Finding your bike is the real trek. Last seen: your Activa between 500 other Activas.",
            verified = true
        ),
        DangerZoneEntity(
            id = "dz_12", name = "Main Gate - Checkpoint",
            centerLat = 11.0640, centerLng = 77.0929, radius = 10.0,
            type = "Restricted", severity = "Critical",
            description = "ID card checkpoint. No ID = no entry. Late entry logged. Security uncle remembers every face.",
            verified = true
        ),
        DangerZoneEntity(
            id = "dz_13", name = "Seminar Hall - Boredom Zone",
            centerLat = 11.0655, centerLng = 77.0927, radius = 12.0,
            type = "Terrain", severity = "High",
            description = "3-hour seminars with no breaks. Phone confiscation risk. Back-benchers have higher survival rate.",
            verified = true
        ),
        DangerZoneEntity(
            id = "dz_14", name = "Exam Hall - Final Boss",
            centerLat = 11.0648, centerLng = 77.0927, radius = 12.0,
            type = "Terrain", severity = "Critical",
            description = "The ultimate danger zone. No phones, no notes, no mercy. Only your brain vs the question paper. Good luck.",
            verified = true
        ),
        DangerZoneEntity(
            id = "dz_6", name = "Mettupalayam - Railway Danger",
            centerLat = 11.3040, centerLng = 76.9250, radius = 200.0,
            type = "Terrain", severity = "Medium",
            description = "Trail crosses near Nilgiri Mountain Railway tracks. Watch for trains.", verified = true
        ),
        DangerZoneEntity(
            id = "dz_15", name = "Steep Construction Area",
            centerLat = 11.0638, centerLng = 77.0935, radius = 40.0,
            type = "Terrain", severity = "High",
            description = "Active construction with loose debris and uneven ground. Watch your step.", verified = true
        ),
        DangerZoneEntity(
            id = "dz_16", name = "Stray Dog Territory",
            centerLat = 11.0658, centerLng = 77.0922, radius = 30.0,
            type = "Wildlife", severity = "Medium",
            description = "Pack of stray dogs frequently spotted. Avoid during early morning/evening.", verified = true
        )
    )

    val noCoverageZones = listOf(
        NoCoverageZoneEntity(
            id = "nc_1", name = "Vellingiri Hills Deep Forest",
            centerLat = 10.9810, centerLng = 76.7630, radius = 3000.0,
            description = "No mobile coverage beyond 2nd hill. Download offline maps before starting."
        ),
        NoCoverageZoneEntity(
            id = "nc_2", name = "Topslip - Parambikulam Forest",
            centerLat = 10.4700, centerLng = 76.8210, radius = 4000.0,
            description = "Entire Anamalai forest area has no coverage. Inform contacts before entering."
        ),
        NoCoverageZoneEntity(
            id = "nc_3", name = "Siruvani Reserve Forest",
            centerLat = 10.9330, centerLng = 76.6170, radius = 2500.0,
            description = "No coverage deep inside Siruvani forest. Signal only near dam area."
        ),
        NoCoverageZoneEntity(
            id = "nc_4", name = "Grass Hills Interior",
            centerLat = 10.3350, centerLng = 76.8630, radius = 5000.0,
            description = "Zero connectivity in Anamalai grasslands. Carry emergency whistle and flares."
        ),
        NoCoverageZoneEntity(
            id = "nc_5", name = "Campus Basement Dead Zone",
            centerLat = 11.0645, centerLng = 77.0938, radius = 60.0,
            description = "Underground basement area with no cellular signal"
        )
    )

    // FR-210: Low activity / unexplored zones
    val lowActivityZones = listOf(
        LowActivityZoneEntity(
            id = "la_1", name = "Unexplored Anaikatti Ridge",
            centerLat = 11.0800, centerLng = 76.7200, radius = 2000.0,
            activityLevel = "unexplored",
            description = "Uncharted ridge with no recorded hikes. Exercise extreme caution."
        ),
        LowActivityZoneEntity(
            id = "la_2", name = "Low-Traffic Siruvani Interior",
            centerLat = 10.9200, centerLng = 76.6300, radius = 2500.0,
            activityLevel = "low",
            description = "Very few hikers visit this area. Limited trail markings."
        ),
        LowActivityZoneEntity(
            id = "la_3", name = "Unexplored Parambikulam Buffer",
            centerLat = 10.4500, centerLng = 76.8400, radius = 3000.0,
            activityLevel = "unexplored",
            description = "Buffer zone with no established trails. Dense forest cover."
        ),
        LowActivityZoneEntity(
            id = "la_4", name = "Low-Activity Kovaipudur Hills",
            centerLat = 10.9500, centerLng = 76.9100, radius = 1500.0,
            activityLevel = "low",
            description = "Occasional hikers. Trails may be overgrown. Inform someone before entering."
        ),
        LowActivityZoneEntity(
            id = "la_5", name = "Unexplored Valparai Corridor",
            centerLat = 10.3800, centerLng = 76.9200, radius = 3500.0,
            activityLevel = "unexplored",
            description = "Remote corridor between Valparai estates. No established paths."
        ),
        LowActivityZoneEntity(
            id = "la_6", name = "Unexplored Back Campus Path",
            centerLat = 11.0635, centerLng = 77.0925, radius = 80.0,
            activityLevel = "unexplored",
            description = "Rarely visited path behind campus buildings. No trail data available."
        ),
        LowActivityZoneEntity(
            id = "la_7", name = "Low-Traffic Evening Route",
            centerLat = 11.0660, centerLng = 77.0920, radius = 100.0,
            activityLevel = "low",
            description = "This route has very few hikers. Limited safety data."
        )
    )

    val sampleHazardReports = listOf(
        HazardReportEntity(
            id = "hr_1",
            type = "Wildlife",
            severity = "High",
            latitude = 11.0648,
            longitude = 77.0930,
            description = "Pack of monkeys blocking the path near the library. They snatch food and bags.",
            reportedAt = System.currentTimeMillis() - 86400000, // 1 day ago
            confirmations = 4,
            expiresAt = System.currentTimeMillis() + 518400000, // 6 days from now
            isVerified = false
        ),
        HazardReportEntity(
            id = "hr_2",
            type = "Trail Damage",
            severity = "Medium",
            latitude = 11.0642,
            longitude = 77.0932,
            description = "Broken tiles and exposed rebar on the walkway. Risk of tripping.",
            reportedAt = System.currentTimeMillis() - 172800000, // 2 days ago
            confirmations = 2,
            expiresAt = System.currentTimeMillis() + 432000000, // 5 days from now
            isVerified = true
        ),
        HazardReportEntity(
            id = "hr_3",
            type = "Flooding",
            severity = "High",
            latitude = 10.9930,
            longitude = 76.7750,
            description = "Water logging after rain near Vellingiri base. Slippery rocks and ankle-deep water.",
            reportedAt = System.currentTimeMillis() - 43200000, // 12 hours ago
            confirmations = 5,
            expiresAt = System.currentTimeMillis() + 561600000,
            isVerified = true
        ),
        HazardReportEntity(
            id = "hr_4",
            type = "Landslide",
            severity = "Critical",
            latitude = 10.9870,
            longitude = 76.7690,
            description = "Fresh rockslide debris blocking 60% of trail. Loose boulders still falling.",
            reportedAt = System.currentTimeMillis() - 7200000, // 2 hours ago
            confirmations = 1,
            expiresAt = System.currentTimeMillis() + 604800000,
            isVerified = false
        ),
        HazardReportEntity(
            id = "hr_5",
            type = "Poor Visibility",
            severity = "Medium",
            latitude = 10.4750,
            longitude = 76.8270,
            description = "Dense fog reducing visibility to under 10 meters. Trail markers not visible.",
            reportedAt = System.currentTimeMillis() - 14400000, // 4 hours ago
            confirmations = 3,
            expiresAt = System.currentTimeMillis() + 590400000,
            isVerified = false
        ),
        HazardReportEntity(
            id = "hr_6",
            type = "Fallen Tree",
            severity = "Low",
            latitude = 11.0655,
            longitude = 77.0927,
            description = "Small fallen branch partially blocking the campus path near seminar hall.",
            reportedAt = System.currentTimeMillis() - 259200000, // 3 days ago
            confirmations = 1,
            expiresAt = System.currentTimeMillis() + 345600000,
            isVerified = false
        )
    )

    val defaultContacts = listOf(
        EmergencyContactEntity(
            id = "ec_1", name = "TN Forest Dept. Helpline",
            phone = "1800-425-1600", relation = "Tamil Nadu Forest Dept"
        ),
        EmergencyContactEntity(
            id = "ec_2", name = "Disaster Mgmt (SDMA)",
            phone = "1070", relation = "State Disaster Response"
        )
    )
}
