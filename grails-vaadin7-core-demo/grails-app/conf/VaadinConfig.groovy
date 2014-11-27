vaadin {

//    defaultFragment = "index"

    mappings {

        "/demo1" {
            ui = "demo"
            namespace = "ns1"
            theme = "valo"
            pageTitle = "Demo Nr 1"


            fragments {
//                set the default action (view) to index
                "index" {
                    view = "index"
                    namespace = "ns1"
                }
                "two" {
                    view = "second"
                }
            }

        }


        "/demo2" {
            ui = "demo"
            theme = "valo"
            pageTitle = "Demo Nr 1"


            fragments {
                "index" {
                    view = "index"
                    namespace = "ns1"
                }
                "two" {
                    view = "second"
                }
            }

        }
    }

}