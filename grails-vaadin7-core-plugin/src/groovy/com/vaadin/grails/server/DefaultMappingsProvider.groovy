package com.vaadin.grails.server

import com.vaadin.grails.Vaadin
import grails.util.Holders
import org.apache.log4j.Logger

import javax.annotation.PostConstruct

/**
 * @author Stephan Grundner
 */
class DefaultMappingsProvider implements MappingsProvider {

    static final def log = Logger.getLogger(DefaultMappingsProvider)

    protected Map<String, Mapping> mappingByPath = new HashMap()

    @PostConstruct
    protected void init() {
        log.debug("Building mappings")
        def mappingsConfig = Holders.config.vaadin.mappings
        mappingsConfig.each { String path, ConfigObject mappingConfig ->
            def mapping = createMapping(path, mappingConfig)
            mappingByPath.put(mapping.path, mapping)
            println "mappings: ${mapping.properties}"
        }
    }

    protected Mapping createMapping(String path, ConfigObject mappingConfig) {
        def mapping = new DefaultMapping()
        mapping.path = path
        String ui = mappingConfig.ui
        String namespace = mappingConfig.namespace ?: null
        mapping.theme = mappingConfig.get("theme")
        mapping.widgetset = mappingConfig.get("widgetset")
        mapping.preservedOnRefresh = mappingConfig.get("preservedOnRefresh")
        mapping.pageTitle = mappingConfig.get("pageTitle")
        mapping.pushMode = mappingConfig.get("pushMode")
        mapping.pushTransport = mappingConfig.get("pushTransport")
        mapping.uiClass = Vaadin.utils.getVaadinUIClass(ui, namespace)

        log.debug("Mapping uri [${path}] to ui [${ui}]" + namespace ? " with namespace [${namespace}]" : "")
        mappingConfig.views.each { String fragment, ConfigObject viewMappingConfig ->
            String view = viewMappingConfig.view
            def viewClass = Vaadin.utils.getVaadinViewClass(view, namespace)
            mapping.addViewClass(fragment, viewClass)

            log.debug("Mapping fragment [${fragment}] to view [${view}]" + namespace ? " with namespace [${namespace}]" : "")
        }
        mapping
    }

    @Override
    Mapping getMapping(String path) {
        mappingByPath.get(path)
    }

    @Override
    Collection<Mapping> getAllMappings() {
        mappingByPath.values()
    }
}
