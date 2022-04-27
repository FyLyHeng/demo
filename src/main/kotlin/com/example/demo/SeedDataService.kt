package com.example.demo


import com.example.demo.model.setting.DocumentSetting
import com.example.demo.repository.DocumentSettingRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

/**
 * this class use for init default data to master tenant
 */
@Component
class SeedDataService : CommandLineRunner {

    @Autowired
    lateinit var documentSettingService : DocumentSettingRepository


    /**
     * Main run fun for process init
     */
    override fun run(vararg args: String?) {
        initSeries()
    }



    private fun initSeries () {

        /**
         * [key = series_name, value = series_prefix]
         */
        val defaultSeries = mapOf(
                "invoice" to "INV"
        )

        val existSeries = documentSettingService.findAllByNameIn(defaultSeries.keys.toList())?.map { it.name }?: mutableListOf()

        defaultSeries.keys.toList().filterNot { existSeries.contains(it) }.forEach { series_name ->
            documentSettingService.save(DocumentSetting(name = series_name, prefix = defaultSeries[series_name], lastCode = 0))
        }
    }
}
