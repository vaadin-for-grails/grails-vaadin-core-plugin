package com.vaadin.grails.server

import com.vaadin.grails.VaadinMappingsClass
import com.vaadin.navigator.View
import com.vaadin.ui.UI
import grails.util.GrailsNameUtils
import grails.util.Holders

import javax.annotation.PostConstruct
import java.util.concurrent.ConcurrentHashMap

/**
 * The default implementation for {@link  MappingsProvider}.
 * <p>
 *     This class is accessible as a bean with the name <code>mappingsProvider</code>.
 * </p>
 * @author Stephan Grundner
 */
class DefaultMappingsProvider implements MappingsProvider {

    static abstract class AbstractMapping implements MappingsProvider.Mapping {

        String path
        String name
        Class clazz
    }

    static class DefaultUIMapping extends AbstractMapping implements MappingsProvider.UIMapping {

        String theme
        String widgetset
        String preservedOnRefresh
        String pageTitle
        String pushMode
        String pushTransport
    }

    static class DefaultViewMapping extends AbstractMapping implements MappingsProvider.ViewMapping {

        Collection<Class<? extends UI>> owners = []
    }

    static class MappingsBuilder extends BuilderSupport {

        final Closure mappingsClosure
        private Map<String, MappingsProvider.Mapping> mappings

        MappingsBuilder(Closure mappingsClosure) {
            this.mappingsClosure = mappingsClosure
        }

        Map<String, MappingsProvider.Mapping> build() {
            mappings = new HashMap<String, MappingsProvider.Mapping>()
            def delegate = mappingsClosure.delegate
            try {
                mappingsClosure.delegate = this
                mappingsClosure.call()
            } finally {
                mappingsClosure.delegate = delegate
            }

            mappings
        }

        @Override
        protected void setParent(Object parent, Object child) { }

        @Override
        protected Object createNode(Object name) {
            throw new UnsupportedOperationException()
        }

        @Override
        protected Object createNode(Object name, Object value) {
            throw new UnsupportedOperationException()
        }

        @Override
        protected Object createNode(Object name, Map attributes) {
            createNode(name, attributes, null)
        }

        protected Class<? extends UI> _resolveUIClass(Object value) {
            if (value instanceof Class) {
                return value
            } else {
                def artefact = Holders.grailsApplication.getArtefacts("UI")
                        .find { it.logicalPropertyName == value }
                if (artefact) {
                    return artefact.clazz
                }
            }
            throw new RuntimeException("Unable to resolve ui class for [${value}]")
        }

        protected Class<? extends View> _resolveViewClass(Object value) {
            if (value instanceof Class) {
                return value
            } else {
                def artefact = Holders.grailsApplication.getArtefacts("View")
                        .find { it.logicalPropertyName == value }
                if (artefact) {
                    return artefact.clazz
                }
            }
            throw new RuntimeException("Unable to resolve view class for [${value}]")
        }

        @Override
        protected Object createNode(Object name, Map attributes, Object value) {
            def mapping

            if (((String) name).startsWith("/")) {
                mapping = new DefaultUIMapping()
                def ui = attributes.remove("ui")
                Class<? extends UI> uiClass = _resolveUIClass(ui)
                mapping.setClazz(uiClass)
                mapping.setName(GrailsNameUtils.getLogicalPropertyName(uiClass.name, "UI"))

                mapping.theme = attributes["theme"]
                mapping.widgetset = attributes["widgetset"]
                mapping.preservedOnRefresh = attributes["preservedOnRefresh"]
                mapping.pageTitle = attributes["pageTitle"]
                mapping.pushMode = attributes["pushMode"]
                mapping.pushTransport = attributes["pushTransport"]
            } else if (((String) name).startsWith("#")) {
                mapping = new DefaultViewMapping()
                def view = attributes.remove("view")
                def viewClass = _resolveViewClass(view)
                mapping.setClazz(view)
                mapping.setName(GrailsNameUtils.getLogicalPropertyName(viewClass.name, "View"))

                def owners = attributes["ui"]
                if (owners) {
                    if (!(owners instanceof Collection)) {
                        owners = [owners]
                    }
                    owners.each { owner ->
                        def ownerClass = _resolveUIClass(owner)
                        mapping.owners.add(ownerClass)
                    }
                } else {
                    throw new RuntimeException("Missing ui attribute for [${name}]")
                }
            } else {
                throw new RuntimeException("Illegal mapping")
            }

            mapping.setPath(name)
            mappings.put(mapping.path, mapping)
            mapping
        }
    }

    protected final def mappings = new ConcurrentHashMap<String, MappingsProvider.Mapping>()

    DefaultMappingsProvider() {

    }

    @Override
    VaadinMappingsClass getMappingsClass() {
        def artefacts = Holders.grailsApplication.getArtefacts("VaadinMappings")
        if (artefacts?.size() > 0) {
            return artefacts.first()
        }
//        TODO merge artefacts
        null
    }

    @PostConstruct
    protected void init() {
        def mappingsClass = getMappingsClass()
        if (mappingsClass) {
            def builder = new MappingsBuilder(mappingsClass.mappingsClosure)
            mappings.putAll(builder.build())
        } else {
            throw new RuntimeException("No VaadinMappings class found")
        }
    }

    @Override
    boolean containsMapping(String path) {
        mappings.contains(path)
    }

    @Override
    MappingsProvider.Mapping getMapping(String path) {
        mappings.get(path)
    }

    @Override
    MappingsProvider.Mapping getMapping(Class<?> clazz) {
        mappings.values().find { it.clazz == clazz }
    }

    @Override
    Collection<MappingsProvider.Mapping> getAllMappings() {
        mappings.values()
    }
}