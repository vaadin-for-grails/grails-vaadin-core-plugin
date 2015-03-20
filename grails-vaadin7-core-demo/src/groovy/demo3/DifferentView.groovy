package demo3

import com.vaadin.grails.Vaadin
import com.vaadin.navigator.View
import com.vaadin.navigator.ViewChangeListener
import com.vaadin.ui.CustomComponent
import com.vaadin.ui.HorizontalLayout

class DifferentView extends CustomComponent implements View {

    HorizontalLayout sidebarAndContent

    DifferentView() {
        init()
    }

    protected void init() {
        compositionRoot = Vaadin.build {
            verticalLayout(sizeFull: true, margin: true, spacing: true) {
                label(value: "Different view", styleName: "h1 colored")
                sidebarAndContent = horizontalLayout(sizeFull: true, spacing: true) {
                    verticalLayout(sizeFull: true, expandRatio: 1, spacing: true) {
                        label(value: "Welcome to a different view", styleName: "h2")
                        label(value: "As you see, the Sidebar remembers its state!", styleName: "light")
                        button(caption: "Go back", styleName: "danger", clickListener: {
                            Vaadin.enter(fragment: "")
                        })
                    }
                }
            }
        }
    }

    @Override
    void enter(ViewChangeListener.ViewChangeEvent event) {
        sidebarAndContent.addComponent(Sidebar.instance, 0)
    }
}
