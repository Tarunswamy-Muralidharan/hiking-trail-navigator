package com.hikingtrailnavigator.app.data.local

import com.hikingtrailnavigator.app.data.local.entity.DangerZoneEntity
import com.hikingtrailnavigator.app.data.local.entity.EmergencyContactEntity
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
                LatLng(11.0100, 76.7950), LatLng(11.0050, 76.7880), LatLng(10.9990, 76.7810),
                LatLng(10.9930, 76.7750), LatLng(10.9870, 76.7690), LatLng(10.9810, 76.7630),
                LatLng(10.9750, 76.7570), LatLng(10.9690, 76.7510)
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
            )
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
                LatLng(10.9450, 76.6350), LatLng(10.9410, 76.6290), LatLng(10.9370, 76.6230),
                LatLng(10.9330, 76.6170), LatLng(10.9290, 76.6110)
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
            )
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
                LatLng(10.4840, 76.8380), LatLng(10.4800, 76.8330), LatLng(10.4750, 76.8270),
                LatLng(10.4700, 76.8210), LatLng(10.4650, 76.8150)
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
            )
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
                LatLng(11.2540, 78.3580), LatLng(11.2510, 78.3550), LatLng(11.2480, 78.3520),
                LatLng(11.2450, 78.3490), LatLng(11.2420, 78.3460)
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
            )
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
                LatLng(11.4010, 76.7350), LatLng(11.4025, 76.7370), LatLng(11.4040, 76.7390),
                LatLng(11.4055, 76.7410)
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
            )
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
                LatLng(10.3500, 76.8800), LatLng(10.3450, 76.8750), LatLng(10.3400, 76.8690),
                LatLng(10.3350, 76.8630), LatLng(10.3300, 76.8570), LatLng(10.3250, 76.8510)
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
            )
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
                LatLng(10.9950, 76.9200), LatLng(10.9965, 76.9170), LatLng(10.9980, 76.9140),
                LatLng(10.9995, 76.9110), LatLng(11.0010, 76.9080)
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
            )
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
                LatLng(11.2950, 76.9400), LatLng(11.2980, 76.9350), LatLng(11.3010, 76.9300),
                LatLng(11.3040, 76.9250), LatLng(11.3070, 76.9200), LatLng(11.3100, 76.9150)
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
            )
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
            id = "dz_6", name = "Mettupalayam - Railway Danger",
            centerLat = 11.3040, centerLng = 76.9250, radius = 200.0,
            type = "Terrain", severity = "Medium",
            description = "Trail crosses near Nilgiri Mountain Railway tracks. Watch for trains.", verified = true
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
