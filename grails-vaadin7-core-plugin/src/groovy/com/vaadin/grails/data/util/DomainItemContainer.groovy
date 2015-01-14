package com.vaadin.grails.data.util

import com.vaadin.data.Container
import com.vaadin.data.Item
import com.vaadin.data.Property
import com.vaadin.data.util.AbstractInMemoryContainer
import com.vaadin.data.util.NestedPropertyDescriptor
import com.vaadin.data.util.VaadinPropertyDescriptor
import com.vaadin.data.util.filter.UnsupportedFilterException

/**
 * @author Stephan Grundner
 */
class DomainItemContainer<T> extends AbstractInMemoryContainer<Serializable, DomainItemProperty<T, ?>, DomainItem<T>>
        implements Container.Filterable, Container.Sortable, Property.ValueChangeListener, Container.PropertySetChangeNotifier {

    final Class<T> type

    final Set<DomainItemProperty<T, ?>> propertyIds = new HashSet()
    final Map<Serializable, DomainItem<T>> itemById = new HashMap()

    DomainItemContainer(Class<? super T> type) throws IllegalArgumentException {
        this.type = type
        def domainClass = DomainItem.getDomainClass(type)
        propertyIds = domainClass.persistentProperties.collect { it.name }
    }

    @Override
    protected void registerNewItem(int position, Serializable itemId, DomainItem<T> item) {
        itemById.put(itemId, item)
        super.registerNewItem(position, itemId, item)
    }

    protected DomainItem<T> createDomainItem(T object) {
        object == null ? null : new DomainItem<T>(object)
    }

    @Override
    Object addItem() throws UnsupportedOperationException {
        def object = type.newInstance()
        def item = createDomainItem(object)
        internalAddItemAtEnd(item.id, item, true)
    }

    @Override
    Object addItemAt(int index) throws UnsupportedOperationException {
        addItemAt(index, type.newInstance())
    }

    @Override
    Item addItemAt(int index, T object) throws UnsupportedOperationException {
        def item = createDomainItem(object)
        internalAddItemAt(index, item.id, item, true)
    }

    @Override
    Object addItemAfter(T previousObject) throws UnsupportedOperationException {
        def object = type.newInstance()
        def item = createDomainItem(object)
        def previousItem = getItem(previousObject)
        internalAddItemAfter(previousItem.id, item.id, item, true)
    }

    @Override
    Item addItemAfter(T previousObject, T object) throws UnsupportedOperationException {
        def item = createDomainItem(object)
        def previousItem = getItem(previousObject)
        internalAddItemAfter(previousItem.id, item.id, item, true)
    }

    @Override
    Item addItem(T object) throws UnsupportedOperationException {
        def item = createDomainItem(object)
        internalAddItemAtEnd(item.id, item, true)
    }

//    TODO implement remove*Item() and removeAllItems()

    @Override
    protected DomainItem<T> getUnfilteredItem(Object itemId) {
        itemById.get(itemId)
    }

    @Override
    Collection<?> getContainerPropertyIds() {
        propertyIds
    }

    @Override
    Property getContainerProperty(Object itemId, Object propertyId) {
        getItem(itemId)?.getItemProperty(propertyId)
    }

    boolean addContainerProperty(String propertyId, VaadinPropertyDescriptor<T> propertyDescriptor) {
        if (null == propertyId || null == propertyDescriptor) {
            return false
        }

        if (propertyIds.contains(propertyId)) {
            return false
        }

        propertyIds.add(propertyId)
        for (DomainItem<T> item : itemById.values()) {
            item.addItemProperty(propertyId, propertyDescriptor.createProperty(item.object))
        }

        fireContainerPropertySetChange()
        true
    }

    public boolean addNestedContainerProperty(String propertyId) {
        return addContainerProperty(propertyId, new NestedPropertyDescriptor(
                propertyId, type));
    }

    @Override
    Class<?> getType(Object propertyId) {
        def domainClass = DomainItem.getDomainClass(type)
        domainClass.getPropertyByName(propertyId).type
    }

    @Override
    void addContainerFilter(Container.Filter filter) throws UnsupportedFilterException {
        super.addFilter(filter)
    }

    @Override
    void removeContainerFilter(Container.Filter filter) {
        super.removeFilter(filter)
    }

    @Override
    void removeAllContainerFilters() {
        super.removeAllFilters()
    }

    @Override
    void sort(Object[] propertyId, boolean[] ascending) {
        super.sortContainer(propertyId, ascending)
    }

    @Override
    Collection<?> getSortableContainerPropertyIds() {
        super.getSortablePropertyIds()
    }

    @Override
    void valueChange(Property.ValueChangeEvent event) {
        super.filterAll()
    }

    @Override
    void addPropertySetChangeListener(Container.PropertySetChangeListener listener) {
        super.addPropertySetChangeListener(listener)
    }
}